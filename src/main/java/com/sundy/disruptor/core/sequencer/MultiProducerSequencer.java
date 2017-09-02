package com.sundy.disruptor.core.sequencer;

import java.util.concurrent.locks.LockSupport;

import sun.misc.Unsafe;

import com.sundy.disruptor.core.exception.InsufficientCapacityException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.waitstrategy.WaitStrategy;
import com.sundy.disruptor.util.Util;

/**
 * 协调器，用于获取访问数据结构中的索引序号，该索引序号可用于跟踪
 * <p/>
 * 该实例适用于 多个发布器线程存在的情况
 * @author Administrator
 *
 */
public final class MultiProducerSequencer extends AbstractSequencer {

	private static final Unsafe UNSAFE = Util.getUnsafe();
	private static final long BASE = UNSAFE.arrayBaseOffset(int[].class);
	private static final long SCALE = UNSAFE.arrayIndexScale(int[].class);
	
	private final Sequence gateSequenceCache = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
	
	/**
	 * 用于追踪ringbuffer中每一个插槽的状态
	 */
	private final int[] availableBuffer; 
	private final int indexMask;
	private final int indexShift;
	
	
	public MultiProducerSequencer(int bufferSize, final WaitStrategy waitStrategy) {
		super(bufferSize, waitStrategy);
		this.availableBuffer = new int[bufferSize];
		this.indexMask = bufferSize - 1;
		this.indexShift = Util.log2(bufferSize);
		initialiseAbailableBuffer();
	}
	
	@Override
	public boolean hasAvailableCapacity(int requiredCapacity) {
		return hasAvailableCapacity(gatingSequences,requiredCapacity,cursor.get());
	}


	private boolean hasAvailableCapacity(Sequence[] gatingSequences,
			int requiredCapacity, long cursorValue) {
		long wrapPoint = (cursorValue + requiredCapacity) - bufferSize;
		long cachedGatingSequence = gateSequenceCache.get();
		if(wrapPoint > cachedGatingSequence || cachedGatingSequence > cursorValue){
			long minSequence = Util.getMinimumSequence(gatingSequences, cursorValue);
			gateSequenceCache.set(minSequence);
			if(wrapPoint > minSequence){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void claim(long sequence) {
		cursor.set(sequence);
	}
	
	@Override
	public long next() {
		return next(1);
	}
	
	@Override
	public long next(int n) {
		if (n < 1) {
            throw new IllegalArgumentException("n must be > 0");
        }
		long current;
		long next;
		do {
			current = cursor.get();
			next = current + n;
			long wrapPoint = next - bufferSize;
			long cachedGatingSequence = gateSequenceCache.get();
			if(wrapPoint > cachedGatingSequence || cachedGatingSequence > current){
				long gatingSequence = Util.getMinimumSequence(gatingSequences, current);
				if(wrapPoint > gatingSequence){
					LockSupport.parkNanos(1);
					continue;
				}
				gateSequenceCache.set(gatingSequence);
			}else if(cursor.compareAndSet(current, next)){
				break;
			}
		} while (true);
		return next;
	}
	
	@Override
	public long tryNext() throws InsufficientCapacityException {
		return tryNext(1);
	}
	
	@Override
	public long tryNext(int n) throws InsufficientCapacityException {
		if (n < 1) {
            throw new IllegalArgumentException("n must be > 0");
        }
		long current;
		long next;
		do {
			current = cursor.get();
			next = current + n;
			if(!hasAvailableCapacity(n)){
				throw InsufficientCapacityException.INSTANCE;
			}
		} while (!cursor.compareAndSet(current, next));
		return next;
	}
	
	@Override
	public long remainingCapacity() {
		long consumed = Util.getMinimumSequence(gatingSequences, cursor.get());
		long produced = cursor.get();
		return getBufferSize() - (produced - consumed);
	}

	private void initialiseAbailableBuffer() {
		for(int i= availableBuffer.length-1; i!=0; i--){
			setAvailableBufferValue(i,-1);
		}
		setAvailableBufferValue(0, -1);
	}
	
	@Override
	public void publish(long sequence) {
		setAvailable(sequence);
		waitStrategy.signalAllWhenBlocking();
	}
	
	@Override
	public void publish(long lo, long hi) {
		for(long i=lo; i<=hi; i++){
			setAvailable(i);
		}
		waitStrategy.signalAllWhenBlocking();
	}


	private void setAvailable(final long sequence) {
		setAvailableBufferValue(calculateIndex(sequence), calculateAvailabilityFlag(sequence));
	}


	private void setAvailableBufferValue(int index, int flag) {
		long bufferAddress = (index * SCALE) + BASE;
		UNSAFE.putOrderedLong(availableBuffer, bufferAddress, flag);
	}
	
	@Override
	public boolean isAvailable(long sequence) {
		int index = calculateIndex(sequence);
		int flag = calculateAvailabilityFlag(sequence);
		long bufferAddress = (index * SCALE) + BASE;
		return UNSAFE.getLongVolatile(availableBuffer, bufferAddress) == flag;
	}
	
	@Override
	public long getHighestPublishedSequence(long lowerBound,
			long availableSequence) {
		for(long sequence=lowerBound; sequence < availableSequence; sequence++){
			if(!isAvailable(sequence)){
				return sequence -1;
			}
		}
		return availableSequence;
	}
	
	
	private int calculateIndex(long sequence) {
		return ((int)sequence) & indexMask;
	}
	
	private int calculateAvailabilityFlag(long sequence) {
		return (int) (sequence >>> indexShift);
	}
	
	

}

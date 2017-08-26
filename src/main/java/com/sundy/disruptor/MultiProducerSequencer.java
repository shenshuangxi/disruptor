package com.sundy.disruptor;

import java.util.concurrent.locks.LockSupport;

import com.sundy.disruptor.util.Util;

import sun.misc.Unsafe;

public final class MultiProducerSequencer extends AbstractSequencer {

	private static final Unsafe UNSAFE = Util.getUnsafe();
	private static final long BASE = UNSAFE.arrayBaseOffset(int[].class);
	private static final long SCALE = UNSAFE.arrayIndexScale(int[].class);
	
	private final Sequence gatingSequenceCache = new Sequence(INITIAL_CURSOR_VALUE);
	
	private final int[] availableBuffer;
	private final int indexMask;
	private final int indexShift;
	
	public MultiProducerSequencer(int bufferSize, WaitStrategy waitStrategy) {
		super(bufferSize, waitStrategy);
		availableBuffer = new int[bufferSize];
		indexMask = bufferSize-1;
		indexShift = Util.log2(bufferSize);
		initialiseAvailableBuffer();
	}
	
	public boolean hasAvailableCapacity(int requiredCapacity) {
		return hasAvailableCapacity(gatingSequences, requiredCapacity, cursor.get());
	}
	

	private boolean hasAvailableCapacity(Sequence[] gatingSequences, final int requiredCapacity, long cursorValue) {
		long wrapPoint = (cursorValue + requiredCapacity) - bufferSize;
		long cacheGatingSequence = gatingSequenceCache.get();
		if(wrapPoint>cacheGatingSequence || cacheGatingSequence>cursorValue){
			long minSequence = Util.getMinimumSequence(gatingSequences, cursorValue);
			gatingSequenceCache.set(minSequence);
			if(wrapPoint > minSequence){
				return false;
			}
		}
		return true;
	}

	private void initialiseAvailableBuffer() {
		for(int i=availableBuffer.length;i!=0;i--){
			setAvailableBufferValue(i, -1);
		}
		setAvailableBufferValue(0, -1);
	}

	private void setAvailableBufferValue(int index, int flag) {
		long bufferAddress = (index*SCALE)+BASE;
		UNSAFE.putOrderedInt(availableBuffer, bufferAddress, flag);
	}

	
	public void clain(long sequence) {
		cursor.set(sequence);
	}

	public long next() {
		return next(1);
	}

	public long next(int n) {
		if (n < 1)
        {
            throw new IllegalArgumentException("n must be > 0");
        }
		long current;
		long next;
		do {
			current = cursor.get();
			next = current + n;
			long wrapPoint = next - bufferSize;
			long cachedGatingSequence = gatingSequenceCache.get();
			if(wrapPoint>cachedGatingSequence || cachedGatingSequence > current){
				long gatingSequence = Util.getMinimumSequence(gatingSequences,current);
				if(wrapPoint > gatingSequence){
					LockSupport.parkNanos(1);
					continue;
				}
				gatingSequenceCache.set(gatingSequence);
			} else if(cursor.compareAndSet(current, next)) {
				break;
			}
		} while (true);
		return next;
	}

	public long tryNext() throws InsufficientCapacityException {
		return tryNext(1);
	}

	public long tryNext(int n) throws InsufficientCapacityException {
		if (n < 1)
        {
            throw new IllegalArgumentException("n must be > 0");
        }
		long current;
		long next;
		do {
			current = cursor.get();
			next = current + n;
			if(!hasAvailableCapacity(gatingSequences, n, current)){
				throw InsufficientCapacityException.INSTANCE;
			}
		} while (!cursor.compareAndSet(current, next));
		return 0;
	}

	public long remainingCapactiy() {
		long consumed = Util.getMinimumSequence(gatingSequences,cursor.get());
		long produced = cursor.get();
		return getBufferSize()-(produced-consumed);
	}


	public void publish(long sequence) {
		setAvailable(sequence);
		waitStrategy.signalAllWhenBlocking();

	}

	private void setAvailable(long sequence) {
		setAvailableBufferValue(calculateIndex(sequence), calculateAvailabilityFlag(sequence));
	}

	private int calculateAvailabilityFlag(long sequence) {
		return (int) (sequence >>> indexShift);
	}

	private int calculateIndex(long sequence) {
		return ((int)sequence) & indexMask;
	}

	public void publish(long lo, long hi) {
		for (long l = lo; l <= hi; l++)
        {
            setAvailable(l);
        }
        waitStrategy.signalAllWhenBlocking();
	}

	public boolean isAvailable(long sequence) {
		int index = calculateIndex(sequence);
		int flag = calculateAvailabilityFlag(sequence);
		long bufferAddress = (index*SCALE)+BASE;
		return UNSAFE.getIntVolatile(availableBuffer, bufferAddress) == flag;

	}

	public long getHighestPublishedSequence(long lowerBound, long availableSequence) {
		for (long sequence = lowerBound; sequence <= availableSequence; sequence++)
        {
            if (!isAvailable(sequence))
            {
                return sequence - 1;
            }
        }
        return availableSequence;
	}

}

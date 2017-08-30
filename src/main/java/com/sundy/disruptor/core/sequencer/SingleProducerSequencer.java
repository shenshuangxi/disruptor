package com.sundy.disruptor.core.sequencer;

import com.sundy.disruptor.InsufficientCapacityException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.waitstrategy.WaitStrategy;
import com.sundy.disruptor.util.Util;

/**
 * 所以序号协调器，用于获取访问数据结构的索引序号，并跟踪这个序号
 * <p/>
 * 一般情况下，如果不通过barriers(序号栅栏)来使用，在多线程的情况下，是不安全的
 * @author Administrator
 *
 */
public final class SingleProducerSequencer extends AbstractSequencer {

	private static class Padding {
		public long nextValue = Sequence.INITIAL_VALUE,cachedValue = Sequence.INITIAL_VALUE,p2,p3,p4,p5,p6,p7;
	} 
	private final Padding pad = new Padding();
	
	public SingleProducerSequencer(int bufferSize, final WaitStrategy waitStrategy) {
		super(bufferSize, waitStrategy);
	}
	
	
	/**
	 * pad.nextValue 的值一定时大于或等于 pad.cachedValue的值  且这个差值肯定是在bufferSize之内
	 * 即 0=<pad.nextValue-pad.cachedValue<=bufferSize
	 * 那么所要获取的n个空间，那么有以下情况
	 * 1，pad.nextValue-pad.cachedValue+n>bufferSize  空间肯定不够 那么就需要重新刷新这个pad.cachedValue这个缓存获取正确的值
	 * 2,pad.cachedValue > pad.nextValue 这种情况是不应该存在的，需要重新刷新缓存
	 */
	@Override
	public boolean hasAvailableCapacity(final int requiredCapacity) {
		long nextValue = pad.nextValue;
		long wrapPoint = (nextValue + requiredCapacity) - bufferSize;
		long cachedGatingSequence = pad.cachedValue;
		if(wrapPoint > cachedGatingSequence || cachedGatingSequence > nextValue){
			long minSequence = Util.getMinimumSequence(gatingSequences, nextValue);
			pad.cachedValue = minSequence;
			if(wrapPoint > minSequence){
				return false;
			}
		}
		
		
		return true;
	}
	
	@Override
	public long next() {
		return next(1);
	}
	
	/**
	 * pad.nextValue 的值一定时大于或等于 pad.cachedValue的值  且这个差值肯定是在bufferSize之内
	 * 即 0=<pad.nextValue-pad.cachedValue<=bufferSize
	 * 那么所要获取的n个空间，那么有以下情况
	 * 1，pad.nextValue-pad.cachedValue+n>bufferSize  空间肯定不够 那么就需要重新刷新这个pad.cachedValue这个缓存获取正确的值
	 * 2,pad.cachedValue > pad.nextValue 这种情况是不应该存在的，需要重新刷新缓存
	 */
	@Override
	public long next(int n) {
		if(n < 1){
			throw new IllegalArgumentException("n must be > 0");
		}
		long nextValue = pad.nextValue;
		long nextSequence = nextValue + n;
		long wrapPoint = nextSequence - bufferSize;
		long cachedGatingSequence = pad.cachedValue;
		if(wrapPoint > cachedGatingSequence || cachedGatingSequence > nextValue){
			long minSequence;
			while(wrapPoint > (minSequence=Util.getMinimumSequence(gatingSequences, nextValue))){
				Thread.yield();
			}
			pad.cachedValue = minSequence;
		}
		pad.nextValue = nextSequence;
		return nextSequence;
	}
	
	@Override
	public long tryNext() throws InsufficientCapacityException {
		return tryNext(1);
	}
	
	@Override
	public long tryNext(int n) throws InsufficientCapacityException {
		if(n < 1){
			throw new IllegalArgumentException("n must be > 0");
		}
		if(!hasAvailableCapacity(n)){
			throw InsufficientCapacityException.INSTANCE;
		}
		long nextValue = pad.nextValue += n;
		return nextValue;
	}
	
	@Override
	public long remainingCapacity() {
		long nextValue = pad.nextValue;
		long consumed = Util.getMinimumSequence(gatingSequences, nextValue);
		long produced = nextValue;
		return getBufferSize() - (produced - consumed);
	}
	
	@Override
	public void claim(long sequence) {
		pad.nextValue = sequence;
	}
	
	@Override
	public void publish(long sequence) {
		cursor.set(sequence);
		waitStrategy.signalAllWhenBlocking();
	}
	
	@Override
	public void publish(long lo, long hi) {
		publish(hi);
	}
	
	@Override
	public boolean isAvailable(long sequence) {
		return sequence <= cursor.get();
	}
	
	@Override
	public long getHighestPublishedSequence(long sequence,
			long availableSequence) {
		return availableSequence;
	}
	

}

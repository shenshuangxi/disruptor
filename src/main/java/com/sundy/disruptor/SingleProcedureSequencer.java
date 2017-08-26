package com.sundy.disruptor;

import com.sundy.disruptor.util.Util;

public class SingleProcedureSequencer extends AbstractSequencer {

	private static class Padding {
		public long nextValue = Sequence.INITIAL_VALUE;
		public long cachedValue = Sequence.INITIAL_VALUE;
		public long p2,p3,p4,p5,p6,p7;
	}
	
	private final Padding pad = new Padding();
	
	public SingleProcedureSequencer(int bufferSize, final WaitStrategy waitStrategy) {
		super(bufferSize, waitStrategy);
	}
	
	public boolean hasAvailableCapacity(int requiredCapacity) {
		long nextValue = pad.cachedValue;
		long wrapPoint = (nextValue+requiredCapacity) - bufferSize;
		long cachedGatingSequence = pad.cachedValue;
		
		if(wrapPoint>cachedGatingSequence || cachedGatingSequence > nextValue){
			long minSequence = Util.getMinimumSequence(gatingSequences, nextValue);
            pad.cachedValue = minSequence;
            if (wrapPoint > minSequence)
            {
                return false;
            }
		}
		return true;
	}

	public long next() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long next(int n) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long tryNext() throws InsufficientCapacityException {
		// TODO Auto-generated method stub
		return 0;
	}

	public long tryNext(int n) throws InsufficientCapacityException {
		// TODO Auto-generated method stub
		return 0;
	}

	public long remainingCapactiy() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void clain(long sequence) {
		// TODO Auto-generated method stub

	}

	public void publish(long sequence) {
		// TODO Auto-generated method stub

	}

	public void publish(long lo, long hi) {
		// TODO Auto-generated method stub

	}

	public boolean isAvailable(long sequence) {
		// TODO Auto-generated method stub
		return false;
	}

	public long getHighestPublishedSequence(long sequence,
			long availableSequence) {
		// TODO Auto-generated method stub
		return 0;
	}

}

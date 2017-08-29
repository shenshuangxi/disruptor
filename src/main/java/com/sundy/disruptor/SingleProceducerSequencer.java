package com.sundy.disruptor;

import com.sundy.disruptor.util.Util;

public class SingleProceducerSequencer extends AbstractSequencer {

	@SuppressWarnings("unused")
	private static class Padding {
		public long nextValue = Sequence.INITIAL_VALUE;
		public long cachedValue = Sequence.INITIAL_VALUE;
		public long p2,p3,p4,p5,p6,p7;
	}
	
	private final Padding pad = new Padding();
	
	public SingleProceducerSequencer(int bufferSize, final WaitStrategy waitStrategy) {
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
		return next(1);
	}

	public long next(int n) {
		if (n < 1)
        {
            throw new IllegalArgumentException("n must be > 0");
        }
		long nextValue = pad.nextValue;
		long nextSequence = nextValue + n;
		long wrapPoint = nextSequence - bufferSize;
		long cachedGatingSequence = pad.cachedValue;
		
		if(wrapPoint > cachedGatingSequence || cachedGatingSequence > nextValue){
			long minSequence;
			while (wrapPoint > (minSequence=Util.getMinimumSequence(gatingSequences, nextValue))) {
				Thread.yield();
			}
			pad.cachedValue = minSequence;
		}
		pad.nextValue = nextSequence;
		return nextSequence;
	}

	public long tryNext() throws InsufficientCapacityException {
		return tryNext(1);
	}

	public long tryNext(int n) throws InsufficientCapacityException {
		if (n < 1)
        {
            throw new IllegalArgumentException("n must be > 0");
        }
		if (!hasAvailableCapacity(n))
        {
            throw InsufficientCapacityException.INSTANCE;
        }
		long nextSequence = pad.nextValue += n;

        return nextSequence;
	}

	public long remainingCapactiy() {
		long nextValue = pad.nextValue;
		long consumed = Util.getMinimumSequence(gatingSequences, nextValue);
		long produced = nextValue;
		return getBufferSize() - (produced - consumed);
	}

	public void clain(long sequence) {
		pad.nextValue = sequence;
	}

	public void publish(long sequence) {
		cursor.set(sequence);
		waitStrategy.signalAllWhenBlocking();
	}

	public void publish(long lo, long hi) {
		publish(hi);
	}

	public boolean isAvailable(long sequence) {
		return sequence <= cursor.get();
	}

	public long getHighestPublishedSequence(long sequence, long availableSequence) {
		return availableSequence;
	}

}

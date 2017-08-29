package com.sundy.disruptor.core.sequencer;

import com.sundy.disruptor.core.Cursored;

public interface Sequencer extends Cursored {

	long INITIAL_CURSOR_VALUE = -1L;
	
	int getBufferSize();
	
	boolean hasAvailableCapacity(final int requiredCapacity);
	
	long next();
	
	long next(int n);
	
	long tryNext();
	
	long tryNext(int n);
	
	long remainingCapacity();
	
	void claim(long sequence);
	
	void publish(long sequence);
	
	void publish(long lo, long hi);
	
	boolean isAvailable(long sequence);
	
	void addGatingSequences(Sequence... gatingSequences);
	
	boolean removeGatingSequence();
	
	SequenceBarrier newBarrier(Sequence... sequencesToTrack);
	
	long getMinimumSequence();
	
	long getHighestPublishedSequence(long sequence, long availableSequence);
	
}

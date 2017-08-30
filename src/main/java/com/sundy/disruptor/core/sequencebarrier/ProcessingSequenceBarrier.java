package com.sundy.disruptor.core.sequencebarrier;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.FixedSequenceGroup;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencer.Sequencer;
import com.sundy.disruptor.core.waitstrategy.WaitStrategy;

public final class ProcessingSequenceBarrier implements SequenceBarrier {

	private final WaitStrategy waitStrategy;
	private final Sequence dependentSequence;
	private volatile boolean alerted = false;
	private final Sequence cursorSequence;
	private final Sequencer sequencer;
	
	public ProcessingSequenceBarrier(final Sequencer sequencer,
									 final WaitStrategy waitStrategy,
									 final Sequence cursorSequence,
									 final Sequence[] dependentSequences) {
		this.sequencer = sequencer;
		this.waitStrategy = waitStrategy;
		this.cursorSequence = cursorSequence;
		if(dependentSequences.length == 0){
			this.dependentSequence = cursorSequence;
		}else{
			dependentSequence = new FixedSequenceGroup(dependentSequences);
		}
	}
	
	@Override
	public long waitFor(long sequence) throws AlertException,
			InterruptedException, TimeoutException {
		checkAlert();
		long availableSequence = waitStrategy.waitFor(sequence, cursorSequence, dependentSequence, this);
		if(availableSequence < sequence){
			return availableSequence;
		}
		return sequencer.getHighestPublishedSequence(sequence, availableSequence);
	}

	@Override
	public long getCursor() {
		return dependentSequence.get();
	}

	@Override
	public boolean isAlerted() {
		return alerted;
	}

	@Override
	public void alert() {
		alerted = true;
		waitStrategy.signalAllWhenBlocking();
	}

	@Override
	public void clearAlert() {
		alerted = false;
	}

	@Override
	public void checkAlert() throws AlertException {
		if(alerted){
			throw AlertException.INSTANCE;
		}
	}

}

package com.sundy.disruptor;

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

/**
 * SequenceBarrier为使用给定的WaitStrategy的游标序列和可选的依赖EventProcessor选择EventProcessors。
 * @author Administrator
 *
 */
public class ProcessingSequenceBarrier implements SequenceBarrier {

	private final WaitStrategy waitStrategy;
	private final Sequence dependentSequence;
	private volatile boolean alerted = false;
	private final Sequence cursorSequence;
	private final Sequencer sequencer;
	
	public ProcessingSequenceBarrier(final Sequencer sequencer, 
			final WaitStrategy waitStrategy,
			final Sequence cursorSequence,
			final Sequence[] dependentSequences
			 ) {
		this.sequencer = sequencer;
		this.waitStrategy = waitStrategy;
		this.cursorSequence = cursorSequence;
		if(0 == dependentSequences.length){
			this.dependentSequence = cursorSequence;
		} else {
			dependentSequence = new FixedSequenceGroup(dependentSequences);
		}
		
	}

	public long waitFor(long sequence) throws AlertException,
			InterruptedException, TimeoutException {
		checkAlert();
		long availableSequence = waitStrategy.waitFor(sequence, cursorSequence, dependentSequence, this);
		if(availableSequence < sequence){
			return sequence;
		}
		return sequencer.getHighestPublishedSequence(sequence, availableSequence);
	}

	public long getCursor() {
		return dependentSequence.get();
	}

	public boolean isAlerted() {
		return alerted;
	}

	public void alert() {
		alerted = true;
		waitStrategy.signalAllWhenBlocking();
	}

	public void clearAlert() {
		alerted = false;
	}

	public void checkAlert() throws AlertException {
		if(alerted){
			throw AlertException.INSTANCE;
		}
	}

}

package com.sundy.disruptor.core.eventprocessor;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sundy.disruptor.core.RingBuffer;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencer.Sequencer;

/**
 * 无任何操作的 {@link EventProcessor}实例，仅仅只跟踪了 {@link Sequence}
 * <p/>
 * 该类对于测试或者publisher用于提前填充{@link RingBuffer}非常有用
 * @author Administrator
 *
 */
public class NoOpEventProcessor implements EventProcessor {

	private final SequencerFollowingSequence sequence;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	public NoOpEventProcessor(final RingBuffer<?> sequencer) {
		this.sequence = new SequencerFollowingSequence(sequencer);
	}
	
	@Override
	public void run() {
		if(!this.running.compareAndSet(false, true)){
			throw new IllegalStateException("Thread is already running");
		}
	}

	@Override
	public Sequence getSequence() {
		return sequence;
	}

	@Override
	public void halt() {
		this.running.set(false);
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}
	
	private static final class SequencerFollowingSequence extends Sequence{
		
		private final RingBuffer<?> sequencer;
		
		public SequencerFollowingSequence(final RingBuffer<?> sequencer) {
			super(Sequencer.INITIAL_CURSOR_VALUE);
			this.sequencer = sequencer;
		}
		
		@Override
		public long get() {
			return sequencer.getCursor();
		}
	}

}

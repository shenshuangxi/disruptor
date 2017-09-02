package com.sundy.disruptor.dsl.consumerInfo;

import java.util.concurrent.Executor;

import com.sundy.disruptor.core.eventhandler.EventHandler;
import com.sundy.disruptor.core.eventprocessor.EventProcessor;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

public class EventProcessorInfo<T> implements ConsumerInfo {

	private final EventProcessor eventProcessor;
	private final EventHandler<T> eventHandler;
	private final SequenceBarrier sequenceBarrier;
	private boolean endOfChain = true;
	
	public EventProcessorInfo(EventProcessor eventProcessor,
			EventHandler<T> eventHandler, SequenceBarrier sequenceBarrier) {
		this.eventProcessor = eventProcessor;
		this.eventHandler = eventHandler;
		this.sequenceBarrier = sequenceBarrier;
	}

	public EventProcessor getEventProcessor(){
		return eventProcessor;
	}
	
	@Override
	public Sequence[] getSequences() {
		return new Sequence[]{eventProcessor.getSequence()};
	}
	
	public EventHandler<T> getHandler() {
        return eventHandler;
    }

	@Override
	public SequenceBarrier getBarrier() {
		return sequenceBarrier;
	}

	@Override
	public boolean isEndOfChain() {
		return endOfChain;
	}

	@Override
	public void start(Executor executor) {
		executor.execute(eventProcessor);
	}

	@Override
	public void halt() {
		eventProcessor.halt();
	}

	@Override
	public void markAsUsedInBarrier() {
		endOfChain = false;
	}

	@Override
	public boolean isRunning() {
		return eventProcessor.isRunning();
	}

}

package com.sundy.disruptor.dsl;

import com.sundy.disruptor.core.sequence.Sequence;

public class EventHandlerGroup<T> {

	private final Disruptor<T> disruptor;
	private final ConsumerRepository<T> consumerRepository;
	private final Sequence[] sequences;
	
	
	public EventHandlerGroup(Disruptor<T> disruptor,
			ConsumerRepository<T> consumerRepository,
			Sequence[] sequences) {
		this.disruptor = disruptor;
		this.consumerRepository = consumerRepository;
		this.sequences = sequences;
	}

}

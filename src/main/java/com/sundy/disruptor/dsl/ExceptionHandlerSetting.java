package com.sundy.disruptor.dsl;

import com.sundy.disruptor.core.eventhandler.EventHandler;
import com.sundy.disruptor.core.eventprocessor.BatchEventProcessor;
import com.sundy.disruptor.core.exceptionhandler.ExceptionHandler;

public class ExceptionHandlerSetting<T> {

	private final EventHandler<T> eventHandler;
	private final ConsumerRepository<T> consumerRepository;
	
	public ExceptionHandlerSetting(EventHandler<T> eventHandler,
			ConsumerRepository<T> consumerRepository) {
		this.eventHandler = eventHandler;
		this.consumerRepository = consumerRepository;
	}
	
	public void with(ExceptionHandler exceptionHandler){
		((BatchEventProcessor<?>)consumerRepository.getEventProcessorFor(eventHandler)).setExceptionHandler(exceptionHandler);
		consumerRepository.getBarrierFor(eventHandler).alert();
	}
	
	
}

package com.sundy.disruptor.dsl;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sundy.disruptor.core.RingBuffer;
import com.sundy.disruptor.core.WorkerPool;
import com.sundy.disruptor.core.eventFactory.EventFactory;
import com.sundy.disruptor.core.eventhandler.EventHandler;
import com.sundy.disruptor.core.eventprocessor.BatchEventProcessor;
import com.sundy.disruptor.core.eventprocessor.EventProcessor;
import com.sundy.disruptor.core.exceptionhandler.ExceptionHandler;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;
import com.sundy.disruptor.core.waitstrategy.WaitStrategy;
import com.sundy.disruptor.core.workhandler.WorkHandler;
import com.sundy.disruptor.util.Util;

public class Disruptor<T> {

	private final RingBuffer<T> ringBuffer;
	private final Executor executor;
	private final ConsumerRepository<T> consumerRepository = new ConsumerRepository<T>();
	private final AtomicBoolean start = new AtomicBoolean(false);
	private ExceptionHandler exceptionHandler;
	
	private Disruptor(final RingBuffer<T> ringBuffer, final Executor executor){
		this.ringBuffer = ringBuffer;
		this.executor = executor;
	}
	
	public Disruptor(
			final EventFactory<T> eventFactory,
			final int bufferSize,
			final Executor executor) {
		this(RingBuffer.createMultiProducer(eventFactory, bufferSize), executor);
	}
	
	public Disruptor(
			final EventFactory<T> eventFactory,
			final int bufferSize,
			final Executor executor,
			final ProducerType producerType,
			final WaitStrategy waitStrategy) {
		this(RingBuffer.create(producerType, eventFactory, bufferSize, waitStrategy), executor);
	}
	
	
	@SuppressWarnings("unchecked")
	public EventHandlerGroup<T> handleEventsWith(final EventHandler<T>... eventHandlers){
		return createEventProcessor(new Sequence[0],eventHandlers);
	}
	
	public EventHandlerGroup<T> handleEventsWith(final EventProcessor... eventProcessors){
		for(EventProcessor eventProcessor : eventProcessors){
			consumerRepository.add(eventProcessor);
		}
		return new EventHandlerGroup<T>(this, consumerRepository, Util.getSequencesFor(eventProcessors));
	}
	
	public EventHandlerGroup<T> handleEventsWithWorkerPool(final WorkHandler<T>... workHandlers){
		return createWorkerPool(new Sequence[0], workHandlers);
	}
	
	public void handleExceptionsWith(final ExceptionHandler exceptionHandler){
		this.exceptionHandler = exceptionHandler;
	}
	
	public ExceptionHandlerSetting<?> handleExceptionsFor(final EventHandler<T> eventHandler){
		return new ExceptionHandlerSetting<T>(eventHandler, consumerRepository);
	} 
	
	
	public EventHandlerGroup<T> createWorkerPool(final Sequence[] barrierSequences,
			final WorkHandler<T>[] workHandlers) {
		final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(barrierSequences);
		final WorkerPool<T> workerPool = new WorkerPool<T>(ringBuffer, sequenceBarrier, exceptionHandler, workHandlers);
		consumerRepository.add(workerPool, sequenceBarrier);
		return new EventHandlerGroup<T>(this, consumerRepository, workerPool.getWorkerSequences());
	}

	public EventHandlerGroup<T> createEventProcessor(Sequence[] barrierSequences, EventHandler<T>[] eventHandlers) {
		checkNotStarted();
		final Sequence[] processorSequences = new Sequence[barrierSequences.length];
		final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(processorSequences);
		for (int i = 0,eventHandlersLenght = eventHandlers.length; i < eventHandlersLenght; i++) {
			final EventHandler<T> eventHandler = eventHandlers[i];
			final BatchEventProcessor<T> batchEventProcessor = new BatchEventProcessor<T>(ringBuffer, sequenceBarrier, eventHandler);
			if(exceptionHandler!=null){
				batchEventProcessor.setExceptionHandler(exceptionHandler);
			}
			consumerRepository.add(batchEventProcessor, eventHandler, sequenceBarrier);
			processorSequences[i] = batchEventProcessor.getSequence();
		}
		if(processorSequences.length > 0){
			consumerRepository.unMarkEventProcessorsAsEndOfChain(barrierSequences);
		}
		return new EventHandlerGroup<T>(this,consumerRepository,processorSequences);
	}

	private void checkNotStarted() {
		if(start.get()){
			throw new IllegalStateException("All event handlers must be added before calling starts.");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}

package com.sundy.disruptor.core.eventprocessor;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sundy.disruptor.core.RingBuffer;
import com.sundy.disruptor.core.LifecycleAware;
import com.sundy.disruptor.core.eventreleaser.EventReleaseAware;
import com.sundy.disruptor.core.eventreleaser.EventReleaser;
import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exceptionhandler.ExceptionHandler;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;
import com.sundy.disruptor.core.workhandler.WorkHandler;

/**
 * 一个{@link WorkProcessor} 关联一个 {@link WorkHandler} ,能够非常高效的消费索引序号，以及确保这个序号跟进栅栏里的
 * <p/>
 * 一般情况下，该类或作为{@link workerPool}的一员
 * @author Administrator
 *
 * @param <T>
 */
public final class WorkProcessor<T> implements EventProcessor {

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final Sequence sequence = new Sequence(Sequence.INITIAL_VALUE);
	private final RingBuffer<T> ringBuffer;
	private final SequenceBarrier sequenceBarrier;
	private final WorkHandler<T> workHandler;
	private final ExceptionHandler exceptionHandler;
	private final Sequence workSequence;
	private final EventReleaser eventReleaser = new EventReleaser() {
		@Override
		public void release() {
			sequence.set(Long.MAX_VALUE);
		}
	};
	
	
	public WorkProcessor(
			final RingBuffer<T> ringBuffer,
			final SequenceBarrier sequenceBarrier,
			final WorkHandler<T> workHandler,
			final ExceptionHandler exceptionHandler,
			final Sequence workSequence) {
		this.ringBuffer = ringBuffer;
		this.sequenceBarrier = sequenceBarrier;
		this.workHandler = workHandler;
		this.exceptionHandler = exceptionHandler;
		this.workSequence = workSequence;
		if(this.workHandler instanceof EventReleaseAware){
			((EventReleaseAware) this.workHandler).setEventReleaser(eventReleaser);
		}
	}
	

	@Override
	public Sequence getSequence() {
		return this.sequence;
	}

	@Override
	public void halt() {
		this.running.set(false);
		sequenceBarrier.alert();
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}
	
	@Override
	public void run() {
		if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Thread is already running");
        }
		sequenceBarrier.clearAlert();
		notifyStart();
		
		boolean processedSequence = true;
		long cachedAvailableSequence = Long.MIN_VALUE;
		long nextSequence = sequence.get();
		T event = null;
		while (true) {
			try {
				/**
				 * 如果前一个所以序列已被处理，那么就去获取下一个索引序列，并设置当前序列已被成功处理。
				 */
				if(processedSequence){
					processedSequence = false;
					do {
						nextSequence = workSequence.get() + 1;
						sequence.set(nextSequence);
					} while (!workSequence.compareAndSet(nextSequence-1L, nextSequence));
				}
				if(cachedAvailableSequence >= nextSequence){
					event = ringBuffer.get(nextSequence);
					workHandler.onEvent(event);
					processedSequence = true;
				}else{
					cachedAvailableSequence = sequenceBarrier.waitFor(nextSequence);
				}
			} catch (AlertException e) {
				if(!running.get()){
					break;
				}
			}  catch (final Throwable e) {
				exceptionHandler.handleEventException(e, nextSequence, event);
				processedSequence = true;
			}
		}

		notifyShutdown();
		running.set(false);
		
	}


	private void notifyStart() {
		if(workHandler instanceof LifecycleAware){
			try {
				((LifecycleAware) workHandler).onStart();
			} catch (Exception e) {
				exceptionHandler.handleOnStartException(e);
			}
		}
	}
	
	private void notifyShutdown() {
		if(workHandler instanceof LifecycleAware){
			try {
				((LifecycleAware) workHandler).onShutdown();
			} catch (Exception e) {
				exceptionHandler.handleOnShutdownException(e);
			}
		}
	}

}

package com.sundy.disruptor.core.eventprocessor;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sundy.disruptor.core.RingBuffer;
import com.sundy.disruptor.core.DataProvider;
import com.sundy.disruptor.core.LifecycleAware;
import com.sundy.disruptor.core.eventhandler.EventHandler;
import com.sundy.disruptor.core.eventhandler.SequenceReportingEventHandler;
import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.exceptionhandler.ExceptionHandler;
import com.sundy.disruptor.core.exceptionhandler.FatalExceptionHandler;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;
import com.sundy.disruptor.core.timeouthandler.TimeoutHandler;

/**
 * 辅助类，用于批量处理从{@link RingBuffer}中获取的事件，并将这些可用事件送到 {@link EventHandler} 处理
 * @author Administrator
 *
 * @param <T>
 */
public final class BatchEventProcessor<T> implements EventProcessor {

	private final AtomicBoolean running = new AtomicBoolean(false);
	private ExceptionHandler exceptionHandler = new FatalExceptionHandler();
	private final DataProvider<T> dataProvider;
	private final SequenceBarrier sequenceBarrier;
	private final EventHandler<T> eventHandler;
	private final Sequence sequence = new Sequence(Sequence.INITIAL_VALUE);
	private final TimeoutHandler timeoutHandler;
	
	public BatchEventProcessor(final DataProvider<T> dataProvider,
							   final SequenceBarrier sequenceBarrier,
							   final EventHandler<T> eventHandler) {
		this.dataProvider = dataProvider;
		this.sequenceBarrier = sequenceBarrier;
		this.eventHandler = eventHandler;
		if(eventHandler instanceof SequenceReportingEventHandler){
			((SequenceReportingEventHandler<?>) eventHandler).setSequenceCallback(sequence);
		}
		this.timeoutHandler =  (eventHandler instanceof TimeoutHandler) ? (TimeoutHandler)eventHandler : null;
	}
	
	@Override
	public Sequence getSequence() {
		return sequence;
	}
	
	@Override
	public void halt() {
		running.set(false);
		sequenceBarrier.alert();

	}
	
	@Override
	public boolean isRunning() {
		return running.get();
	}
	
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		if (null == exceptionHandler){
            throw new NullPointerException();
        }
		this.exceptionHandler = exceptionHandler;
	}
	
	@Override
	public void run() {
		if(!running.compareAndSet(false, true)){
			throw new IllegalStateException("Thread is already running");
		}
		sequenceBarrier.clearAlert();
		
		notifyStart();
		
		T event = null;
		long nextSequence = sequence.get() + 1L;
		
		try {
			while (true) {
				try {
					final long availableSequence = sequenceBarrier.waitFor(nextSequence);
					if (nextSequence > availableSequence) {
						Thread.yield();
					}
					while (nextSequence <= availableSequence) {
						event = dataProvider.get(nextSequence);
						eventHandler.onEvent(event, nextSequence, nextSequence == availableSequence);
						nextSequence++;
					}
					sequence.set(availableSequence);
				} catch (final AlertException e) {
					if (!running.get()) {
						break;
					}
				} catch (TimeoutException e) {
					notifyTimeout(sequence.get());
				} catch (final Throwable e) {
					exceptionHandler.handleEventException(e, nextSequence,
							event);
					sequence.set(nextSequence);
					nextSequence++;
				}
			}
		} finally {
			notifyShutdown();
			running.set(false);
		}

	}

	private void notifyShutdown() {
		if(eventHandler instanceof LifecycleAware){
			try {
				((LifecycleAware) eventHandler).onShutdown();
			} catch (final Throwable e) {
				exceptionHandler.handleOnShutdownException(e);
			}
		}
	}

	private void notifyTimeout(long sequence) {
		try {
			if(timeoutHandler!=null){
				timeoutHandler.onTimeout(sequence);
			}
		} catch (Throwable e) {
			exceptionHandler.handleEventException(e, sequence, null);
		}
	}

	/**
	 * 通知事件处理器，当前时间进程已启动
	 */
	private void notifyStart() {
		if(eventHandler instanceof LifecycleAware){
			try {
				((LifecycleAware) eventHandler).onStart();
			} catch (final Exception e) {
				exceptionHandler.handleOnStartException(e);
			}
		}
		
	}

	

	

	

}

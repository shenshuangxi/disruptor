package com.sundy.disruptor.core.exceptionhandler;

import com.sundy.disruptor.core.LifecycleAware;

/**
 * {@link BatchEventProcessor}  处理事件的生命周期中 产生没有捕获的异常，调用该类的实例来处理
 * @author Administrator
 *
 */
public interface ExceptionHandler {

	/**
	 * 处理 在处理事件时产生的未捕获异常。的策略
	 * <p/>
	 * 如果该策略希望停止 {@link BatchEventProcessor} 的进一步处理，可以抛出 {@link RuntimeException}
	 * @param ex {@link EventHandler} 处理事件抛出的异常
	 * @param sequence	事件的索引序号
	 * @param event 发生异常的事件
	 */
	void handleEventException(Throwable ex, long sequence, Object event);
	
	/**
	 * 作为回调用于通知  在{@link LifecycleAware#onStart()}  期间发生了异常
	 * @param ex
	 */
	void handleOnStartException(Throwable ex);
	

	/**
	 * 作为回调用于通知  在{@link LifecycleAware#onShutdown()}  期间发生了异常
	 * @param ex
	 */
	void handleOnShutdownException(Throwable ex);
	
}

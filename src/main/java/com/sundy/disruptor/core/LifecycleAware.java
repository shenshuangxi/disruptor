package com.sundy.disruptor.core;

/**
 * 该实例用于事件处理器，用于获取{@link BatchEventProcessor}的线程启用或停止的通知
 * @author Administrator
 *
 */
public interface LifecycleAware {

	/**
	 * 用于线程启动后，第一个事件处理前调用
	 */
	void onStart();
	
	/**
	 * 用于线程 shutdown前
	 */
	void onShutdown();
	
}

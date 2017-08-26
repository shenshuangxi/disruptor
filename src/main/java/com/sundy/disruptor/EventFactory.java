package com.sundy.disruptor;

/**
 * {@link RingBuffer} 调用，用于将事件工厂产生的事件填充 RingBuffer
 * @author Administrator
 *
 * @param <T>
 */
public interface EventFactory<T> {

	/**
	 * 该接口继承类，用于产生事件实体，
	 * @return
	 */
	T newInstance();
	
}

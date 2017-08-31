package com.sundy.disruptor.core.eventFactory;

import com.sundy.disruptor.RingBuffer;

/**
 * {@link RingBuffer} 提前调用，用于填充ringbuffer
 * @author Administrator
 *
 * @param <T>
 */
public interface EventFactory<T> {

	/**
	 * 初始化一个事件，所有内存已被分配
	 * @return
	 */
	T newInstance();
	
}

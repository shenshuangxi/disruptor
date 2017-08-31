package com.sundy.disruptor.core.eventhandler;

import com.sundy.disruptor.RingBuffer;

/**
 * 回调接口的实现类，该实现类用于处理事件， 被{@link RingBuffer} 使用
 * @author Administrator
 *
 * @param <T>
 */
public interface EventHandler<T> {

	/**
	 * 处理  {@link RingBuffer} 发布的事件
	 * @param event {@link RingBuffer} 发布的事件
	 * @param sequence 事件在ringbuffer中的索引序号
	 * @param endOfBatch 判断是否是批量事件的最后一个
	 * @throws Exception
	 */
	void onEvent(T event, long sequence, boolean endOfBatch) throws Exception;
	
}

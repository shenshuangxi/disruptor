package com.sundy.disruptor.core.workhandler;

import com.sundy.disruptor.core.RingBuffer;

/**
 * 回调接口，用于处理在{@link RingBuffer}中已经可用的工作单元
 * @author Administrator
 *
 * @param <T>
 */
public interface WorkHandler<T> {

	/**
	 * 回调 用于判断一个工作单元是否需要被处理
	 * @param event  {@link RingBuffer}中已发布的事件
	 * @throws Exception	如果{@link WorkHandler}希望在链上进一步处理异常。
	 */
	void onEvent(T event) throws Exception;
	
}

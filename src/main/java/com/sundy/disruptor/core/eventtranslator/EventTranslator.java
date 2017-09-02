package com.sundy.disruptor.core.eventtranslator;

import com.sundy.disruptor.core.RingBuffer;

/**
 * 将数据转换为{@link RingBuffer}中声明的数据类型
 * <p/>
 * 当发布事件到RingBuffer，会提供一个EventTranslator，在发布的索引序号更新前，RingBuffer会根据序号选择下一个可用的事件，并提供一个EventTranslator来确保事件更新
 * @author Administrator
 *
 * @param <T>
 */
public interface EventTranslator<T> {

	/**
	 * 将数据转换为给定事件中的一个字段
	 * @param event 数据序号转换到该事件中的一个字段
	 * @param sequence 事件关联的索引序号
	 */
	void translateTo(final T event, long sequence);
	
}

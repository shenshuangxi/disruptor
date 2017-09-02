package com.sundy.disruptor.core.eventtranslator;

import com.sundy.disruptor.core.RingBuffer;

/**
 * 将其他数据转换到从{@link RingBuffer}中获取到的事件中
 * @author Administrator
 *
 * @param <T> 事件实例，存储用于交换的数据，或者是需要并行协调的事件
 */
public interface EventTranslatorOneArg<T, A> {

	/**
	 * 将数据转换为事件中的某个字段的值
	 * @param event	放入数据转换后对应字段的事件
	 * @param sequence	事件所在的索引序号
	 * @param arg0	第一个用于转换的具体参数
	 */
	void translateTo(final T event, long sequence, final A arg0);
	
}

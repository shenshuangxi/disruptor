package com.sundy.disruptor.core.eventhandler;

import com.sundy.disruptor.core.eventprocessor.BatchEventProcessor;
import com.sundy.disruptor.core.sequence.Sequence;

/**
 * 被{@link BatchEventProcessor} 作为回调方法用于通知处理器 事件已消费完成  处于{@link EventHandler#onEvent(Object, long, boolean)} 发生后调用
 * <p/>
 * 通常，该处理器用于处理一系列的批量操作比如写IO，该连续事件处理完成后，用于{@link Sequence#set} 更新索引序号，允许后续的处理器来处理该事件
 * @author Administrator
 *
 * @param <T>
 */
public interface SequenceReportingEventHandler<T> extends EventHandler<T> {

	/**
	 * {@link BatchEventProcessor} 用于回调处理
	 * @param sequenceCallback
	 */
	void setSequenceCallback(final Sequence sequenceCallback);
	
}

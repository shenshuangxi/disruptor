package com.sundy.disruptor.core.eventprocessor;

import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

/**
 * EventProcessors 等待RingBuffer中一个事件可用，并消费这个事件
 * <p/>
 * 一个EventProcessor 关联一个线程
 * @author Administrator
 *
 */
public interface EventProcessor extends Runnable{

	/**
	 * {@link EventProcessor} 通过该方法获取索引序号的引用
	 * @return
	 */
	Sequence getSequence();
	
	/**
	 * 停止 EventProcessor 线程，当天已经完成事件的消费处理，
	 * 该方法 调用 {@link SequenceBarrier#alert()} 来告诉线程 检测状态
	 */
	void halt();
	
	boolean isRunning();
	
}

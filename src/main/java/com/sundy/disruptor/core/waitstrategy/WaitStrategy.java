package com.sundy.disruptor.core.waitstrategy;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

/**
 * 等待策略，用于配置 {@link EventProcessor}s 等待一个游标所指的序号值
 * @author Administrator
 *
 */
public interface WaitStrategy {

	/**
	 * 等待获取可用的所以序号，该方法得到序号值一般会小于你所要的索引序号值，
	 * 该方法的常见用途是发出超时信号。任何一个 EventProcessor 都可以使用该策略类用于获取消息变的可用的通知，并且处理这些信号。
	 * {@link BatchEventProcessor} 会显示处理这个信号，如果需要的话，还可以发出一个超时信号
	 * @param sequence 等待的索引序号
	 * @param cursor	Ringbuffer上的主序号。等待/通知 策略需要这个来知道序号是否已被通知更新
	 * @param dependentSequence 等待哪个
	 * @param barrier	事件处理器等待的位置
	 * @return 返回序列值可能大于所请求的序列。
	 * @throws AlertException 如果Disruptor状态改变 抛出该异常
     * @throws InterruptedException 线程中断抛出
     * @throws TimeoutException	超时异常抛出
	 */
	long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier)throws AlertException, InterruptedException, TimeoutException;
	
	/**
	 * 当游标指向所等待的索引值时，发出信号给等待这个值的 {@link EventProcessor}
	 */
	void signalAllWhenBlocking();
	
}

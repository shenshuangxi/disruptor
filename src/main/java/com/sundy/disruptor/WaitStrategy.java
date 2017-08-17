package com.sundy.disruptor;

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

/**
 * 用于使EventProcessors等待游标的策略序列
 * @author Administrator
 *
 */
public interface WaitStrategy {

	/**
	 * 等待一个可用的sequence，该方法返回的值可能小于{@link WaitStrategy}的实例所提供的的序列号。该方法的一个常见用途是发送超时信号。
	 * 其他 EventProcessor 可以使用 WaitStrategy来获取通知，得到消息可用，并处理这个情况。{@link BatchEventProcessor}显示处理这些消息。如果需要的话可以发出超时信号
	 * @param sequence 被等待的序号
	 * @param cursor	来着缓存的序号，等待/通知策略将需要这个，因为它是唯一的更新时通知的序列。
	 * @param dependentSequence 等待的缓存中的序号
	 * @param barrier	处理器等待的
	 * @return 可靠的序号，这个序号可能会大于我们的请求的序号
	 * @throws AlertException		disruptor状态改变
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier)throws AlertException,InterruptedException,TimeoutException;
	
	/**
	 * 用于告诉{@link EventProcessor}s 等待的游标已就绪
	 */
	void signalAllWhenBlocking();
	
}

package com.sundy.disruptor.core.waitstrategy;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;


/**
 * 自旋锁策略，{@link EventProcessor}s会使 SequenceBarrier轮询等待
 * <p/>
 * 本策略使用消耗cpu资源来避免系统切换调用产生的等待时间。该策略最好的使用环境是当线程绑定到特定到的cpu内核
 * @author Administrator
 *
 */
public final class BusySpinWaitStrategy implements WaitStrategy {

	@Override
	public long waitFor(long sequence, Sequence cursor,
			Sequence dependentSequence, SequenceBarrier barrier)
			throws AlertException, InterruptedException, TimeoutException {
		long availableSequence;
		while ((availableSequence=dependentSequence.get())<sequence) {
			barrier.checkAlert();
		}
		return availableSequence;
	}

	@Override
	public void signalAllWhenBlocking() {
		// TODO Auto-generated method stub

	}

}

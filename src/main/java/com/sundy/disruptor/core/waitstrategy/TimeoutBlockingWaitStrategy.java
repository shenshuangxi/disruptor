package com.sundy.disruptor.core.waitstrategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

public class TimeoutBlockingWaitStrategy implements WaitStrategy {

	private final Lock lock = new ReentrantLock();
	private final Condition processorNotifyCondition = lock.newCondition();
	private final long timeoutInNanos;
	
	public TimeoutBlockingWaitStrategy(final long timeout,TimeUnit timeUnit) {
		this.timeoutInNanos = timeUnit.toNanos(timeout);
	}
	
	@Override
	public long waitFor(long sequence, Sequence cursor,
			Sequence dependentSequence, SequenceBarrier barrier)
			throws AlertException, InterruptedException, TimeoutException {
		long nanos = timeoutInNanos;
		long availableSequence;
		if((availableSequence=cursor.get())<sequence){
			lock.lock();
			try {
				while ((availableSequence = cursor.get()) < sequence) {
					barrier.checkAlert();
					nanos = processorNotifyCondition.awaitNanos(nanos);
					if (nanos <= 0) {
						throw TimeoutException.INSTANCE;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return availableSequence;
	}

	@Override
	public void signalAllWhenBlocking() {
		// TODO Auto-generated method stub

	}

}

package com.sundy.disruptor.core.waitstrategy;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;


public class BlockingWaitStrategy implements WaitStrategy {

	private final Lock lock = new ReentrantLock();
	private final Condition processNotifyCondition = lock.newCondition();
	
	@Override
	public long waitFor(long sequence, Sequence cursor,
			Sequence dependentSequence, SequenceBarrier barrier)
			throws AlertException, InterruptedException, TimeoutException {
		long availableSequence;
		if((availableSequence = cursor.get()) < sequence){
			lock.lock();
			try {
				while ((availableSequence = cursor.get()) < sequence) {
					barrier.checkAlert();
					processNotifyCondition.await();
				}
			} finally {
				lock.unlock();
			}
		}
		while((availableSequence = dependentSequence.get()) < sequence){
			barrier.checkAlert();
		}
		return availableSequence;
	}

	@Override
	public void signalAllWhenBlocking() {
		lock.lock();
		try {
			processNotifyCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}

}

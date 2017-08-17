package com.sundy.disruptor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

public final class BlockingWaitStrategy implements WaitStrategy {

	private final Lock lock = new ReentrantLock();
	private final Condition processorNotifyCondition = lock.newCondition();
	
	public long waitFor(long sequence, Sequence cursor,
			Sequence dependentSequence, SequenceBarrier barrier)
			throws AlertException, InterruptedException, TimeoutException {
		long availableSequence;
		if((availableSequence=cursor.get()) < sequence){
			lock.lock();
			try {
				while((availableSequence=cursor.get()) < sequence){
					barrier.checkAlert();
					processorNotifyCondition.await();
				}
			} finally{
				lock.unlock();
			}
		}
		
		while((availableSequence=dependentSequence.get()) < sequence){
			barrier.checkAlert();
		}
		return availableSequence;
	}

	public void signalAllWhenBlocking() {
		lock.lock();
		try {
			processorNotifyCondition.signalAll();
		} finally{
			lock.unlock();
		}
	}

}

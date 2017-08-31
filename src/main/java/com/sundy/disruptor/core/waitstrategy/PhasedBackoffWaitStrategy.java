package com.sundy.disruptor.core.waitstrategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;


public final class PhasedBackoffWaitStrategy implements WaitStrategy {

	private static final int SPIN_TRIES = 10000;
	private final long spinTimeoutNanos;
	private final long yieldTimeoutNanos;
	private final BlockingStrategy lockingStrategy;
	
	public PhasedBackoffWaitStrategy(long spinTimeoutMillis,
									 long yieldTimeoutMillis,
									 TimeUnit timeUnit,
									 BlockingStrategy lockingStrategy) {
		this.spinTimeoutNanos = timeUnit.toNanos(spinTimeoutMillis);
		this.yieldTimeoutNanos = spinTimeoutNanos + timeUnit.toNanos(yieldTimeoutMillis);
		this.lockingStrategy = lockingStrategy;
	}
	
	public static PhasedBackoffWaitStrategy withLock(long spinTimeoutMillis,long yieldTimeoutMillis,TimeUnit units){
		return new PhasedBackoffWaitStrategy(spinTimeoutMillis, yieldTimeoutMillis, units, new LockBlockingStrategy());
	}
	
	public static PhasedBackoffWaitStrategy withSleep(long spinTimeoutMillis,long yieldTimeoutMillis,TimeUnit units){
		return new PhasedBackoffWaitStrategy(spinTimeoutMillis, yieldTimeoutMillis, units, new SleepBlockingStrategy());
	}
	
	
	@Override
	public long waitFor(long sequence, Sequence cursor,
			Sequence dependentSequence, SequenceBarrier barrier)
			throws AlertException, InterruptedException, TimeoutException {
		long availableSequence;
		long startTime = 0;
		int counter = SPIN_TRIES;
		do {
			if((availableSequence=dependentSequence.get())>=sequence){
				return availableSequence;
			}
			
			if(0 == --counter){
				if(0 == startTime){
					startTime = System.nanoTime();
				} else {
					long timeDelta = System.nanoTime() - startTime;
					if(timeDelta > yieldTimeoutNanos){
						return lockingStrategy.waitOnLock(sequence, cursor, dependentSequence, barrier);
					} else if(timeDelta > spinTimeoutNanos){
						Thread.yield();
					}
				}
				counter = SPIN_TRIES;
			}
			
		} while (true);
	}
	

	@Override
	public void signalAllWhenBlocking() {
		lockingStrategy.signalAllWhenBlocking();
	}
	
	private interface BlockingStrategy {
		
		long waitOnLock(long sequence, 
				Sequence curosrSequence, 
				Sequence dependentSequence, 
				SequenceBarrier barrier) throws AlertException, InterruptedException;
		
		void signalAllWhenBlocking();
		
	}
	
	private static class LockBlockingStrategy implements BlockingStrategy {
		
		private final Lock lock = new ReentrantLock();
		private final Condition processorNotifyCondition = lock.newCondition();
		private volatile int numWaiters = 0;
		
		@Override
		public long waitOnLock(long sequence, 
				Sequence curosrSequence,
				Sequence dependentSequence, 
				SequenceBarrier barrier) throws AlertException, InterruptedException {
			long availableSequence;
			lock.lock();
			try {
				++numWaiters;
				while ((availableSequence = curosrSequence.get()) < sequence) {
					barrier.checkAlert();
					processorNotifyCondition.await(1, TimeUnit.MILLISECONDS);
				}
			} finally {
				-- numWaiters;
				lock.unlock();
			}
			
			while ((availableSequence = dependentSequence.get()) < sequence) {
				barrier.checkAlert();
			}
			
			return availableSequence;
		}
		@Override
		public void signalAllWhenBlocking() {
			if(numWaiters != 0){
				lock.lock();
				try {
					processorNotifyCondition.signalAll();
				} finally {
					lock.unlock();
				}
			}
		}
	}
	
	private static class SleepBlockingStrategy implements BlockingStrategy {

		@Override
		public long waitOnLock(long sequence, Sequence curosrSequence,
				Sequence dependentSequence, SequenceBarrier barrier)
				throws AlertException, InterruptedException {
			long availableSequence;
			while ((availableSequence=curosrSequence.get()) < sequence) {
				LockSupport.parkNanos(1);
			}
			return availableSequence;
		}

		@Override
		public void signalAllWhenBlocking() {
			// TODO Auto-generated method stub
			
		}
		
	}

}

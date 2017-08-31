package com.sundy.disruptor.core.waitstrategy;

import java.util.concurrent.locks.LockSupport;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

/**
 *  这种策略在性能和CPU资源之间是一个很好的妥协。 延迟尖峰可能在安静期后发生。
 * @author Administrator
 *
 */
public class SleepingWaitStrategy implements WaitStrategy {

	private static final int RETRIES = 200;
	
	
	@Override
	public long waitFor(long sequence, Sequence cursor,
			Sequence dependentSequence, SequenceBarrier barrier)
			throws AlertException, InterruptedException, TimeoutException {
		long availableSequence;
		int counter = RETRIES;
		while ((availableSequence=dependentSequence.get()) < sequence) {
			counter = applyWaitMethod(barrier,counter);
		}
		return availableSequence;
	}


	@Override
	public void signalAllWhenBlocking() {
		// TODO Auto-generated method stub

	}
	
	private int applyWaitMethod(SequenceBarrier barrier, int counter) throws AlertException {
		barrier.checkAlert();
		
		if(counter>100){
			-- counter; 
		}else if(counter > 0){
			-- counter;
			Thread.yield();
		}else{
			LockSupport.parkNanos(1L);
		}
		return counter;
	}

}

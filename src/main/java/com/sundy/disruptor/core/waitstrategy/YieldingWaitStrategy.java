package com.sundy.disruptor.core.waitstrategy;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

/**
 * 该类的实例 可以在进行自旋锁等待后，使用 Thread yield来等待
 * <p/>
 * 该行为可以在cpu资源占用和性能上获得平衡。不会引起系统调用产生的开销
 * @author Administrator
 *
 */
public class YieldingWaitStrategy implements WaitStrategy {

	private static final int SPIN_TRIES = 100;
	
	@Override
	public long waitFor(long sequence, Sequence cursor,
			Sequence dependentSequence, SequenceBarrier barrier)
			throws AlertException, InterruptedException, TimeoutException {

		long availableSequence;
		int counter = SPIN_TRIES;
		while((availableSequence=dependentSequence.get())<sequence){
			counter = applyWaitMethod(barrier, counter);
		}
		return availableSequence;
	}


	@Override
	public void signalAllWhenBlocking() {
		// TODO Auto-generated method stub

	}
	
	private int applyWaitMethod(SequenceBarrier barrier, int counter) throws AlertException {
		barrier.checkAlert();
		if(counter==0){
			Thread.yield();
		}else{
			-- counter;
		}
		return counter;
	}

}

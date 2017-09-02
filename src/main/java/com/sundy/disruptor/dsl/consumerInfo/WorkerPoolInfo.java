package com.sundy.disruptor.dsl.consumerInfo;

import java.util.concurrent.Executor;

import com.sundy.disruptor.core.WorkerPool;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

public class WorkerPoolInfo<T> implements ConsumerInfo {

	private final WorkerPool<T> workerPool;
	private final SequenceBarrier sequenceBarrier;
	private boolean endOfChain = true;
	
	public WorkerPoolInfo(WorkerPool<T> workerPool,
			SequenceBarrier sequenceBarrier) {
		this.workerPool = workerPool;
		this.sequenceBarrier = sequenceBarrier;
	}

	@Override
	public Sequence[] getSequences() {
		return workerPool.getWorkerSequences();
	}

	@Override
	public SequenceBarrier getBarrier() {
		return sequenceBarrier;
	}

	@Override
	public boolean isEndOfChain() {
		return endOfChain;
	}

	@Override
	public void start(Executor executor) {
		workerPool.start(executor);
	}

	@Override
	public void halt() {
		workerPool.halt();
	}

	@Override
	public void markAsUsedInBarrier() {
		endOfChain = false;
	}

	@Override
	public boolean isRunning() {
		return workerPool.isRunning();
	}

}

package com.sundy.disruptor.dsl.consumerInfo;

import java.util.concurrent.Executor;

import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

public interface ConsumerInfo {

	Sequence[] getSequences();
	
	SequenceBarrier getBarrier();
	
	boolean isEndOfChain();
	
	void start(Executor executor);
	
	void halt();
	
	void markAsUsedInBarrier();
	
	boolean isRunning();
	
}

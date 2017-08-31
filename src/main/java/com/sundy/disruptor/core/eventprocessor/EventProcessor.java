package com.sundy.disruptor.core.eventprocessor;

import com.sundy.disruptor.core.sequence.Sequence;

public interface EventProcessor extends Runnable{

	Sequence getSequence();
	
	void halt();
	
	boolean isRunning();
	
}

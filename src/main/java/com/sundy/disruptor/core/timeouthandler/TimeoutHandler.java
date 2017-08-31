package com.sundy.disruptor.core.timeouthandler;

public interface TimeoutHandler {

	void onTimeout(long sequence);
	
}

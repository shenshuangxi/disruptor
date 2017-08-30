package com.sundy.disruptor.core.exception;

@SuppressWarnings("serial")
public class TimeoutException extends Exception {

	public static final TimeoutException INSTANCE = new TimeoutException();

    private TimeoutException()
    {
        // Singleton
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        return this;
    }
	
}

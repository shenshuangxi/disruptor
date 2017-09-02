package com.sundy.disruptor.core.exception;

import com.sundy.disruptor.core.RingBuffer;


/**
 * <p>Exception thrown when the it is not possible to insert a value into
 * the ring buffer without it wrapping the consuming sequenes.  Used
 * specifically when claiming with the {@link RingBuffer#tryNext()} call.
 *
 * <p>For efficiency this exception will not have a stack trace.
 * @author mikeb01
 *
 */
@SuppressWarnings("serial")
public final class InsufficientCapacityException extends Exception
{
    public static final InsufficientCapacityException INSTANCE = new InsufficientCapacityException();

    private InsufficientCapacityException()
    {
        // Singleton
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        return this;
    }
}
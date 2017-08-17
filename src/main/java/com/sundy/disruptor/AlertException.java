package com.sundy.disruptor;

/**
 * Used to alert {@link EventProcessor}s waiting at a {@link SequenceBarrier} of status changes.
 * <P>
 * It does not fill in a stack trace for performance reasons.
 */
@SuppressWarnings("serial")
public final class AlertException extends Exception
{
    /** Pre-allocated exception to avoid garbage generation */
    public static final AlertException INSTANCE = new AlertException();

    /**
     * Private constructor so only a single instance exists.
     */
    private AlertException()
    {
    }

    /**
     * Overridden so the stack trace is not filled in for this exception for performance reasons.
     *
     * @return this instance.
     */
    @Override
    public Throwable fillInStackTrace()
    {
        return this;
    }
}

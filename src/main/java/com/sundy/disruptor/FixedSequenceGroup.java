package com.sundy.disruptor;

import java.util.Arrays;

import com.sundy.disruptor.util.Util;

/**
 * 在一个序列之后隐藏一组序列
 * @author Administrator
 *
 */
public final class FixedSequenceGroup extends Sequence {

	private final Sequence[] sequences;
	
	public FixedSequenceGroup(Sequence[] sequences) {
		this.sequences = Arrays.copyOf(sequences, sequences.length);
	}
	
	@Override
	public long get(){
		return Util.getMinimumSequence(sequences);
	}
	
	@Override
	public String toString(){
		return Arrays.toString(sequences);
	}
	
	/**
     * Not supported.
     */
    @Override
    public void set(long value)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public boolean compareAndSet(long expectedValue, long newValue)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public long incrementAndGet()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public long addAndGet(long increment)
    {
        throw new UnsupportedOperationException();
    }
	
}

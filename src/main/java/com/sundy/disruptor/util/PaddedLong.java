package com.sundy.disruptor.util;

/**
 * 用于填充cpu缓存行，不填充的话，可能导致伪共享
 * @author Administrator
 *
 */
public class PaddedLong extends MutableLong {

	public volatile long p1=7l;
	public volatile long p2=7l;
	public volatile long p3=7l;
	public volatile long p4=7l;
	public volatile long p5=7l;
	public volatile long p6=7l;
	
	public PaddedLong() {
		// TODO Auto-generated constructor stub
	}
	
	public PaddedLong(final long initialValue) {
		super(initialValue);
	}
	
	public long sumPaddingToPreventOptimisation(){
		return p1+p2+p3+p4+p5+p6;
	}
	
}

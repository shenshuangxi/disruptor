package com.sundy.disruptor.util;

/**
 * 持有long值的类
 * @author Administrator
 *
 */
public class MutableLong {

	private long value = 0l;
	
	public MutableLong() {
		// TODO Auto-generated constructor stub
	}
	
	public MutableLong(final long initialValue){
		this.value = initialValue;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
	
}

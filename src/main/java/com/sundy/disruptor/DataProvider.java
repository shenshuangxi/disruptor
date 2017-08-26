package com.sundy.disruptor;

public interface DataProvider<T> {

	T get(long sequence);
	
}

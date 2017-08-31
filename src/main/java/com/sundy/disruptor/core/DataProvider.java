package com.sundy.disruptor.core;

public interface DataProvider<T> {

	T get(long sequence);
	
}

package com.sundy.disruptor.core.sequence;

import com.sundy.disruptor.util.Util;

import sun.misc.Unsafe;

/**
 * 并发索引序列类，用于追踪RingBuffer和event processor的进度。
 * 支持多并发操作，包括原子操作(cas)和顺序执行写入(防止重排序)
 * <p/>
 * 通过在volatile字段周围添加padding值来避免伪共享而获取更高的效率
 * @author Administrator
 *
 */
public class Sequence {

	public static final long INITIAL_VALUE = -1L;
	private static final Unsafe UNSAFE;
	private static final long VALUE_OFFSET;
	private final long[] paddedValue = new long[15];
	
	static {
		UNSAFE = Util.getUnsafe();
		final int base = UNSAFE.arrayBaseOffset(long[].class);
		final int scale = UNSAFE.arrayIndexScale(long[].class);
		VALUE_OFFSET = base + (scale * 7);
	}
	
	/**
	 * 创建一个新的所有序列，初始值为-1
	 */
	public Sequence() {
		this(INITIAL_VALUE);
	}
	
	/**
	 * 通过一个初始值来创建一个新的索引序列
	 * @param initialCursorValue
	 */
	public Sequence(long initialCursorValue) {
		UNSAFE.putOrderedLong(paddedValue, VALUE_OFFSET, initialCursorValue);
	}

	/**
	 * 获取索引序列值，获取的值支持volatile语义
	 * @return
	 */
	public long get() {
		return UNSAFE.getLongVolatile(paddedValue, VALUE_OFFSET);
	}

	/**
	 * 顺序写入索引序列值，该方法有一个Store/Store 栅栏 在写和存储之间  可以提高些的速度
	 * @param cursorSequence
	 */
	public void set(final long cursorSequence) {
		UNSAFE.putOrderedLong(paddedValue, VALUE_OFFSET, cursorSequence);
	}
	
	/**
	 * 该方法用于volatile 写入一个序列值，该方法在写入之前有个Store/Store的栅栏 在写和读之间有一个 Store/Load 栅栏
	 * @param value
	 */
	public void setVolatile(final long value){
		UNSAFE.putLongVolatile(paddedValue, VALUE_OFFSET, value);
	}
	
	/**
	 * 先比较再写入的操作，用于原子曹做
	 * @param expectedValue
	 * @param newValue
	 * @return
	 */
	public boolean compareAndSet(final long expectedValue, final long newValue){
		return UNSAFE.compareAndSwapLong(paddedValue, VALUE_OFFSET, expectedValue, newValue);
	}
	
	/**
	 * 索引序列值 原子加1
	 * @return
	 */
	public long incrementAndGet(){
		return addAndGet(1L);
	}

	/**
	 * 索引序号值 原子性的加上所给的值
	 * @param increment
	 * @return
	 */
	public long addAndGet(final long increment) {
		long currentValue;
		long newValue;
		do {
			currentValue = get();
			newValue = currentValue + increment;
		} while (!compareAndSet(currentValue, newValue));
		
		return newValue;
	}
	
	public String toString()
    {
        return Long.toString(get());
    }
	
	

}

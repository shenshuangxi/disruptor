package com.sundy.disruptor;


import com.sundy.disruptor.util.Util;

import sun.misc.Unsafe;

/**
 * 并发序列类用于跟踪环形缓冲区和事件处理器的进度。 支持多个并发操作，包括CAS和订单写入。
 * <p/>
 * 还尝试通过在易失性字段周围添加填充来更有效地进行虚假共享。
 * @author Administrator
 *
 */
public class Sequence {

	static final long INITIAL_VALUE = -1L;
	
	/**
	 * java中的原子操作
	 */
	private static final Unsafe UNSAFE;
	
	/**
	 * 数组中array[7]元素的偏移量
	 */
	private static final long VALUE_OFFSET;
	
	static{
		UNSAFE = Util.getUnsafe();								//获取java中原子操作的对象
		final int base = UNSAFE.arrayBaseOffset(long[].class); //获取数组头的长度
		final int scale = UNSAFE.arrayIndexScale(long[].class); //获取数组中每个元素的大小
		VALUE_OFFSET = base + (scale*7);						//获取数组中array[7]元素的偏移量
	}
	
	private final long[] paddedValue = new long[15];
	
	/**
	 * 根据初始化值-1创建一个Sequence实例
	 */
	public Sequence() {
		this(INITIAL_VALUE);
	}
	
	/**
	 * 根据传入的初始化值创建一个Sequence实例
	 * @param initialValue
	 */
	public Sequence(final long initialValue) {
		UNSAFE.putOrderedLong(paddedValue, VALUE_OFFSET, initialValue);
	}
	
	/**
	 * 获取当前sequence的值
	 * @return
	 */
	public long get(){
		return UNSAFE.getLongVolatile(paddedValue, VALUE_OFFSET);
	}
	
	/**
	 * 写入一个序号值,该写入值在写入完毕后别的线程不一定会立即发现，除非该值是被Volatile修饰
	 * @param value
	 */
	public void set(final long value){
		UNSAFE.putOrderedLong(paddedValue, VALUE_OFFSET, value);
	}
	
	/**
	 * 写入一个序号值,支持Volatile语义
	 * @param value
	 */
	public void setVolatile(final long value){
		UNSAFE.putLongVolatile(paddedValue, VALUE_OFFSET, value);
	}
	
	/**
	 * 修改期望的值为给定值
	 * @param expectedValue sequence中的期望值
	 * @param newValue		sequence中修改值
	 * @return	修改是否成功
	 */
	public boolean compareAndSet(final long expectedValue, final long newValue){
		return UNSAFE.compareAndSwapLong(paddedValue, VALUE_OFFSET, expectedValue, newValue);
	}
	
	/**
	 * 将sequence的值原子添加1
	 * @return
	 */
	public long incrementAndGet(){
		return addAndGet(1L);
	}
	
	/**
	 * 向当前Sequence原子性的添加给定值
	 * @param increment
	 * @return
	 */
	public long addAndGet(final long increment){
		long currentValue;
		long newValue;
		do {
			currentValue = get();
			newValue = currentValue + increment;
		} while (!compareAndSet(currentValue, newValue));
		return newValue;
	}
	
	public String toString(){
        return Long.toString(get());
    }
	
}

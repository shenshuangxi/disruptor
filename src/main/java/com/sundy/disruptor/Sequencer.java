package com.sundy.disruptor;

/**
 * 协调获取可访问数据结构的序号同时跟踪相关序号
 * @author Administrator
 *
 */
public interface Sequencer extends Cursored {

	/**
	 * 设置sequencer的初始指针的值
	 */
	long INITIAL_CURSOR_VALUE = -1;
	
	/**
	 * 当前数据结构所容纳的实体数
	 * @return
	 */
	int getBufferSize();
	
	/**
	 * 用于判断当前缓存中是否能分配指定大小的内存空间
	 * @param requiredCapacity
	 * @return
	 */
	boolean hasAvailableCapacity(final int requiredCapacity);
	
	/**
	 * 声明获取下一个要发布事件的序号
	 * @return
	 */
	long next();
	
	/**
	 * 声明获取n个事件的序号
	 * 获取的事件第一个为序号为 
	 * start = sequencer(n)-(n-1)
	 * end = sequencer(n)
	 * @param n 需要的序号数
	 * @return 最大的那个序号值
	 */
	long next(int n);
	
	/**
	 * 试图声明获取下一个事件的序号用于发布。如果只要还有可用大小的空间，那么就会返回序号
	 * @return
	 * @throws InsufficientCapacityException
	 */
	long tryNext() throws InsufficientCapacityException;
	
	/**
	 * 试图声明获取下一个事件的序号用于发布。如果只要还有可用大小的空间，那么就会返回序号
	 * 类似于{@link #next(int)}
	 * @return
	 * @throws InsufficientCapacityException
	 */
	long tryNext(int n) throws InsufficientCapacityException;
	
	/**
	 * 返回序列号发生器所剩余空间大小
	 * @return
	 */
	long remainingCapactiy();
	
	/**
	 * 声明一个具体的序号，仅仅只在初始化ring buffer为初始值时使用
	 * @param sequence
	 */
	void clain(long sequence);
	
	/**
	 * 当事件已填充调用该方法发布一个序号，
	 * @param sequence
	 */
	void publish(long sequence);
	
	/**
	 * 当所有事件已填充调用该方法批量发布序号
	 * @param lo 用于发布的最小的序号
	 * @param hi 用于发布的最大序号
	 */
	void publish(long lo, long hi);
	
	/**
	 * 确认序号已发布，且事件时可用，非阻塞调用
	 * @param sequence
	 * @return 
	 */
	boolean isAvailable(long sequence);
	
	/**
	 * 向当前Disruptor实例添加一个具体的门序号(gating sequence),这个序号会安全并自动的添加到门序号列表(gating sequence list)
	 * @param gatingSequences 需要添加的门序号
	 */
	void addGatingSequences(Sequence... gatingSequences);
	
	/**
	 * 从当前Disruptor实例移除一个具体的门序号(gating sequence)
	 * @param sequence
	 * @return
	 */
	boolean removeGatingSequence(Sequence sequence);
	
	/**
	 * 根据传入的门序号参数创建序号屏障(SequenceBarrier),该序号屏障用于给EventProcessor来获取缓存中哪些可用消息是可以读取的
	 * @see SequenceBarrier 
	 * @param sequencesToTrack 哪些序号是可用的
	 * @return	返回序号屏障
	 */
	SequenceBarrier newBarrier(Sequence... sequencesToTrack);
	
	/**
	 * 从门序号(gating sequence)中获取序号最小的值，然后添加到缓存中(ring buffer)
	 * @return 返回序号最小的门序号，或者如果没有序号被添加，那么会获取当前指针所在的序号值
	 */
	long getMinimumSequence();
	
	/**
	 * 获取最大的已发布的序号
	 * @param sequecne
	 * @param avaiableSequence
	 * @return
	 */
	long getHighestPublishedSequence(long sequence, long availableSequence);
	
	
}

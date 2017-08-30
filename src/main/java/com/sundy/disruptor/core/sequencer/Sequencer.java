package com.sundy.disruptor.core.sequencer;

import com.sundy.disruptor.InsufficientCapacityException;
import com.sundy.disruptor.core.Cursored;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;

/**
 * 协调索引序列用于访问数据结构，同时跟踪这些索引序列
 * @author Administrator
 *
 */
public interface Sequencer extends Cursored {

	/**
	 * 初始序号
	 */
	long INITIAL_CURSOR_VALUE = -1L;
	
	/**
	 * 数据结构能容纳的实体数
	 * @return RingBuffer的大小
	 */
	int getBufferSize();
	
	/**
	 * 判断缓冲区有能力分配另一个序列。 这是一种并发方法，因此响应只能作为可用容量的指示
	 * @param requiredCapacity 需要分配的内存数
	 * @return	如果缓冲区具有分配下一个序列的能力，则为true，否则为false。
	 */
	boolean hasAvailableCapacity(final int requiredCapacity);
	
	/**
	 * 索取一个序号用于发布下一事件
	 * @return
	 */
	long next();
	
	/**
	 * 索取多个序号用于事件发布，该方法用于产生批量事件。
	 * <pre>
	 * int n = 10;
	 * long hi = sequencer.next(n);
	 * long lo = hi - (n -1);
	 * for(long sequence=lo;sequence<=hi;sequence++){
	 * 	//dowork
	 * }
	 * sequencer.publish(lo,hi);
	 * </pre>
	 * @param n 需要的的索引序列个数
	 * @return 返回需要索引个数的最大的序列值
	 */
	long next(int n);
	
	/**
	 * 尝试获取一个序号用于发布下一个事件，如果有可用的缓存，那么返回这个缓存所在的序号
	 * @return 获取到的索引序号值
	 * @throws InsufficientCapacityException 
	 */
	long tryNext() throws InsufficientCapacityException;
	
	/**
	 * 尝试获取多个事件的序号用于发布。如果缓存容量足够，那么返回缓存编号最大的那个。该方法获取到的序号使用可以参考{@link #next(int)}
	 * @param n 需要获取的索引个数
	 * @return 返回获取到的缓存序号的最大值
	 * @throws InsufficientCapacityException 
	 */
	long tryNext(int n) throws InsufficientCapacityException;
	
	/**
	 * 通过该方法获取本序号器所在缓存的剩余容量
	 * @return 剩余多少容量可用
	 */
	long remainingCapacity();
	
	/**
	 *声明一个具体的序号，该方法声明的序号值仅用于初始化RingBuffer
	 */
	void claim(long sequence);
	
	/**
	 * 发布一个序号，仅当该序号所在缓存被可用事件填充
	 * @param sequence
	 */
	void publish(long sequence);
	
	/**
	 * 批量发布序号，仅当这些序号锁代表的缓存被可用事件填充
	 * @param lo 所要发布的第一个序号
	 * @param hi 所要发布的最后一个序号
	 */
	void publish(long lo, long hi);
	
	/**
	 * 用于确认该序号已经发布，且事件是可用的。非阻塞
	 * @param sequence
	 * @return
	 */
	boolean isAvailable(long sequence);
	
	/**
	 * 往Disroptor实例中添加具体的门索引序号。这些序号会被安全和原子性的添加到门索引序号列表中
	 * @param gatingSequences
	 */
	void addGatingSequences(Sequence... gatingSequences);
	
	/**
	 * 从当前sequencer的实例中移除具体的某个门索引序号。
	 * @param gatingSequence
	 * @return
	 */
	boolean removeGatingSequence(Sequence gatingSequence);
	
	/**
	 * 创建一个新的索引序号栅栏(SequenceBarrier)实例。该实例用于帮助  EventProcessor 跟踪 RingBuffer所给的索引序号表中哪些序号所在的消息是可用的
	 * @param sequencesToTrack
	 * @return
	 */
	SequenceBarrier newBarrier(Sequence... sequencesToTrack);
	
	/**
	 * 从添加到本RingBuffer中的所有门索引序号中，获取值最小的那个
	 * @return 返回最小的门索引序号，如果还没有序号被添加，那么返回游标所在的序号
	 */
	long getMinimumSequence();
	
	/**
	 * 获取已发布的最大的索引序号值
	 * @param sequence
	 * @param availableSequence
	 * @return
	 */
	long getHighestPublishedSequence(long sequence, long availableSequence);

}

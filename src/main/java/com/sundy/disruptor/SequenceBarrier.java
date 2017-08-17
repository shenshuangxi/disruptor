package com.sundy.disruptor;

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

/**
 * 用于跟踪用于发布者的游标和用于处理数据结构的从属EventProcessors序列的协调障碍
 * @author Administrator
 *
 */
public interface SequenceBarrier {

	/**
	 * 等待可用序号给予消费端
	 * @param sequence 等待的序号
	 * @return
	 * @throws AlertException 当disruptor发生状态改变
	 * @throws InterruptedException 条件满足唤醒线程
	 * @throws TimeoutException 等待超时
	 */
	long waitFor(long sequence)throws AlertException,InterruptedException,TimeoutException;
	
	/**
	 * 获取当前可读的游标值
	 * @return 返回已发布实体的游标值
	 */
	long getCursor();
	
	/**
	 * 屏障的当前警报状态
	 * @return 返回true 报警状态
	 */
	boolean isAlerted();
	
	/**
	 * 警告{@link EventProcessor}状态已经改变，并保持这个状态直到清除
	 */
	void alert();
	
	/**
	 * 清除当前的警报状态
	 */
	void clearAlert();
	
	/**
	 * 检查当前是否已有警报，如果有的话，抛出 AlertException 异常
	 * @throws AlertException
	 */
	void checkAlert() throws AlertException;
	
	
	
	
	
}

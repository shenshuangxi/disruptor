package com.sundy.disruptor.core.sequencebarrier;

import com.sundy.disruptor.core.exception.AlertException;
import com.sundy.disruptor.core.exception.TimeoutException;

/**
 * 协作栅栏，用于追踪游标所指的已发布的索引序号，该索引序号用于给{@link EventProcessor}s 处理所需的数据所在的地址
 * @author Administrator
 *
 */
public interface SequenceBarrier {

	/**
	 * 等待给定的序号可用
	 * @param sequence 等待的索引序号
	 * @return 返回可用的序号值
	 * @throws AlertException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	long waitFor(long sequence) throws AlertException,InterruptedException,TimeoutException;
	
	/**
	 * 获取当前游标所在的索引序号
	 * @return
	 */
	long getCursor();
	
	/**
	 * 栅栏的当前的警报状态
	 * @return 为true表示当前处于报警状态
	 */
	boolean isAlerted();
	
	/**
	 * 提醒 {@link EventProcessor}s 状态改变，并保持该状态直到状态被清除
	 */
	void alert();
	
	/**
	 * 清除警报状态
	 */
	void clearAlert();
	
	/**
	 * 检测警报状态，如果警报触发则抛出 异常
	 * @throws AlertException
	 */
	void checkAlert() throws AlertException;
	
}

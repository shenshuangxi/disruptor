package com.sundy.disruptor.core;

/**
 * 本接口实例必须提供一个代表当前游标值的long类型的值。
 * 一般用于动态的添加/删除 序号 到{@link SequenceGroups#addSequences(Object, java.util.concurrent.atomic.AtomicReferenceFieldUpdater, Cursored, Sequence...)}.
 * @author Administrator
 *
 */
public interface Cursored {

	/**
	 * 获取当前游标值
	 * @return
	 */
	long getCursor();
	
}

package com.sundy.disruptor;

/**
 * 该接口的实例必须提供一个single long类型的值来代表当前指针的值
 * 用于动态添加/删除序号 {@link SequenceGroups#addSequences(Object, java.util.concurrent.atomic.AtomicReferenceFieldUpdater, Cursored, Sequence...)}.
 * @author Administrator
 *
 */
public interface Cursored {

	/**
	 * 获取当前指针值
	 * @return
	 */
	long getCursor();
	
}

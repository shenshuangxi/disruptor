package com.sundy.disruptor.core.sequence;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.sundy.disruptor.core.Cursored;
import com.sundy.disruptor.core.SequenceGroups;
import com.sundy.disruptor.util.Util;


/**
 * 该类实例可以线程安全的 动态从 {@link Sequence} group中 添加和删除 {@link Sequence}s 
 * <p/>
 * 
 *  {@link #get()} 和 {@link #set(long)} 是无锁的，   
 *  可以同时使用 {@link #add(Sequence)} 和 {@link #remove(Sequence)} 
 * @author Administrator
 *
 */
public final class SequenceGroup extends Sequence {

	private static final AtomicReferenceFieldUpdater<SequenceGroup, Sequence[]> SEQUENCE_UPDATE = 
			AtomicReferenceFieldUpdater.newUpdater(SequenceGroup.class, Sequence[].class, "sequences");
	private volatile Sequence[] sequences;
	
	public SequenceGroup() {
		super(-1);
	}
	
	@Override
	public long get() {
		return Util.getMinimumSequence(sequences);
	}
	
	@Override
	public void set(final long cursorSequence) {
		final Sequence[] sequences = this.sequences;
		for(Sequence sequence : sequences){
			sequence.set(cursorSequence);
		}
	}
	
	/**
	 * 添加{@link Sequence} 到本聚合中，该方法一般在初始化时使用，
	 * 运行时使用 {@link SequenceGroup#addWhileRunning(Cursored, Sequence)}
	 * @param sequence
	 */
	public void add(final Sequence sequence){
		Sequence[] oldSequence;
		Sequence[] newSequence;
		do {
			oldSequence = sequences;
			final int oldLength = oldSequence.length;
			newSequence = new Sequence[oldLength + 1];
			System.arraycopy(oldSequence, 0, newSequence, 0, oldLength);
			newSequence[oldLength] = sequence;
		} while (SEQUENCE_UPDATE.compareAndSet(this, oldSequence, newSequence));
	}
	
	/**
	 * 移除集合中跟传入参数一样的{@link Sequence}
	 * @param sequence
	 * @return
	 */
	public boolean remove(final Sequence sequence){
		return SequenceGroups.removeSequence(this, SEQUENCE_UPDATE, sequence);
	}
	
	/**
	 * 获取该集合中序列的大小
	 * @return
	 */
	public int size(){
		return sequences.length;
	}
	
	/**
	 * 在线程开始发布到Disruptor之后，向序列组添加一个序列。 它将在添加它们之后将序列设置为ringBuffer的游标值。 这样可以防止任何令人讨厌的倒带/缠绕效果。
	 * @param cursored
	 * @param sequence
	 */
	public void addWhileRunning(Cursored cursored, Sequence sequence){
		SequenceGroups.addSequences(this, SEQUENCE_UPDATE, cursored, sequence);
	}
	
	
	
	
	
}

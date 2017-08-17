package com.sundy.disruptor;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.sundy.disruptor.util.Util;


/**
 * 各种sequencer实现类(single/multi)的基础类，提供共有功能比如门序列(gating sequence)的管理(add/remove)
 * 和 当前游标的所有权
 * @author Administrator
 *
 */
public abstract class AbstractSequencer implements Sequencer {

	/**
	 * 可用于对指定类的指定的volatile引用字段进行原子更新原子,
	 * 如下所示用于更新当前sequencer中字段类型为Sequence[]，字段名称为gatingSequences的值
	 */
	private static final AtomicReferenceFieldUpdater<AbstractSequencer, Sequence[]> SEQUENCE_UPDATER = 
			AtomicReferenceFieldUpdater.newUpdater(AbstractSequencer.class, Sequence[].class, "gatingSequences");

	protected final int bufferSize;
	protected final WaitStrategy waitStrategy;
	protected final Sequence cursor = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
	protected volatile Sequence[] gatingSequences = new Sequence[0];
	
	
	/**
	 * 用给定参数创建Sequencer实例，
	 * @param bufferSize 缓存大小
	 * @param waitStrategy	获取sequence策略
	 */
	public AbstractSequencer(int bufferSize, WaitStrategy waitStrategy){
		if(bufferSize < 1){
			throw new IllegalArgumentException("bufferSize must not be less than 1");
		}
		if (Integer.bitCount(bufferSize)!=1){
			throw new IllegalArgumentException("bufferSize must be a power of 2");
		}
		this.bufferSize = bufferSize;
		this.waitStrategy = waitStrategy;
	}


	public long getCursor() {
		return this.cursor.get();
	}


	public int getBufferSize() {
		return this.bufferSize;
	}


	public void addGatingSequences(Sequence... gatingSequences) {
		SequenceGroups.addSequences(this, SEQUENCE_UPDATER, this, gatingSequences);
	}


	public boolean removeGatingSequence(Sequence sequence) {
		return SequenceGroups.removeSequence(this, SEQUENCE_UPDATER, sequence);
	}


	public SequenceBarrier newBarrier(Sequence... sequencesToTrack) {
		return new ProcessingSequenceBarrier(this, waitStrategy, cursor, sequencesToTrack);
	}


	public long getMinimumSequence() {
		return Util.getMinimumSequence(gatingSequences, cursor.get());
	}
	
	
	
	
	
	
	
	
	
}

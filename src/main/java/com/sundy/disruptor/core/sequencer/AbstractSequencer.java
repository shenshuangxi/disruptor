package com.sundy.disruptor.core.sequencer;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.sundy.disruptor.core.SequenceGroups;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.ProcessingSequenceBarrier;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;
import com.sundy.disruptor.core.waitstrategy.WaitStrategy;
import com.sundy.disruptor.util.Util;

public abstract class AbstractSequencer implements Sequencer {

	private static final AtomicReferenceFieldUpdater<AbstractSequencer, Sequence[]> SEQUENCE_UPDATE = 
			AtomicReferenceFieldUpdater.newUpdater(AbstractSequencer.class, Sequence[].class, "gatingSequences");
	
	protected final int bufferSize;
	protected final WaitStrategy waitStrategy;
	protected final Sequence cursor = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
	protected volatile Sequence[] gatingSequences = new Sequence[0];
	
	/**
	 * 根据具体的缓存大小和等待策略创建Sequencer的实例
	 * @param bufferSize 缓存大小，该大小必须为2的指数
	 * @param waitStrategy 
	 */
	public AbstractSequencer(int bufferSize, WaitStrategy waitStrategy){
		if(bufferSize<1){
			throw new IllegalArgumentException("bufferSize must not be than 1");
		}
		if(Integer.bitCount(bufferSize)!=1){
			throw new IllegalArgumentException("bufferSize must not be a power of 2");
		}
		this.bufferSize = bufferSize;
		this.waitStrategy = waitStrategy;
	}
	
	@Override
	public final long getCursor(){
		return cursor.get();
	}
	
	@Override
	public final int getBufferSize(){
		return bufferSize;
	}
	
	@Override
	public final void addGatingSequences(Sequence... gatingSequences){
		SequenceGroups.addSequences(this, SEQUENCE_UPDATE, this, gatingSequences);
	}
	
	@Override
	public boolean removeGatingSequence(Sequence gatingSequence) {
		return SequenceGroups.removeSequence(this, SEQUENCE_UPDATE, gatingSequence);
	}
	
	@Override
	public long getMinimumSequence() {
		return Util.getMinimumSequence(gatingSequences, cursor.get());
	}
	
	@Override
	public SequenceBarrier newBarrier(Sequence... sequencesToTrack) {
		return new ProcessingSequenceBarrier(this, waitStrategy, cursor, sequencesToTrack);
	}

}

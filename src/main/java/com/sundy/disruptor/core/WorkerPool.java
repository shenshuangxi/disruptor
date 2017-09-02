package com.sundy.disruptor.core;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sundy.disruptor.core.eventFactory.EventFactory;
import com.sundy.disruptor.core.eventprocessor.WorkProcessor;
import com.sundy.disruptor.core.exceptionhandler.ExceptionHandler;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;
import com.sundy.disruptor.core.sequencer.Sequencer;
import com.sundy.disruptor.core.waitstrategy.BlockingWaitStrategy;
import com.sundy.disruptor.core.workhandler.WorkHandler;
import com.sundy.disruptor.util.Util;

public final class WorkerPool<T> {

	private final AtomicBoolean start = new AtomicBoolean(false);
	private final Sequence workSequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
	private final RingBuffer<T> ringBuffer;
	private final WorkProcessor<?>[] workProcessors;
	
	/**
	 * 创建一个工作池，填充一批 {@link WorkHandler} 用于消费已发布的事件
	 * 
	 * 该方法需要提前配置好 {@link RingBuffer}，该ringbuffer必须要要在{@link WorkerPool#start(Executor)}调用前调用{@link RingBuffer#addGatingSequences(Sequence...)}
	 * @param ringBuffer 存储需要被消费的事件
	 * @param sequenceBarrier workhandler 依赖的用于获取事件索引序号的栅栏
	 * @param exceptionHandler	{@link WorkHandler}s 消费事件发生异常时，依赖的组件
	 * @param workHandlers	用于处理事件
	 */
	public WorkerPool(
			final RingBuffer<T> ringBuffer,
			final SequenceBarrier sequenceBarrier,
			final ExceptionHandler exceptionHandler,
			final WorkHandler<T>... workHandlers) {
		this.ringBuffer = ringBuffer;
		final int numWorkers = workHandlers.length;
		workProcessors = new WorkProcessor[numWorkers];
		for(int i=0;i<numWorkers;i++){
			workProcessors[i] = new WorkProcessor<T>(ringBuffer, sequenceBarrier, workHandlers[i], exceptionHandler, workSequence);
		}
	}
	
	/**
	 * 创建一个工作池，填充一批 {@link WorkHandler} 用于消费已发布的事件
	 * 
	 * 该方法需要提前配置好 {@link RingBuffer}，该ringbuffer必须要要在{@link WorkerPool#start(Executor)}调用前调用{@link RingBuffer#addGatingSequences(Sequence...)}
	 * @param eventFactory 用于产生事件填充{@link RingBuffer}
	 * @param exceptionHandler {@link WorkHandler}s 消费事件发生异常时，依赖的组件
	 * @param workHandlers	用于处理事件
	 */
	public WorkerPool(
			final EventFactory<T> eventFactory,
			final ExceptionHandler exceptionHandler,
			final WorkHandler<T>... workHandlers) {
		this.ringBuffer = RingBuffer.createMultiProducer(eventFactory,1024,new BlockingWaitStrategy());
		final SequenceBarrier sequenceBarrier = this.ringBuffer.newBarrier();
		final int numWorkers = workHandlers.length;
		workProcessors = new WorkProcessor[numWorkers];
		for(int i=0;i<numWorkers;i++){
			workProcessors[i] = new WorkProcessor<T>(ringBuffer, sequenceBarrier, workHandlers[i], exceptionHandler, workSequence);
		}
	}
	
	/**
	 * 获取该工作池中每一个{@link WorkHandler}所有的{@link Sequence} 以及该工作池的索引序号
	 * @return
	 */
	public Sequence[] getWorkerSequences(){
		final Sequence[] sequences = new Sequence[workProcessors.length+1];
		for(int i=0,size=workProcessors.length; i<size; i++){
			sequences[i] = workProcessors[i].getSequence();
		}
		sequences[sequences.length-1] = workSequence;
		return sequences;
	}
	
	
	/**
	 * 启动工作池，用于处理索引序号标记的事件
	 * @param executor  用于启动工作线程处理事件
	 * @return
	 */
	public RingBuffer<T> start(final Executor executor){
		if(!start.compareAndSet(false, true)){
			throw new IllegalStateException("WorkerPool has already been started and cannot be restarted until halted.");
		}
		final long cursor = this.ringBuffer.getCursor();
		workSequence.set(cursor);
		for(WorkProcessor workProcessor : workProcessors){
			workProcessor.getSequence().set(cursor);
			executor.execute(workProcessor);
		}
		return this.ringBuffer;
	}
	
	/**
	 * 停止所有的已经到他的生命末尾的工作线程
	 */
	public void drainAndHalt(){
		Sequence[] workerSequences = getWorkerSequences();
		while(ringBuffer.getCursor() > Util.getMinimumSequence(workerSequences)){
			Thread.yield();
		}
		for(WorkProcessor<?> processor : workProcessors){
			processor.halt();
		}
		start.set(false);
	}
	
	/**
	 * 立即停止所有事件处理，不管有没有未处理的事件
	 */
	public void halt(){
		for(WorkProcessor<?> processor : workProcessors){
			processor.halt();
		}
		start.set(false);
	}
	
	public boolean isRunning(){
		return start.get();
	}
	
	
	
	
	
}

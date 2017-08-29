package com.sundy.disruptor;

import com.sundy.disruptor.dsl.ProducerType;

public final class RingBuffer<E> implements Cursored, DataProvider<E>{

	public static final long INITIAL_CURSOR_VALUE = Sequence.INITIAL_VALUE;
	
	private final int indexMask;
	private final Object[] entries;
	private final int bufferSize;
	private final Sequencer sequencer;
	
	/**
	 * 构建一个ringbuffer 使用eventfactory产生的实例来填充整个空格
	 * @param eventFactory 用于生成事件数据填充ringbuffer
	 * @param sequencer	用于通过ringbuffer来处理事件的顺序移动的序号器
	 */
	RingBuffer(EventFactory<E> eventFactory, Sequencer sequencer) {
		this.sequencer = sequencer;
		this.bufferSize = sequencer.getBufferSize();
		if (bufferSize < 1)
        {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1)
        {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        this.indexMask = bufferSize-1;
        this.entries = new Object[this.bufferSize];
        fill(eventFactory);
	}
	
	public static <E> RingBuffer<E> createMultiProducer(EventFactory<E> eventFactory,int bufferSize, WaitStrategy waitStrategy){
		MultiProducerSequencer sequencer = new MultiProducerSequencer(bufferSize, waitStrategy);
		return new RingBuffer<E>(eventFactory, sequencer);
	}
	
	public static <E> RingBuffer<E> createMultiProducer(EventFactory<E> eventFactory, int bufferSize){
		return createMultiProducer(eventFactory, bufferSize, new BlockingWaitStrategy());
	}
	
	public static <E> RingBuffer<E> createSingleProducer(EventFactory<E> eventFactory, int bufferSize, WaitStrategy waitStrategy){
		SingleProceducerSequencer sequencer = new SingleProceducerSequencer(bufferSize, waitStrategy);
		return new RingBuffer<E>(eventFactory, sequencer);
	}
	
	public static <E> RingBuffer<E> createSingleProducer(EventFactory<E> eventFactory, int bufferSize){
		SingleProceducerSequencer sequencer = new SingleProceducerSequencer(bufferSize, new BlockingWaitStrategy());
		return new RingBuffer<E>(eventFactory, sequencer);
	}
	
	public static <E> RingBuffer<E> create(ProducerType producerType, EventFactory<E> eventFactory, int bufferSize, WaitStrategy waitStrategy){
		switch (producerType) {
		case SINGLE:
			return createSingleProducer(eventFactory, bufferSize, waitStrategy);
		case MULTI:
			return createMultiProducer(eventFactory, bufferSize, waitStrategy);
		default:
			throw new IllegalStateException(producerType.toString());
		}
	}
	
	private void fill(EventFactory<E> eventFactory) {
		for(int i=0;i<entries.length;i++){
			entries[i] = eventFactory.newInstance();
		}
		
	}

	@SuppressWarnings("unchecked")
	public E get(long sequence) {
		return (E) entries[(int)sequence&indexMask];
	}
	
	
	public long next(){
		return sequencer.next();
	}
	
	public long next(int n){
		return sequencer.next(n);
	}
	
	public long tryNext() throws InsufficientCapacityException{
		return sequencer.tryNext();
	}
	
	public long tryNext(int n) throws InsufficientCapacityException{
		return sequencer.tryNext(n);
	}
	
	public void resetTo(long sequence){
		sequencer.clain(sequence);
		sequencer.publish(sequence);
	}

	public long getCursor() {
		// TODO Auto-generated method stub
		return 0;
	}

}

package com.sundy.disruptor;

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
		SingleProcedureSequencer sequencer = new SingleProceducerSequencer(bufferSize, waitStrategy);
		return new RingBuffer<E>(eventFactory, sequencer)
	}
	
	
	private void fill(EventFactory<E> eventFactory) {
		for(int i=0;i<entries.length;i++){
			entries[i] = eventFactory.newInstance();
		}
		
	}

	public E get(long sequence) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getCursor() {
		// TODO Auto-generated method stub
		return 0;
	}

}

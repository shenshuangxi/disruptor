package com.sundy.disruptor.core;

import com.sundy.disruptor.core.eventFactory.EventFactory;
import com.sundy.disruptor.core.eventtranslator.EventTranslator;
import com.sundy.disruptor.core.eventtranslator.EventTranslatorOneArg;
import com.sundy.disruptor.core.eventtranslator.EventTranslatorThreeArg;
import com.sundy.disruptor.core.eventtranslator.EventTranslatorTwoArg;
import com.sundy.disruptor.core.eventtranslator.EventTranslatorVararg;
import com.sundy.disruptor.core.exception.InsufficientCapacityException;
import com.sundy.disruptor.core.sequence.Sequence;
import com.sundy.disruptor.core.sequencebarrier.SequenceBarrier;
import com.sundy.disruptor.core.sequencer.MultiProducerSequencer;
import com.sundy.disruptor.core.sequencer.Sequencer;
import com.sundy.disruptor.core.sequencer.SingleProducerSequencer;
import com.sundy.disruptor.core.waitstrategy.BlockingWaitStrategy;
import com.sundy.disruptor.core.waitstrategy.WaitStrategy;
import com.sundy.disruptor.dsl.ProducerType;

public class RingBuffer<E> implements Cursored, DataProvider<E> {

	public static final long INITIAL_CURSOR_VALUE = Sequence.INITIAL_VALUE;
	
	private final int indexMask;
	private final Object[] entries;
	private final int bufferSize;
	private final Sequencer sequencer;
	
	public RingBuffer(EventFactory<E> eventFactory, Sequencer sequencer) {
		this.sequencer = sequencer;
		this.bufferSize = sequencer.getBufferSize();
		if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        this.indexMask = this.bufferSize - 1;
        this.entries = new Object[sequencer.getBufferSize()];
        fill(eventFactory);
	}
	
	public static <E> RingBuffer<E> createMultiProducer(EventFactory<E> eventFactory,int bufferSize, WaitStrategy waitStrategy){
		MultiProducerSequencer multiProducerSequencer = new MultiProducerSequencer(bufferSize, waitStrategy);
		return new RingBuffer<E>(eventFactory, multiProducerSequencer);
	}
	
	public static <E> RingBuffer<E> createMultiProducer(EventFactory<E> eventFactory,int bufferSize){
		return createMultiProducer(eventFactory, bufferSize, new BlockingWaitStrategy());
	}
	
	public static <E> RingBuffer<E> createSingleProducer(EventFactory<E> eventFactory,int bufferSize, WaitStrategy waitStrategy){
		SingleProducerSequencer singleProducerSequencer = new SingleProducerSequencer(bufferSize, waitStrategy);
		return new RingBuffer<E>(eventFactory, singleProducerSequencer);
	}
	
	public static <E> RingBuffer<E> createSingleProducer(EventFactory<E> eventFactory,int bufferSize){
		return createSingleProducer(eventFactory, bufferSize, new BlockingWaitStrategy());
	}
	
	public static <E> RingBuffer<E> create(ProducerType producerType,
			EventFactory<E> eventFactory,
			int bufferSize,
			WaitStrategy waitStrategy){
		switch (producerType) {
		
		case SINGLE:
			return createSingleProducer(eventFactory, bufferSize, waitStrategy);
		
		case MULTI:
			return createMultiProducer(eventFactory, bufferSize,waitStrategy);

		default:
			throw new IllegalStateException(producerType.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	public E get() {
		return (E) entries[(int) sequencer.getCursor()];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(long sequence) {
		return(E) entries[((int)sequence) & indexMask];
	}
	
	public long next(){
		return sequencer.next();
	}
	
	public long next(int n){
		return sequencer.next(n);
	}
	
	public long tryNext() throws InsufficientCapacityException {
        return sequencer.tryNext();
    }
	
	public long tryNext(int n) throws InsufficientCapacityException {
        return sequencer.tryNext(n);
    }
	
	public void resetTo(long sequence){
		sequencer.claim(sequence);
		sequencer.publish(sequence);
	}
	
	public E claimAndGetPreallocated(long sequence) {
        sequencer.claim(sequence);
        return get(sequence);
    }
	
	public boolean isPublished(long sequence) {
        return sequencer.isAvailable(sequence);
    }
	
	public void addGatingSequences(Sequence... gatingSequences) {
        sequencer.addGatingSequences(gatingSequences);
    }
	
	public long getMinimumGatingSequence() {
        return sequencer.getMinimumSequence();
    }
	
	public boolean removeGatingSequence(Sequence sequence) {
        return sequencer.removeGatingSequence(sequence);
    }
	
	public SequenceBarrier newBarrier(Sequence... sequencesToTrack) {
        return sequencer.newBarrier(sequencesToTrack);
    }
	
	public long getCursor() {
        return sequencer.getCursor();
    }
	
	public int getBufferSize() {
        return bufferSize;
    }
	
	public boolean hasAvailableCapacity(int requiredCapacity) {
        return sequencer.hasAvailableCapacity(requiredCapacity);
    }
	
	public void publishEvent(EventTranslator<E> translator) {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence);
    }
	
	
	
	public boolean tryPublishEvent(EventTranslator<E> translator) {
        try {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public <A> void publishEvent(EventTranslatorOneArg<E, A> translator, A arg0) {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, arg0);
    }
	
	public <A> boolean tryPublishEvent(EventTranslatorOneArg<E, A> translator, A arg0) {
        try {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, arg0);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public <A, B> void publishEvent(EventTranslatorTwoArg<E, A, B> translator, A arg0, B arg1) {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, arg0, arg1);
    }
	
	public <A, B> boolean tryPublishEvent(EventTranslatorTwoArg<E, A, B> translator, A arg0, B arg1) {
        try {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, arg0, arg1);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public <A, B, C> void publishEvent(EventTranslatorThreeArg<E, A, B, C> translator, A arg0, B arg1, C arg2) {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, arg0, arg1, arg2);
    }
	
	public <A, B, C> boolean tryPublishEvent(EventTranslatorThreeArg<E, A, B, C> translator, A arg0, B arg1, C arg2) {
        try {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, arg0, arg1, arg2);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public void publishEvent(EventTranslatorVararg<E> translator, Object...args) {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, args);
    }
	
	public boolean tryPublishEvent(EventTranslatorVararg<E> translator, Object...args) {
        try {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, args);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public void publishEvents(EventTranslator<E>[] translators) {
        publishEvents(translators, 0, translators.length);
    }
	
	public void publishEvents(EventTranslator<E>[] translators, int batchStartsAt, int batchSize) {
        checkBounds(translators, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translators, batchStartsAt, batchSize, finalSequence);
    }
	
	public boolean tryPublishEvents(EventTranslator<E>[] translators) {
        return tryPublishEvents(translators, 0, translators.length);
    }
	
	public boolean tryPublishEvents(EventTranslator<E>[] translators, int batchStartsAt, int batchSize) {
        checkBounds(translators, batchStartsAt, batchSize);
        try {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translators, batchStartsAt, batchSize, finalSequence);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public <A> void publishEvents(EventTranslatorOneArg<E, A> translator, A[] arg0) {
        publishEvents(translator, 0, arg0.length, arg0);
    }
	
	public <A> void publishEvents(EventTranslatorOneArg<E, A> translator, int batchStartsAt, int batchSize, A[] arg0) {
        checkBounds(arg0, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, arg0, batchStartsAt, batchSize, finalSequence);
    }
	
	public <A> boolean tryPublishEvents(EventTranslatorOneArg<E, A> translator, A[] arg0) {
        return tryPublishEvents(translator, 0, arg0.length, arg0);
    }
	
	public <A> boolean tryPublishEvents(EventTranslatorOneArg<E, A> translator, int batchStartsAt, int batchSize, A[] arg0) {
        checkBounds(arg0, batchStartsAt, batchSize);
        try {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, arg0, batchStartsAt, batchSize, finalSequence);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public <A, B> void publishEvents(EventTranslatorTwoArg<E, A, B> translator, A[] arg0, B[] arg1) {
        publishEvents(translator, 0, arg0.length, arg0, arg1);
    }
	
	public <A, B> void publishEvents(EventTranslatorTwoArg<E, A, B> translator, int batchStartsAt, int batchSize, A[] arg0, B[] arg1) {
        checkBounds(arg0, arg1, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, arg0, arg1, batchStartsAt, batchSize, finalSequence);
    }
	
	public <A, B> boolean tryPublishEvents(EventTranslatorTwoArg<E, A, B> translator, A[] arg0, B[] arg1) {
        return tryPublishEvents(translator, 0, arg0.length, arg0, arg1);
    }
	
	public <A, B> boolean tryPublishEvents(EventTranslatorTwoArg<E, A, B> translator, int batchStartsAt, int batchSize, A[] arg0, B[] arg1) {
        checkBounds(arg0, arg1, batchStartsAt, batchSize);
        try {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, arg0, arg1, batchStartsAt, batchSize, finalSequence);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public <A, B, C> void publishEvents(EventTranslatorThreeArg<E, A, B, C> translator, A[] arg0, B[] arg1, C[] arg2) {
        publishEvents(translator, 0, arg0.length, arg0, arg1, arg2);
    }
	
	public <A, B, C> void publishEvents(EventTranslatorThreeArg<E, A, B, C> translator, int batchStartsAt, int batchSize, A[] arg0, B[] arg1, C[] arg2) {
        checkBounds(arg0, arg1, arg2, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, arg0, arg1, arg2, batchStartsAt, batchSize, finalSequence);
    }
	
	public <A, B, C> boolean tryPublishEvents(EventTranslatorThreeArg<E, A, B, C> translator, A[] arg0, B[] arg1, C[] arg2) {
        return tryPublishEvents(translator, 0, arg0.length, arg0, arg1, arg2);
    }
	
	public <A, B, C> boolean tryPublishEvents(EventTranslatorThreeArg<E, A, B, C> translator, int batchStartsAt, int batchSize, A[] arg0, B[] arg1, C[] arg2) {
        checkBounds(arg0, arg1, arg2, batchStartsAt, batchSize);
        try {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, arg0, arg1, arg2, batchStartsAt, batchSize, finalSequence);
            return true;
        }  catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public void publishEvents(EventTranslatorVararg<E> translator, Object[]... args) {
        publishEvents(translator, 0, args.length, args);
    }
	
	public void publishEvents(EventTranslatorVararg<E> translator, int batchStartsAt, int batchSize, Object[]... args) {
        checkBounds(batchStartsAt, batchSize, args);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, batchStartsAt, batchSize, finalSequence, args);
    }
	
	public boolean tryPublishEvents(EventTranslatorVararg<E> translator, Object[]... args) {
        return tryPublishEvents(translator, 0, args.length, args);
    }
	
	public boolean tryPublishEvents(EventTranslatorVararg<E> translator, int batchStartsAt, int batchSize, Object[]... args) {
        checkBounds(args, batchStartsAt, batchSize);
        try {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, batchStartsAt, batchSize, finalSequence, args);
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }
	
	public void publish(long sequence) {
        sequencer.publish(sequence);
    }
	
	public void publish(long lo, long hi) {
        sequencer.publish(lo, hi);
    }
	
	public long remainingCapacity() {
        return sequencer.remainingCapacity();
    }
	
	private void checkBounds(final EventTranslator<E>[] translators, final int batchStartsAt, final int batchSize){
		checkBatchSizing(batchStartsAt,batchSize);
		batchOverRuns(translators,batchStartsAt,batchSize);
	}
	
	private <A> void checkBounds(final A[] arg0, final int batchStartsAt, final int batchSize)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(arg0, batchStartsAt, batchSize);
    }

    private <A, B> void checkBounds(final A[] arg0, final B[] arg1, final int batchStartsAt, final int batchSize)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(arg0, batchStartsAt, batchSize);
        batchOverRuns(arg1, batchStartsAt, batchSize);
    }

    private <A, B, C> void checkBounds(final A[] arg0, final B[] arg1, final C[] arg2, final int batchStartsAt, final int batchSize)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(arg0, batchStartsAt, batchSize);
        batchOverRuns(arg1, batchStartsAt, batchSize);
        batchOverRuns(arg2, batchStartsAt, batchSize);
    }

    private void checkBounds(final int batchStartsAt, final int batchSize, final Object[][] args)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(args, batchStartsAt, batchSize);
    }
	
	private void checkBatchSizing(int batchStartsAt, int batchSize) {
		if(batchStartsAt < 0 || batchSize < 0) {
            throw new IllegalArgumentException("Both batchStartsAt and batchSize must be positive but got: batchStartsAt " + batchStartsAt + " and batchSize " + batchSize);
        }
        else if(batchSize > bufferSize) {
            throw new IllegalArgumentException("The ring buffer cannot accommodate " + batchSize + " it only has space for " + bufferSize + " entities.");
        }
	}
	
	private <A> void batchOverRuns(final A[] arg0, final int batchStartsAt, final int batchSize) {
        if(batchStartsAt + batchSize > arg0.length) {
            throw new IllegalArgumentException("A batchSize of: " + batchSize +
                                               " with batchStatsAt of: " + batchStartsAt +
                                               " will overrun the available number of arguments: " + (arg0.length - batchStartsAt));
        }
    }
	
	private void translateAndPublish(EventTranslator<E> translator, long sequence) {
        try {
            translator.translateTo(get(sequence), sequence);
        }
        finally {
            sequencer.publish(sequence);
        }
    }

    private <A> void translateAndPublish(EventTranslatorOneArg<E, A> translator, long sequence, A arg0) {
        try {
            translator.translateTo(get(sequence), sequence, arg0);
        }
        finally {
            sequencer.publish(sequence);
        }
    }

    private <A, B> void translateAndPublish(EventTranslatorTwoArg<E, A, B> translator, long sequence, A arg0, B arg1) {
        try {
            translator.translateTo(get(sequence), sequence, arg0, arg1);
        }
        finally {
            sequencer.publish(sequence);
        }
    }

    private <A, B, C> void translateAndPublish(EventTranslatorThreeArg<E, A, B, C> translator, long sequence,
                                               A arg0, B arg1, C arg2) {
        try {
            translator.translateTo(get(sequence), sequence, arg0, arg1, arg2);
        }
        finally {
            sequencer.publish(sequence);
        }
    }

    private void translateAndPublish(EventTranslatorVararg<E> translator, long sequence, Object...args) {
        try {
            translator.translateTo(get(sequence), sequence, args);
        }
        finally {
            sequencer.publish(sequence);
        }
    }

    private void translateAndPublishBatch(final EventTranslator<E>[] translators, int batchStartsAt,
                                          final int batchSize, final long finalSequence) {
        final long initialSequence = finalSequence - (batchSize - 1);
        try {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++) {
                final EventTranslator<E> translator = translators[i];
                translator.translateTo(get(sequence), sequence++);
            }
        }
        finally {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private <A> void translateAndPublishBatch(final EventTranslatorOneArg<E, A> translator, final A[] arg0,
                                              int batchStartsAt, final int batchSize, final long finalSequence) {
        final long initialSequence = finalSequence - (batchSize - 1);
        try {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++) {
                translator.translateTo(get(sequence), sequence++, arg0[i]);
            }
        }
        finally {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private <A, B> void translateAndPublishBatch(final EventTranslatorTwoArg<E, A, B> translator, final A[] arg0,
                                                 final B[] arg1, int batchStartsAt, int batchSize,
                                                 final long finalSequence) {
        final long initialSequence = finalSequence - (batchSize - 1);
        try {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++) {
                translator.translateTo(get(sequence), sequence++, arg0[i], arg1[i]);
            }
        }
        finally {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private <A, B, C> void translateAndPublishBatch(final EventTranslatorThreeArg<E, A, B, C> translator,
                                                    final A[] arg0, final B[] arg1, final C[] arg2, int batchStartsAt,
                                                    final int batchSize, final long finalSequence) {
        final long initialSequence = finalSequence - (batchSize - 1);
        try {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++) {
                translator.translateTo(get(sequence), sequence++, arg0[i], arg1[i], arg2[i]);
            }
        }
        finally {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private void translateAndPublishBatch(final EventTranslatorVararg<E> translator, int batchStartsAt,
                                          final int batchSize, final long finalSequence, final Object[][] args) {
        final long initialSequence = finalSequence - (batchSize - 1);
        try {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++) {
                translator.translateTo(get(sequence), sequence++, args[i]);
            }
        }
        finally {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

	private void fill(EventFactory<E> eventFactory) {
		for(int i=0; i<entries.length; i++){
			entries[i] = eventFactory.newInstance();
		}
	}

	



}

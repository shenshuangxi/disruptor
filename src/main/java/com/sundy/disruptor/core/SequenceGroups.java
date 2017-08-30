package com.sundy.disruptor.core;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.sundy.disruptor.core.sequence.Sequence;

public class SequenceGroups {

	/**
	 * 原子添加引用对象
	 * @param holder 持有引用的对象
	 * @param sequenceUpdate 原子操作类
	 * @param cursor	游标值
	 * @param sequencesToAdd 需要添加的引用序列
	 */
	public static <T> void addSequences(
			final T holder,
			final AtomicReferenceFieldUpdater<T, Sequence[]> sequenceUpdate,
			final Cursored cursor, 
			final Sequence... sequencesToAdd) {
		
		long cursorSequence;
		Sequence[] updatedSequences;
		Sequence[] currentSequences;
		
		do {
			currentSequences = sequenceUpdate.get(holder);
			updatedSequences = Arrays.copyOf(currentSequences, sequencesToAdd.length+currentSequences.length);
			cursorSequence = cursor.getCursor();
			int index = currentSequences.length;
			for(Sequence sequence : sequencesToAdd){
				sequence.set(cursorSequence);
				updatedSequences[index++] = sequence;
			}
		} while (!sequenceUpdate.compareAndSet(holder, currentSequences, updatedSequences));
		
	}

	/**
	 * 原子移除引用对象
	 * @param holder 持有引用的对象
	 * @param sequenceUpdate 原子操作类
	 * @param sequence 需要移除的索引序号
	 * @return
	 */
	public static <T> boolean removeSequence(
			final T holder,
			AtomicReferenceFieldUpdater<T, Sequence[]> sequenceUpdate,
			Sequence sequence) {
		int numToRemove;
		Sequence[] oldSequences;
		Sequence[] newSequences;
		
		do {
			oldSequences = sequenceUpdate.get(holder);
			numToRemove = countMatching(oldSequences, sequence);
			newSequences = new Sequence[oldSequences.length-numToRemove];
			int pos = 0;
			for(Sequence temp : oldSequences){
				if(temp != sequence){
					newSequences[pos++] = temp;
				}
			}
		} while (!sequenceUpdate.compareAndSet(holder, oldSequences, newSequences));
		
		return numToRemove != 0;
	}

	private static <T> int countMatching(T[] values, final T toMatch) {
		int numToRemove = 0;
		for(T value : values){
			if(value == toMatch){
				numToRemove ++;
			}
		}
		return numToRemove;
	}

}

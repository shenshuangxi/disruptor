package com.sundy.disruptor.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import com.sundy.disruptor.Sequence;

import sun.misc.Unsafe;

public final class Util {

	private static final Unsafe THE_UNSAGE;
	static{
		try {
			final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {

				public Unsafe run() throws Exception {
					Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
					theUnsafe.setAccessible(true);
					return (Unsafe) theUnsafe.get(null);
				}
				
			};
			THE_UNSAGE = AccessController.doPrivileged(action);
		} catch (Exception e) {
			throw new RuntimeException("Unable to load unsafe", e);
		}
	}
	public static Unsafe getUnsafe() {
		return THE_UNSAGE;
	}
	
	/**
	 * 找到sequences 中值最小的，如果minimum最小则返回这个，或者sequences中最小的
	 * @param sequences
	 * @param minimum
	 * @return
	 */
	public static long getMinimumSequence(Sequence[] sequences, long minimum) {
		for (int i = 0; i < sequences.length; i++) {
			long value = sequences[i].get();
			minimum = Math.min(value, minimum);
		}
		return minimum;
	}

	public static long getMinimumSequence(Sequence[] sequences) {
		return getMinimumSequence(sequences, Long.MAX_VALUE);
	}

	public static int log2(int bufferSize) {
		int r = 0;
		while((bufferSize>>=1)!=0){
			++r;
		}
		return r;
	}

}

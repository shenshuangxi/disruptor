package com.sundy.disruptor.util;

import java.lang.instrument.Instrumentation;

public class MemoryCalculator {

	private static Instrumentation instrumentation = null;
	
	public static void premain(String agentArgs,Instrumentation instrumentation){
		MemoryCalculator.instrumentation = instrumentation;
	}
	
	public static long getObjectSize(Object obj){
		if (instrumentation == null) {
			throw new IllegalStateException("Instrumentation initialize failed");
		}
		return instrumentation.getObjectSize(obj);
	}
	
	/**
	 * 判断是否为共享对象
	 * @param obj
	 * @return
	 */
	private static boolean isSharedObj(Object obj) {
		if (obj instanceof Comparable) {
			if (obj instanceof Enum) {
				return true;
			} else if (obj instanceof String) {
				return (obj == ((String) obj).intern());
			} else if (obj instanceof Boolean) {
				return (obj == Boolean.TRUE || obj == Boolean.FALSE);
			} else if (obj instanceof Integer) {
				return (obj == Integer.valueOf((Integer) obj));
			} else if (obj instanceof Short) {
				return (obj == Short.valueOf((Short) obj));
			} else if (obj instanceof Byte) {
				return (obj == Byte.valueOf((Byte) obj));
			} else if (obj instanceof Long) {
				return (obj == Long.valueOf((Long) obj));
			} else if (obj instanceof Character) {
				return (obj == Character.valueOf((Character) obj));
			}
		}
		return false;
	}
	
}

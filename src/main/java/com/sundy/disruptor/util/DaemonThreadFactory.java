package com.sundy.disruptor.util;

import java.util.concurrent.ThreadFactory;

/**
 * 访问ThreadFactory实例，所有的线程都是由setDaemon(true)的线程创建的
 * @author Administrator
 *
 */
public enum DaemonThreadFactory implements ThreadFactory {
	
	INSTANCE;

	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}

}

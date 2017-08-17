package com.sundy.disruptor;

import com.sundy.disruptor.util.DaemonThreadFactory;
import com.sundy.disruptor.util.MemoryCalculator;
import com.sundy.disruptor.util.PaddedLong;

public class CalculateTest {

	public static void main(String[] args) {
		PaddedLong paddedLong = new PaddedLong();
		/*System.out.println(MemoryCalculator.getObjectSize(paddedLong));*/
		
		final Thread thread = DaemonThreadFactory.INSTANCE.newThread(new Runnable() {
			public void run() {
				System.out.println("哈哈哈");
			}
		});
		thread.start();
		
		Thread thread1 = new Thread(new Runnable() {
			public void run() {
				System.out.println(thread.getThreadGroup().activeCount());
				System.out.println("哈哈哈1");
			}
		});
		
		thread1.start();
		
		Thread thread2 = new Thread(new Runnable() {
			public void run() {
				System.out.println(thread.getThreadGroup().activeCount());
				System.out.println("哈哈哈2");
			}
		});
		
		thread2.start();
		
		try {
			thread.join();
			thread1.join();
			thread2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}

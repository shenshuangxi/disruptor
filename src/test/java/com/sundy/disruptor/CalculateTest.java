package com.sundy.disruptor;

import com.sundy.disruptor.util.MemoryCalculator;
import com.sundy.disruptor.util.PaddedLong;

public class CalculateTest {

	public static void main(String[] args) {
		PaddedLong paddedLong = new PaddedLong();
		/*System.out.println(MemoryCalculator.getObjectSize(paddedLong));*/
		Thread thread = new Thread(){

			@Override
			public void run() {
				System.out.println("哈哈哈哈");
			}
			
		};
		thread.start();
	}

}

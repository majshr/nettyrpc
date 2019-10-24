package com.rpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.rpc.nettyrpc.client.MessageSendExecutor;
import com.rpc.serialize.RpcSerializeProtocolEnum;

import test.service.Calculate;

public class ClientSendRequestTest {
	
	public static void main(String[] args) {
		MessageSendExecutor executor = MessageSendExecutor.getInstance()
				.setRpcServerLoader("localhost:8081", RpcSerializeProtocolEnum.JDKSERIALIZE);
		Calculate calculate = executor.execute(Calculate.class);
//		try {
//			System.out.println(calculate.add(1, 2));	
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		int parallel = 1000;
		CountDownLatch startLatch = new CountDownLatch(parallel);
		CountDownLatch finishLatch = new CountDownLatch(parallel);
		for(int i = 0; i < parallel; i++) {
			new Thread(new RequestThread(startLatch, finishLatch, calculate)).start();
			startLatch.countDown();
		}
		try {
			finishLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.stop();
	}
	
	static class RequestThread implements Runnable{

        static AtomicInteger count = new AtomicInteger(1);

		private CountDownLatch countDownLatch;
		private Calculate calculate;
		private CountDownLatch finishLatch;
		
		@Override
		public void run() {
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
            System.out.println(count.getAndIncrement() + " : " + calculate.add(10, 11));
			finishLatch.countDown();
		}

		public RequestThread(CountDownLatch countDownLatch, CountDownLatch finishLatch, Calculate calculate) {
			super();
			this.countDownLatch = countDownLatch;
			this.calculate = calculate;
			this.finishLatch = finishLatch;
		}
		
	}
}


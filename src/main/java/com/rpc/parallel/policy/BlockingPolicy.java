package com.rpc.parallel.policy;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程池满了，添加到阻塞队列，阻塞当前线程的拒绝策略
 * @author maj
 *
 */
public class BlockingPolicy implements RejectedExecutionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(BlockingPolicy.class);
	
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        LOG.info("线程池满了阻塞队列也满了，线程重新放入阻塞队列，阻塞中.....");
		if(!executor.isShutdown()) {
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				LOG.error("线程放入阻塞队列错误！", e);
			}
		}
	}
}


























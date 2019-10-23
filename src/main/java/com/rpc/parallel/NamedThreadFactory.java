package com.rpc.parallel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义名称的线程工厂
 * @author maj
 *
 */
public class NamedThreadFactory implements ThreadFactory {

	/**
	 * 每个线程池制定一个新的线程工厂；线程工厂数量（使用默认创建方式才递增）
	 */
	private static final AtomicInteger THREAD_POOL_NUMBER = new AtomicInteger(1);
	
	/**
	 * 线程工厂生产的线程数量
	 */
	private final AtomicInteger mThreadNum = new AtomicInteger();
	
	/**
	 * 线程名称前缀
	 */
	private String prefix;
	
	/**
	 * 是否后台线程
	 */
	private final boolean daemoThread;
	
	/**
	 * 线程组
	 */
	private final ThreadGroup threadGroup;
	
	public NamedThreadFactory() {
        this("rpcserver-threadpool-" + THREAD_POOL_NUMBER.getAndIncrement(), false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }
	
	public NamedThreadFactory(String prefix, boolean daemo) {
		this.prefix = prefix + "-thread-";
		this.daemoThread = daemo;
		SecurityManager s = System.getSecurityManager();
		threadGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
		
	}
	
	@Override
	public Thread newThread(Runnable r) {
		String name = prefix + mThreadNum.getAndIncrement();
        Thread ret = new Thread(threadGroup, r, name, 0);
        ret.setDaemon(daemoThread);
        return ret;
	}

}






















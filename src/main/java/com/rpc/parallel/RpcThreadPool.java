package com.rpc.parallel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpc.parallel.policy.BlockingPolicy;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年10月23日 下午5:35:17
 */
public class RpcThreadPool {
	private static final Logger LOG = LoggerFactory.getLogger(RpcThreadPool.class);
	
	private static final int CORE_THREAD_NUM = 5;
	private static final int MAX_THREAD_NUM = 5;
	private static final int WAIT_TIME = 5;
	
    /**
     * （1）执行服务端，方法调用
     */
	static volatile ThreadPoolExecutor executor = null;
	
	/**
	 * rpc调用线程池单例
	 * @return
	 */
	public static Executor getExecutor() {
		
		if(executor == null) {
			synchronized (RpcThreadPool.class) {
				if(executor == null) {
					String name = "RpcThreadPool";
					executor = new ThreadPoolExecutor(CORE_THREAD_NUM, MAX_THREAD_NUM, WAIT_TIME, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), 
							new NamedThreadFactory(name, true), new BlockingPolicy());
				}
			}
		}
		
		return executor;
	}
}

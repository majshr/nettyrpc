package com.rpc.nettyrpc;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aspectj.bridge.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.rpc.config.RpcSystemConfig;
import com.rpc.nettyrpc.client.MessageSendInitializeTask;
import com.rpc.nettyrpc.client.handler.MessageSendHandler;
import com.rpc.parallel.RpcThreadPool;
import com.rpc.serialize.RpcSerializeProtocol;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * 服务端加载器
 * @author maj
 *
 */
public class RpcServerLoader {
	static final Logger LOG = LoggerFactory.getLogger(RpcServerLoader.class);
	
	private static volatile RpcServerLoader rpcServerLoader;
	
	private RpcServerLoader() {
		
	}
	
	/**
	 * 获取单例
	 * @return
	 */
    public static RpcServerLoader getInstance() {
        if (rpcServerLoader == null) {
            synchronized (RpcServerLoader.class) {
                if (rpcServerLoader == null) {
                    rpcServerLoader = new RpcServerLoader();
                }
            }
        }
        return rpcServerLoader;
    }
	
    /******************************功能逻辑***********************************/
    /**
     * ":"分隔符
     */
    private static final String DELIMITER = RpcSystemConfig.DELIMITER;
    /**
     * 默认序列化方式
     */
    private RpcSerializeProtocol serializeProtocol = RpcSerializeProtocol.JDKSERIALIZE;
    /**
     * 线程组线程数，系统可用核数*2
     */
    private static final int PARALLEL = RpcSystemConfig.SYSTEM_PROPERTY_PARALLEL * 2;
    /**
     * NioEventLoopGroup对象
     */
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(PARALLEL);
    
    /**
     * netty rpc线程池数量
     */
    private static int threadNums = RpcSystemConfig.SYSTEM_PROPERTY_THREADPOOL_THREAD_NUMS;
    private static int queueNums = RpcSystemConfig.SYSTEM_PROPERTY_THREADPOOL_QUEUE_NUMS;
    
    /**
     * guava修饰的：带监听功能的线程池
     */
    private static ListeningExecutorService threadPoolExecutor = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor());
    /**
     * 客户端消息处理Handler
     */
    private MessageSendHandler messageSendHandler = null;
    
    /**
     *  是否连接完成锁
     */
    private Lock lock = new ReentrantLock();
    
    private volatile boolean connected = false;
    /**
     * condition 连接成功
     */
    private Condition connectStatus = lock.newCondition();
    
    /**
     * 连接服务端，使用指定序列化协议
     * @param serverAddress
     * @param serializeProtocol
     */
    public void load(String serverAddress, RpcSerializeProtocol serializeProtocol) {
    	String[] ipAddr = serverAddress.split(DELIMITER);
    	if(ipAddr.length == RpcSystemConfig.IPADDR_OPRT_ARRAY_LENGTH) {
    		String host = ipAddr[0];
    		int port = Integer.valueOf(ipAddr[1]);
    		InetSocketAddress address = new InetSocketAddress(host, port);
    		
    		LOG.info("客户端启动，连接远程服务：" + serverAddress);
    		
    		ListenableFuture<Boolean> listenableFuture = threadPoolExecutor.submit(new MessageSendInitializeTask(eventLoopGroup, address, serializeProtocol));

    		Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {

				@Override
				public void onSuccess(Boolean result) {
					// 方法返回时，连接还未成功，MessageSendHandler还没有设置
					// 但是我们在调用发送请求的方法时，不一定连接成功了，
					// 在没连接成功获取MessageSendHandler为空，阻塞等待，所以需要同步通知一下
					// 在连接成功后，设置Handler信息的时候，唤醒一下
					// 此处唤醒没啥用，此时还没有连接成功呢
					
				}

				@Override
				public void onFailure(Throwable t) {
					LOG.error("连接服务端错误", t);
				}
			}, threadPoolExecutor);
    	}
    }
    
    /**
     * 设置messageSendHandler信息，成功连接状态
     * @param messageSendHandler
     */
    public void setMessageSendHandler(MessageSendHandler messageSendHandler) {
    	try {
    		lock.lock();
    		this.messageSendHandler = messageSendHandler;
    		connected = true;
    		connectStatus.signalAll();
    	} finally {
			lock.unlock();
		}
    	
    }
    
    /**
     * 获取发送消息Handler
     * @return
     */
    public MessageSendHandler getMessageSendHandler() {
    	if(connected) {
    		return messageSendHandler; 
    	}
    	
    	// 如果还没有连接完成，阻塞等待，直到连接完成
    	try {
			lock.lock();
			
			// 阻塞等待连接完成
			while(!connected) {
				try {
					connectStatus.await();
				} catch (InterruptedException e) {
					LOG.error("等待连接完成被中断", e);
				}
			}
			
			return messageSendHandler;
		} finally {
			lock.unlock();
		}
    }
    
    /**
     * 卸载资源
     */
    public void unLoad() {
    	// 关闭通道连接
    	messageSendHandler.close();
    	// 关闭线程池
    	threadPoolExecutor.shutdown();
    	// 关闭Netty线程池
    	eventLoopGroup.shutdownGracefully();
    	
    }
}



























package com.rpc.nettyrpc.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.rpc.config.RpcSystemConfig;
import com.rpc.nettyrpc.client.handler.MessageSendChannelInitializer;
import com.rpc.nettyrpc.client.handler.MessageSendHandler;
import com.rpc.parallel.RpcThreadPool;
import com.rpc.serialize.RpcSerializeProtocolEnum;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 客户端连接服务端的加载器，load方法启动，连接服务端
 * 
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
     * 线程组线程数，系统可用核数*2
     */
    private static final int PARALLEL = RpcSystemConfig.SYSTEM_PROPERTY_PARALLEL * 2;
    /**
     * NioEventLoopGroup对象
     */
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(PARALLEL);
    
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
    public void load(String serverAddress, RpcSerializeProtocolEnum serializeProtocol) {
    	String[] ipAddr = serverAddress.split(DELIMITER);
    	if(ipAddr.length == RpcSystemConfig.IPADDR_OPRT_ARRAY_LENGTH) {
    		String host = ipAddr[0];
    		int port = Integer.valueOf(ipAddr[1]);
    		InetSocketAddress address = new InetSocketAddress(host, port);
    		
    		LOG.info("客户端启动，连接远程服务：" + serverAddress);
    		
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                    .remoteAddress(address);

            b.handler(new MessageSendChannelInitializer(serializeProtocol));

            connect(b, address);
    	}
    }
    
    /**
     * Bootstrap连接服务端
     * 
     * @param b
     * @param address
     *            void
     * @date: 2019年10月24日 上午10:56:56
     */
    public void connect(Bootstrap b, InetSocketAddress address) {
        ChannelFuture channelFuture = b.connect();
        channelFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // 连接成功
                if (future.isSuccess()) {
                    MessageSendHandler handler = future.channel().pipeline().get(MessageSendHandler.class);
                    setMessageSendHandler(handler);
                } else { // 连接失败，过一段时间重试
                    eventLoopGroup.schedule(() -> {
                        Log.info("重新尝试连接服务端:    " + address.getHostName() + ":" + address.getPort());
                        try {
                            connect(b, address);
                        } catch (Exception e) {
                            LOG.error("尝试重新连接错误", e);
                        }
                    }, RpcSystemConfig.SYSTEM_PROPERTY_CLIENT_RECONNECT_DELAY, TimeUnit.SECONDS);
                }
            }
        });
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



























package com.rpc.nettyrpc.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.logging.LogFactory;
import com.esotericsoftware.minlog.Log;
import com.rpc.config.RpcSystemConfig;
import com.rpc.nettyrpc.RpcServerLoader;
import com.rpc.nettyrpc.client.handler.MessageSendChannelInitializer;
import com.rpc.nettyrpc.client.handler.MessageSendHandler;
import com.rpc.serialize.RpcSerializeProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 消息发送初始化任务；完成客户端与服务端连接
 * @author maj
 *
 */
public class MessageSendInitializeTask implements Callable<Boolean>{
    static Logger LOG = LoggerFactory.getLogger(MessageSendInitializeTask.class);
	private EventLoopGroup eventLoopGroup;
	private InetSocketAddress remoteAddress;
	private RpcSerializeProtocol protocol;
	
	public MessageSendInitializeTask(EventLoopGroup eventLoopGroup, InetSocketAddress remoteAddress,
			RpcSerializeProtocol protocol) {
		super();
		this.eventLoopGroup = eventLoopGroup;
		this.remoteAddress = remoteAddress;
		this.protocol = protocol;
	}



	@Override
	public Boolean call() throws Exception {
		Bootstrap b = new Bootstrap();
		b.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
		.remoteAddress(remoteAddress);
		
		b.handler(new MessageSendChannelInitializer(protocol));
		
		ChannelFuture channelFuture = b.connect();
		channelFuture.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// 连接成功
				if(future.isSuccess()) {
					MessageSendHandler handler = future.channel().pipeline().get(MessageSendHandler.class);
					RpcServerLoader.getInstance().setMessageSendHandler(handler);
				} else { // 连接失败，过一段时间重试
					eventLoopGroup.schedule(()->{
						Log.info("重新尝试连接服务端:    " + remoteAddress.getHostName() + ":" + remoteAddress.getPort());
						try {
							call();
						} catch (Exception e) {
							LOG.error("尝试重新连接错误", e);
						}
					}, RpcSystemConfig.SYSTEM_PROPERTY_CLIENT_RECONNECT_DELAY, TimeUnit.SECONDS);
				}
			}
		});
		
		return Boolean.TRUE;
	}

}

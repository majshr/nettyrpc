package com.rpc.nettyrpc.client.handler;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.rpc.model.MessageRequest;
import com.rpc.model.MessageResponse;
import com.rpc.nettyrpc.client.MessageCallback;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageSendHandler extends ChannelInboundHandlerAdapter{

	/**
	 * key：Request的id    value  request对应的请求响应对象
	 */
	private ConcurrentHashMap<String, MessageCallback> mapCallback = new ConcurrentHashMap<String, MessageCallback>();
	
	/**
	 * 当前使用的通道
	 */
	private volatile Channel channel;
	
    /**
     * channel 远程连接的地址
     */
    private SocketAddress remoteAddress;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 设置本类需要的信息
		this.remoteAddress = ctx.channel().remoteAddress();
	}
	
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // handler仅是读取响应，请求是主动发送的
    	MessageResponse response = (MessageResponse) msg;
    	String messageId = response.getMessageId();
    	MessageCallback callback = mapCallback.get(messageId);
    	if(callback != null) {
    		// 删除这个引用，设置相应信息
    		mapCallback.remove(messageId);
    		callback.backfillResponse(response);
    	}
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    
    /**
     * 关闭通道连接
     */
    public void close() {
    	channel.close();
    }
    
    /**
     * 发送请求，返回callback信息
     * @param request
     * @return
     */
    public MessageCallback sendRequest(MessageRequest request) {
        MessageCallback callback = new MessageCallback();
    	mapCallback.putIfAbsent(request.getMessageId(), callback);
    	// 发送请求，响应的信息就是调用方法的返回信息
    	channel.writeAndFlush(request);
    	
    	// 向future模式，先返回信息，里边的response信息等服务端返回了再设置
    	return callback;
    }
}

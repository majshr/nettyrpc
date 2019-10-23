package com.rpc.nettyrpc.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpc.model.MessageRequest;
import com.rpc.model.MessageResponse;
import com.rpc.nettyrpc.server.MessageRecvExecutor;
import com.rpc.util.SpringUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageRecvHandler extends SimpleChannelInboundHandler<MessageRequest> {
	private Logger LOG = LoggerFactory.getLogger(MessageRecvHandler.class);

	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MessageRequest request) throws Exception {
		// 根据请求，调用本地方法，然后响应信息
		MessageResponse response = new MessageResponse();
		// 进行请求调用，发送响应（业务线程中执行，不阻塞Netty线程）
		SpringUtil.getBean(MessageRecvExecutor.class).submit(ctx, request, response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("Handler出现错误！", cause);
		ctx.close();
	}
}

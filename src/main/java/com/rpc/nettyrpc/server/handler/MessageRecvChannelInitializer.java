package com.rpc.nettyrpc.server.handler;

import java.util.Map;

import com.rpc.nettyrpc.server.handler.pipeline.init.strategy.NettyRpcPipelineInitRecvFactory;
import com.rpc.serialize.RpcSerializeProtocol;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class MessageRecvChannelInitializer extends ChannelInitializer<SocketChannel>{

	/**
	 * 序列化格式
	 */
	private RpcSerializeProtocol protocol;
	
	private Map<String, Object> handlerMap;
	
	public MessageRecvChannelInitializer(RpcSerializeProtocol protocol, Map<String, Object> handlerMap) {
		super();
		this.protocol = protocol;
		this.handlerMap = handlerMap;
	}



	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		NettyRpcPipelineInitRecvFactory.getNettyRpcPipelineInitUtil(protocol)
			.init(ch.pipeline());
	}

}

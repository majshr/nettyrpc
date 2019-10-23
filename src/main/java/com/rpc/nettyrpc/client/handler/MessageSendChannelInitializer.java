package com.rpc.nettyrpc.client.handler;

import com.rpc.nettyrpc.client.handler.pipeline.init.strategy.NettyRpcPipelineInitFactory;
import com.rpc.serialize.RpcSerializeProtocol;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * client的Handler初始化方法
 * @author maj
 *
 */
public class MessageSendChannelInitializer extends ChannelInitializer<SocketChannel>{

	/**
	 * 序列化协议
	 */
	private RpcSerializeProtocol rpcSerializeProtocol;
	
	public MessageSendChannelInitializer(RpcSerializeProtocol rpcSerializeProtocol) {
		super();
		this.rpcSerializeProtocol = rpcSerializeProtocol;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		NettyRpcPipelineInitFactory
			.getNettyRpcPipelineInitUtil(rpcSerializeProtocol)
			.init(ch.pipeline());
	}

}

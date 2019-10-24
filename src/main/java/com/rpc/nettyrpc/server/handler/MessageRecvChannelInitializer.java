package com.rpc.nettyrpc.server.handler;

import com.rpc.nettyrpc.server.handler.pipeline.init.strategy.NettyRpcPipelineInitRecvFactory;
import com.rpc.serialize.RpcSerializeProtocolEnum;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * handler初始化
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年10月23日 下午5:57:25
 */
public class MessageRecvChannelInitializer extends ChannelInitializer<SocketChannel>{

	/**
     * 序列化方式
     */
	private RpcSerializeProtocolEnum protocol;
	
    public MessageRecvChannelInitializer(RpcSerializeProtocolEnum protocol) {
        super();
        this.protocol = protocol;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		NettyRpcPipelineInitRecvFactory.getNettyRpcPipelineInitUtil(protocol)
			.init(ch.pipeline());
	}

}

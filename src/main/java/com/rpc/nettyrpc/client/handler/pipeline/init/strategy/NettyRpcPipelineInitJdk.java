package com.rpc.nettyrpc.client.handler.pipeline.init.strategy;

import com.rpc.nettyrpc.client.handler.MessageSendHandler;
import com.rpc.serialize.MessageCodecUtil;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * jdk自带方式序列化，初始化pipeline
 * @author maj
 *
 */
public class NettyRpcPipelineInitJdk implements NettyRpcPipelineInit {

	@Override
	public void init(ChannelPipeline pipeline) {
		// 设置头4个字节为长度，四个字节之后数据为数据信息
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, MessageCodecUtil.MESSAGE_LENGTH, 0, MessageCodecUtil.MESSAGE_LENGTH));
        pipeline.addLast(new LengthFieldPrepender(MessageCodecUtil.MESSAGE_LENGTH));
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
        pipeline.addLast(new MessageSendHandler());
	}

}

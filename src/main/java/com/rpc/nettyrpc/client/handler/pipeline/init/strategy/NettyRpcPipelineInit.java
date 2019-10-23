package com.rpc.nettyrpc.client.handler.pipeline.init.strategy;

import io.netty.channel.ChannelPipeline;

/**
 * 初始化netty client pipeline
 * @author maj
 *
 */
public interface NettyRpcPipelineInit {
	/**
	 * 初始化pipeline
	 * @param pipeline
	 */
	public void init(ChannelPipeline pipeline);
}

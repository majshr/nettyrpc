package com.rpc.nettyrpc.client.handler.pipeline.init.strategy;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.rpc.serialize.RpcSerializeProtocolEnum;

/**
 * 根据序列化方式获取初始化pipeline公爵
 * @author maj
 *
 */
public class NettyRpcPipelineInitFactory {
	private static ClassToInstanceMap<NettyRpcPipelineInit> initUtilInstance = MutableClassToInstanceMap.create();

    static {
    	initUtilInstance.putInstance(NettyRpcPipelineInitJdk.class, new NettyRpcPipelineInitJdk());
        // 另外三种待补充
//        handler.putInstance(KryoSendHandler.class, new KryoSendHandler());
//        handler.putInstance(HessianSendHandler.class, new HessianSendHandler()); 
//        handler.putInstance(ProtostuffSendHandler.class, new ProtostuffSendHandler());
    }
	
	public static NettyRpcPipelineInit getNettyRpcPipelineInitUtil(RpcSerializeProtocolEnum protocol) {
		NettyRpcPipelineInit initUtil = null;
		switch(protocol) {
			case JDKSERIALIZE:{
				initUtil = initUtilInstance.get(NettyRpcPipelineInitJdk.class);
				break;
			}
			default:{
				initUtil = initUtilInstance.get(NettyRpcPipelineInitJdk.class);
				break;
			}
		}
		return initUtil;
	}
}

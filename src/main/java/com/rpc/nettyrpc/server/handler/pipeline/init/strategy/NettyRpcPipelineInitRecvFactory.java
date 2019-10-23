package com.rpc.nettyrpc.server.handler.pipeline.init.strategy;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.rpc.nettyrpc.client.handler.pipeline.init.strategy.NettyRpcPipelineInit;
import com.rpc.serialize.RpcSerializeProtocol;

/**
 * 根据序列化方式获取初始化pipeline公爵
 * @author maj
 *
 */
public class NettyRpcPipelineInitRecvFactory {
	private static ClassToInstanceMap<NettyRpcPipelineInit> initUtilInstance = MutableClassToInstanceMap.create();

    static {
    	initUtilInstance.putInstance(NettyRpcPipelineInitRecvJdk.class, new NettyRpcPipelineInitRecvJdk());
        // 另外三种待补充
//        handler.putInstance(KryoSendHandler.class, new KryoSendHandler());
//        handler.putInstance(HessianSendHandler.class, new HessianSendHandler()); 
//        handler.putInstance(ProtostuffSendHandler.class, new ProtostuffSendHandler());
    }
	
    /**
     * 获取初始化pipeline工具类
     * @param protocol
     * @return
     */
	public static NettyRpcPipelineInit getNettyRpcPipelineInitUtil(RpcSerializeProtocol protocol) {
		NettyRpcPipelineInit initUtil = null;
		switch(protocol) {
			case JDKSERIALIZE:{
				initUtil = initUtilInstance.get(NettyRpcPipelineInitRecvJdk.class);
				break;
			}
			default:{
				initUtil = initUtilInstance.get(NettyRpcPipelineInitRecvJdk.class);
				break;
			}
		}
		return initUtil;
	}
}

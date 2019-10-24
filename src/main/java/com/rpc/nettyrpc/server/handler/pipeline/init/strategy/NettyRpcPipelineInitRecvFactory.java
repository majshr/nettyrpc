package com.rpc.nettyrpc.server.handler.pipeline.init.strategy;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.rpc.nettyrpc.client.handler.pipeline.init.strategy.NettyRpcPipelineInit;
import com.rpc.serialize.RpcSerializeProtocolEnum;

/**
 * 根据序列化方式获取初始化pipeline工具
 * 
 * @author maj
 *
 */
public class NettyRpcPipelineInitRecvFactory {
	private static ClassToInstanceMap<NettyRpcPipelineInit> initUtilMap = MutableClassToInstanceMap.create();

    static {
    	initUtilMap.putInstance(NettyRpcPipelineInitRecvJdk.class, new NettyRpcPipelineInitRecvJdk());
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
	public static NettyRpcPipelineInit getNettyRpcPipelineInitUtil(RpcSerializeProtocolEnum protocol) {
		NettyRpcPipelineInit initUtil = null;
		switch(protocol) {
			case JDKSERIALIZE:{
				initUtil = initUtilMap.get(NettyRpcPipelineInitRecvJdk.class);
				break;
			}
			default:{
				initUtil = initUtilMap.get(NettyRpcPipelineInitRecvJdk.class);
				break;
			}
		}
		return initUtil;
	}
}

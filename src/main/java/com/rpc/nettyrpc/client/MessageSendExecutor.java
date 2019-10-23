package com.rpc.nettyrpc.client;

import com.google.common.reflect.Reflection;
import com.rpc.nettyrpc.RpcServerLoader;
import com.rpc.serialize.RpcSerializeProtocol;

/**
 * 消息发送类
 * @author maj
 *
 */
public class MessageSendExecutor {
	static class MessageSendExecutorHolder{
		public static MessageSendExecutor instance = new MessageSendExecutor();	
	}
	
	private MessageSendExecutor() {
		
	}
	
	/**
	 * 单例获取
	 * @return
	 */
	public static MessageSendExecutor getInstance() {
		return MessageSendExecutorHolder.instance;
	}
	
	/***************具体逻辑********************/
	private RpcServerLoader rpcServerLoader = RpcServerLoader.getInstance();
	
	/**
	 * 设置远程连接信息；进行远程连接
	 * @param serverAddress
	 * @param serializeProtocol
	 */
	public MessageSendExecutor setRpcServerLoader(String serverAddress, RpcSerializeProtocol serializeProtocol) {
		rpcServerLoader.load(serverAddress, serializeProtocol);
		return this;
	}
	
	/**
	 * 中断连接
	 */
	public void stop() {
		rpcServerLoader.unLoad();
	}
	
	/**
	 * 为rpc接口生成一个代理类，用于请求服务；（调用方法时，代理会封装方法信息，进行远程调用）
	 * @param <T>
	 * @param rpcInterface
	 * @return
	 */
	public<T> T execute(Class<T> rpcInterface) {
		return Reflection.newProxy(rpcInterface, new MessageSendProxy());
	}
}

package com.rpc.nettyrpc.client;

import java.lang.reflect.Method;
import java.util.UUID;

import com.google.common.reflect.AbstractInvocationHandler;
import com.rpc.model.MessageRequest;
import com.rpc.nettyrpc.RpcServerLoader;
import com.rpc.nettyrpc.client.handler.MessageCallback;

/**
 * 消息发送代理处理类（根据调用的接口，生成处理代理）
 * @author maj
 *
 */
public class MessageSendProxy extends AbstractInvocationHandler {

	@Override
	protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
		MessageRequest request = new MessageRequest();
		request.setClassName(method.getDeclaringClass().getName());
		request.setMessageId(UUID.randomUUID().toString());
		request.setMethodName(method.getName());
		request.setParametersVal(args);
		request.setTypeParameters(method.getParameterTypes());
		
		MessageCallback callback = RpcServerLoader.getInstance().
				getMessageSendHandler().sendRequest(request);
		// 获取响应结果
		return callback.getResponseDataSync();
	}

}

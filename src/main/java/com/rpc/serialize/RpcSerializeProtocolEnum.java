package com.rpc.serialize;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 序列化方式
 * @author maj
 *
 */
public enum RpcSerializeProtocolEnum {
	JDKSERIALIZE("jdknative"), KRYOSERIALIZE("kryo"), HESSIANSERIALIZE("hessian"), PROTOSTUFFSERIALIZE("protostuff");

    private String serializeProtocol;

    private RpcSerializeProtocolEnum(String serializeProtocol) {
        this.serializeProtocol = serializeProtocol;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
        return ReflectionToStringBuilder.toString(this);
    }

    /**
     * 获取序列化方式
     * 
     * @return String
     * @date: 2019年10月23日 下午5:36:43
     */
    public String getProtocol() {
        return serializeProtocol;
    }
}

package com.rpc.serialize;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

/**
 * 消息序列化接口
 * @author maj
 *
 */
public interface MessageCodecUtil {
	/**
	 * 消息的长度，用一个int类型表示
	 */
    final static int MESSAGE_LENGTH = 4;

    /**
     * 对象编码成Netty的ByteBuf
     * @param out
     * @param message
     * @throws IOException
     */
    void encode(final ByteBuf out, final Object message) throws IOException;

    /**
     * 字节码解码成Object对象
     * @param body
     * @return
     * @throws IOException
     */
    Object decode(byte[] body) throws IOException;
}

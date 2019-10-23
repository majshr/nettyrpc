package com.rpc.nettyrpc.echo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

public class ApiEchoInitializer extends ChannelInitializer<SocketChannel> {

	SslContext sslCtx = null;
	
	public ApiEchoInitializer(SslContext sslCtx) {
		super();
		this.sslCtx = sslCtx;
	}


	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
	}

}

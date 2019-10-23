package com.rpc.nettyrpc.echo;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * 查看提供api，服务
 * @author maj
 *
 */
public class ApiEchoResolver implements Callable<Boolean> {
	static Logger LOG = LoggerFactory.getLogger(ApiEchoResolver.class);
	/**
	 * 是否使用ssl加密
	 */
    private static final boolean SSL = System.getProperty("ssl") != null;
    private String host;
    private int port;
	
    public ApiEchoResolver(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
	@Override
	public Boolean call() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
        	// ssl配置
            SslContext sslCtx = null;
            if (SSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            }
            
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ApiEchoInitializer(sslCtx));
            
            ChannelFuture channelFuture = b.bind(host, port).sync();
            // 通道关闭了的话，关闭线程池
            channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(future.isSuccess()) {
						bossGroup.shutdownGracefully();
						workerGroup.shutdownGracefully();
					}
				}
			});
		} catch (Exception e) {
			LOG.error("启动echo服务错误！", e);
			return false;
		} finally {
		}
        
		return true;
	}

}

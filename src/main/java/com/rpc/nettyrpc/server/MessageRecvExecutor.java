package com.rpc.nettyrpc.server;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.rpc.config.RpcSystemConfig;
import com.rpc.model.MessageKeyVal;
import com.rpc.model.MessageRequest;
import com.rpc.model.MessageResponse;
import com.rpc.nettyrpc.echo.ApiEchoResolver;
import com.rpc.nettyrpc.server.handler.MessageRecvChannelInitializer;
import com.rpc.parallel.NamedThreadFactory;
import com.rpc.parallel.RpcThreadPool;
import com.rpc.serialize.RpcSerializeProtocolEnum;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 服务端
 * 
 * @author maj
 *
 */
public class MessageRecvExecutor implements ApplicationContextAware {

    static Logger LOG = LoggerFactory.getLogger(MessageRecvExecutor.class);

    /**
     * 服务端地址
     */
    private String serverAddress;
    /**
     * echo服务端口
     */
    private int echoApiPort;
    /**
     * 序列化方式
     */
    private RpcSerializeProtocolEnum serializeProtocol = RpcSerializeProtocolEnum.JDKSERIALIZE;
    private static final String DELIMITER = RpcSystemConfig.DELIMITER;

    /**
     * 带监听功能的线程池 服务端执行方法调用的线程池
     */
    private static volatile ListeningExecutorService threadPoolExecutor;

    /**
     * key：服务 value：服务对象
     */
    private Map<String, Object> handlerMap = new ConcurrentHashMap<String, Object>();
    private int numberOfEchoThreadsPool = 1;

    /**
     * 执行器
     */
    private static final int PARALLEL = RpcSystemConfig.SYSTEM_PROPERTY_PARALLEL * 2;
    ThreadFactory threadRpcFactory = new NamedThreadFactory("NettyRPC-Server ");
    EventLoopGroup boss = new NioEventLoopGroup(RpcSystemConfig.ONE);
    EventLoopGroup worker = new NioEventLoopGroup(PARALLEL, threadRpcFactory);

    /** 构造方法 */
    public MessageRecvExecutor(String serverAddress) {
        this.serverAddress = serverAddress;
        start();
    }

    /**
     * 提交请求，等待结果
     * 
     * @param ctx
     * @param request
     * @param response
     */
    public void submit(final ChannelHandlerContext ctx, final MessageRequest request, final MessageResponse response) {
        submit(new ExeRequestTask(request, response), ctx, request, response);
    }

    /**
     * 执行请求任务的线程类
     * 
     * @author maj
     *
     */
    class ExeRequestTask implements Callable<Boolean> {

        private MessageRequest request;
        private MessageResponse response;

        @Override
        public Boolean call() throws Exception {
            try {
                // 根据反射调用方法，设置请求结果
                response.setMessageId(request.getMessageId());

                Object serviceBean = handlerMap.get(request.getClassName());
                Class clazz = serviceBean.getClass();
                Method method = clazz.getMethod(request.getMethodName(), request.getTypeParameters());
                method.setAccessible(true);
                Object object = method.invoke(serviceBean, request.getParametersVal());
                response.setReturnNotNull(object == null ? true : false);
                response.setResult(object);
            } catch (Exception e) {
                LOG.error("反射调用错误！", e);
                response.setResult(e);
                response.setError(e.getMessage());
                return false;
            }
            return true;
        }

        public ExeRequestTask(MessageRequest request, MessageResponse response) {
            super();
            this.request = request;
            this.response = response;
        }

    }

    /**
     * 提交请求任务到线程池，等待响应结果
     * 
     * @param task
     *            执行器
     * @param ctx
     * @param request
     * @param response
     */
    public static void submit(Callable<Boolean> task, final ChannelHandlerContext ctx, final MessageRequest request,
            final MessageResponse response) {
        if (threadPoolExecutor == null) {
            synchronized (MessageRecvExecutor.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = MoreExecutors
                            .listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor());
                }
            }
        }

        ListenableFuture<Boolean> future = threadPoolExecutor.submit(task);
        // 等待请求结果是异步的；如果用future的get方法，这里就阻塞住了
        Futures.addCallback(future, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // 响应消息可以不用回调吧，直接在方法调用那，有了结果就回填
                // 响应数据返回给客户端
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        LOG.info("rpc server响应消息message-id：" + request.getMessageId());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("响应错误！", t);
            }
        }, threadPoolExecutor);
    }

    /**
     * 启动服务端Netty应用
     * 启动服务端，启动另一个netty服务，用于查看接口信息
     */
    public void start() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(worker, boss).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new MessageRecvChannelInitializer(serializeProtocol));

            String[] ipAddr = serverAddress.split(MessageRecvExecutor.DELIMITER);

            if (ipAddr.length == RpcSystemConfig.IPADDR_OPRT_ARRAY_LENGTH) {
                final String host = ipAddr[0];
                final int port = Integer.parseInt(ipAddr[1]);
                // 同步绑定端口号
                ChannelFuture channelFuture = b.bind(host, port).sync();

                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            ExecutorService executor = Executors.newFixedThreadPool(numberOfEchoThreadsPool);
                            ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(
                                    executor);

                            // rpc提供接口查看服务
                            completionService.submit(new ApiEchoResolver(host, echoApiPort));

                            LOG.info("nettyRPC服务启动成功..............");

                            // 服务端处理新建连接的连接关闭
                            channelFuture.channel().closeFuture().sync().addListener(new ChannelFutureListener() {

                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    executor.shutdown();
                                    stop();
                                }
                            });
                        }

                    }
                });

            } else {
                LOG.error("NettyRPC服务端域名、端口号配置错误");
            }
        } catch (Exception e) {
            LOG.error("服务端启动错误！", e);
        }

    }

    /**
     * 停止服务
     */
    public void stop() {
        worker.shutdownGracefully();
        boss.shutdownGracefully();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        // 初始化时，设置handlerMap
        try {
            MessageKeyVal keyVal = ctx.getBean(MessageKeyVal.class);
            Map<String, Object> rpcServerObject = keyVal.getMessageKeyVal();
            rpcServerObject.forEach((key, value) -> {
                handlerMap.put(key, value);
            });
        } catch (Exception e) {
            LOG.error("错误", e);
        }
    }

    /**
     * 获取handlerMap
     * 
     * @return
     */
    public Map<String, Object> getHandlerMap() {
        return handlerMap;
    }

}

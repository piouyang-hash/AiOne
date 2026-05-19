package org.myfx.controls.aione.ConnectService.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Netty长连接服务端（connect-service专属，监听8094端口）
 */
@Slf4j
@Component

public class NettyServer {
    // 长连接端口（配置到Nacos，微服务规范）
    @Value("${connect.netty.port}")
    private int nettyPort;

    // Netty核心线程组（boss：接收连接，worker：处理读写）
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    private ObjectProvider<HeartbeatHandler> heartbeatHandlerProvider;
    @Autowired
    private ObjectProvider<ConnectHandler> connectHandlerProvider;

//    /**
//     * 微服务启动时初始化Netty服务
//     */
//    @PostConstruct
//    public void start() {
//        bossGroup = new NioEventLoopGroup(1); // 单线程接收连接
//        workerGroup = new NioEventLoopGroup(); // 多线程处理读写
//
//        try {
//            ServerBootstrap bootstrap = new ServerBootstrap();
//            bootstrap.group(bossGroup, workerGroup)
//                    .channel(NioServerSocketChannel.class)
//                    // 服务端参数：队列大小（高并发）
//                    .option(ChannelOption.SO_BACKLOG, 1024)
//                    // 长连接保持：TCP心跳（底层）
//                    .childOption(ChannelOption.SO_KEEPALIVE, true)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) {
//                            // 增加一个最简单的打印，看控制台有没有输出
//                            System.out.println("收到新连接，正在初始化 Pipeline: " + ch.remoteAddress());
//                            // 1. WebSocket必备处理器（加在最前面）
//                            ch.pipeline().addLast(new HttpServerCodec()); // HTTP编解码（WebSocket基于HTTP握手）
//                            ch.pipeline().addLast(new HttpObjectAggregator(65536)); // 聚合HTTP请求
//                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws")); // WebSocket协议处理器，路径/ws
//
//                            // 2. 心跳检测处理器：30秒读空闲（客户端没发数据）触发检测
//                            ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
//                            // 3. 自定义心跳处理器（处理空闲事件+更新Redis）
//                            // ch.pipeline().addLast(new HeartbeatHandler());
//                            // 要是你这样写，就会因为缺少构造参数而报错，此时修改成自动注入（字段注入方式）
//                            // 但要是这样写，在并发下，又会因为单例bean报错（需要在类上加scope）
//                            // 这是一个典型的“Spring Bean 循环依赖/初始化死锁”导致的 Netty 握手超时。
////                            ch.pipeline().addLast(applicationContext.getBean(HeartbeatHandler.class));
////                            // 4. 长连接业务处理器（绑定用户-通道）
////                            ch.pipeline().addLast(applicationContext.getBean(ConnectHandler.class));
//
//                            // 直接从 Provider 获取新实例（前提是类上有 @Scope("prototype")）
//                            ch.pipeline().addLast(heartbeatHandlerProvider.getObject());
//                            ch.pipeline().addLast(connectHandlerProvider.getObject());
//                        }
//                    });
//
//            // 绑定端口，启动服务
//            ChannelFuture future = bootstrap.bind(nettyPort).sync();
//            log.info("Netty长连接服务启动成功，端口：{}", nettyPort);
//            // 阻塞等待服务关闭
//            future.channel().closeFuture().sync();
//        } catch (InterruptedException e) {
//            log.error("Netty服务启动失败", e);
//            Thread.currentThread().interrupt();
//        }
//    }

    /**
     * 核心修改点：
     * 1. 移除了 sync() 阻塞调用，改用异步线程启动，防止阻塞 Spring 主线程导致初始化死锁。
     * 2. 增加了 PreDestroy 优雅停机，确保 Spring 容器关闭时 Netty 能正常释放资源。
     * 3. 配合 ObjectProvider 解决单例锁竞争问题。
     */
    @PostConstruct
    public void start() {
        new Thread(() -> {
            bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)   // 这个不用改
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                System.out.println("收到新连接，正在初始化 Pipeline: " + ch.remoteAddress());

                                ch.pipeline().addLast(new HttpServerCodec());
                                ch.pipeline().addLast(new HttpObjectAggregator(65536));
                                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                                ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));

                                ch.pipeline().addLast(connectHandlerProvider.getObject());
                                ch.pipeline().addLast(heartbeatHandlerProvider.getObject());
                            }
                        });

                ChannelFuture future = bootstrap.bind(nettyPort).sync();
                log.info("Netty长连接服务启动成功，端口：{}", nettyPort);

                future.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("Netty服务运行异常", e);
            } finally {
                stop();
            }
        }, "netty-start-thread").start();
    }

    /**
     * 优雅关闭 Netty 资源
     */
    @PreDestroy
    public void stop() {
        log.info("正在关闭 Netty 服务...");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
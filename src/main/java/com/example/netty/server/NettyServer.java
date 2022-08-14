package com.example.netty.server;

import com.example.netty.handle.NettyServerEventHandle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author AoDeng
 * @date 2022/8/12
 */
@Component
public class NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Value("${netty.server.port}")
    private Integer nettyPort;

    @Value("${netty.enable}")
    private boolean enable;

    @Autowired
    private NettyServerEventHandle nettyEventHandle;

    private EventLoopGroup boss = new NioEventLoopGroup();

    private EventLoopGroup work = new NioEventLoopGroup();

    /**
     * 启动 Netty
     */
    @PostConstruct
    public void start() {
        if (enable) {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap()
                        .group(boss, work)
                        .channel(NioServerSocketChannel.class)
                        .localAddress(new InetSocketAddress(nettyPort))
                        //服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝
                        .option(ChannelOption.SO_BACKLOG, 128)
                        //Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。
                        //可以将此功能视为TCP的心跳机制，需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline()
                                        .addLast(new IdleStateHandler(60, 60, 20, TimeUnit.SECONDS))
                                        .addLast(nettyEventHandle);
                            }
                        });
                //绑定并开始接受传入的连接。
                ChannelFuture future = bootstrap.bind().sync();
                if (future.isSuccess()) {
                    log.info("Start Netty Serve Success..");
                }
            } catch (Exception e) {
                log.error("启动Netty服务异常,异常原因:{}", e.getMessage());
            }
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        boss.shutdownGracefully().syncUninterruptibly();
        work.shutdownGracefully().syncUninterruptibly();
        log.info("Close Netty Serve Success..");
    }
}

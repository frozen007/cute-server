package com.myz.cuteserver;

import com.myz.cuteserver.handler.HttpRequestHandler;
import com.myz.cuteserver.processor.UriMappingProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.InetSocketAddress;

/**
 * @author: zhaomingyu
 * @date: 2023/11/8 11:24 AM
 * @description:
 */
public class CuteServer {

    private static final Logger logger = LoggerFactory.getLogger(CuteServer.class);

    private static final int NIO_CONN_THREADS = 1;
    private static final int NIO_WORKER_THREADS = 1;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private int port;

    public CuteServer(int port) {
        this.port = port;
    }


    public void start() {
        if (Epoll.isAvailable()) {
            logger.info("CuteServer use EpollEventLoopGroup!");
            bossGroup = new EpollEventLoopGroup(NIO_CONN_THREADS, new DefaultThreadFactory("CuteServerBossGroup", false));
            workerGroup = new EpollEventLoopGroup(NIO_WORKER_THREADS,
                    new DefaultThreadFactory("CuteServerWorkerGroup", true));
        } else {
            bossGroup = new NioEventLoopGroup(NIO_CONN_THREADS, new DefaultThreadFactory("CuteServerBossGroup", false));
            workerGroup = new NioEventLoopGroup(NIO_WORKER_THREADS,
                    new DefaultThreadFactory("CuteServerWorkerGroup", true));
        }

        UriMappingProcessor uriMappingProcessor = new UriMappingProcessor();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(workerGroup instanceof EpollEventLoopGroup ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast("decoder", new HttpRequestDecoder())
                                .addLast("encoder", new HttpResponseEncoder())
                                //.addLast("handler", new CuteServerHandler());
                                .addLast("handler", new HttpRequestHandler(uriMappingProcessor));
                    }
                });

        InetSocketAddress localAddress = new InetSocketAddress(port);
        ChannelFuture channelFuture = bootstrap.bind(localAddress);
        channelFuture.syncUninterruptibly();
    }
}

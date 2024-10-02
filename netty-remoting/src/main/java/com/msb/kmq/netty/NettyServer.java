package com.msb.kmq.netty;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.coder.KryoDecoder;
import com.msb.kmq.coder.KryoEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import javafx.util.Pair;
import java.net.InetSocketAddress;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

//KMQ的Netty的服务端
public class NettyServer implements Server{
    //Netty的服务端启动器
    private  ServerBootstrap serverBootstrap;
    //Netty的主Reactor线程组：用处理客户端的连接请求，接收客户端的TCP请求连接
    private  EventLoopGroup eventLoopGroupBoss;
    //Netty的从Reactor线程组：处理IO的读写时间，处理服务端的读写请求
    private  EventLoopGroup eventLoopGroupSelector;
    //Netty服务端的配置类：
    private  NettyServerConfig nettyServerConfig;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    // Netty的异步线程池，用于处理请求的异步回调结果执行
    private  ExecutorService publicExecutor;

    private NettyServerHandler serverHandler;

    private final HashMap<Integer/* request code */, Pair<NettyRequestProcessor, ExecutorService>> processorTable =
            new HashMap<>(64);

    public NettyServer(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
        this.serverBootstrap = new ServerBootstrap();//Netty服务启动的
        this.eventLoopGroupBoss = new NioEventLoopGroup();
        this.eventLoopGroupSelector = new NioEventLoopGroup();
        this.eventLoopGroupSelector = new NioEventLoopGroup();
        this.publicExecutor = buildPublicExecutor(nettyServerConfig);

    }

    private ExecutorService buildPublicExecutor(NettyServerConfig nettyServerConfig) {
        int publicThreadNums = nettyServerConfig.getServerCallbackExecutorThreads();
        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }
        //ThreadFactory 就是给线程指定名字
        return Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyServerPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });
    }

    //这里是启动Netty服务端的方法
    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyServerConfig.getServerWorkerThreads(),
                new ThreadFactory() {

                    private final AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
                    }
                });
        this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                        .channel( NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)   //服务端处理新连接的请求的最大队列长度
                        .option(ChannelOption.SO_REUSEADDR, true)  //是否允许重复使用地址和端口
                        .option(ChannelOption.SO_KEEPALIVE, false)  //tcp keepalive机制
                        .childOption(ChannelOption.TCP_NODELAY, true)  // 禁止    数据立马发送（不延迟）
                        .childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())  //发送数据缓冲区
                        .childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize()) //接收数据缓冲区
                        .localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(defaultEventExecutorGroup,
                                        new LengthFieldBasedFrameDecoder(65535,
                                                0,2,0,
                                                2),
                                        new LengthFieldPrepender(2),
                                                new KryoDecoder(), //反序列化
                                                new KryoEncoder(), //序列化
                                                new IdleStateHandler(0, 0, nettyServerConfig.getServerChannelMaxIdleTimeSeconds()),
                                               new NettyServerHandler()
                                        );
                            }
                        });
        try {
            ChannelFuture sync = this.serverBootstrap.bind(nettyServerConfig.getListenPort()).sync();
        } catch (InterruptedException e1) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerProcessor(int type, NettyRequestProcessor processor, ExecutorService executor) {
        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<>(processor, executor);
        this.processorTable.put(type, pair);
    }

    @Override
    public Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(int requestCode) {
        return processorTable.get(requestCode);
    }


    class NettyConnectManageHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        }
    }
    //Netty服务端的处理器，处理客户端请求、，根据请求类型选择合适的处理器进行处理
    class NettyServerHandler extends SimpleChannelInboundHandler<KMessage> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, KMessage msg) throws Exception {
            processMessageReceived(ctx, msg);
        }
    }
    //根据接收到的KMessage 类型进行 不同处理，  类型为1   消息发送， 类型2  ，获取某个主题中队列的偏移量 。。。。。
    public void processMessageReceived(ChannelHandlerContext ctx, KMessage msg) throws Exception {
        Channel channel = ctx.channel();
        KMessage response = new KMessage();
        if (msg != null) {
            Pair<NettyRequestProcessor, ExecutorService> matched = this.processorTable.get(Integer.valueOf(msg.getkHeader().getType()));
           //这里的业务逻辑就是要根据注册的
            response = matched.getKey().processRequest(ctx,msg);
            channel.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("send response to client successfully");
                } else {
                    System.out.println("send response to client failed");
                }
            });
        }
    }
}

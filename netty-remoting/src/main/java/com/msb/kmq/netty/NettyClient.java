package com.msb.kmq.netty;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.coder.KryoDecoder;
import com.msb.kmq.coder.KryoEncoder;
import com.msb.kmq.coder.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//基于Netty封装的客户端：用于发送并接收响应。
public class NettyClient implements Client {


    // IO的线程池，主要用于管理和执行IO的处理： 读写操作、编解码
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    //配置类：连接超时时间、发送数据的缓冲区等等
    private final NettyClientConfig nettyClientConfig;
    //启动类（Netty简化）
    private final Bootstrap bootstrap = new Bootstrap();
    // 线程池：执行业务
    private final EventLoopGroup eventLoopGroupWorker;
    //JUC的线程池： 客户端中，需要异步执行一些任务
    private final ExecutorService publicExecutor;

    private AtomicInteger opaque = new AtomicInteger(0);
    //JUC的线程池：客户端的回调，执行一些回调的任务
    private ExecutorService callbackExecutor;
    // 用于保存一些Channle相关：每一个服务器的地址对应一个连接，每个连接ChannelWrapper来标识。
    //用于管理，类似于连接池的概念：复用连接，减少连接的创建和销毁成本
    private final ConcurrentMap<String /* addr */, ChannelWrapper> channelTables = new ConcurrentHashMap<String, ChannelWrapper>();
    //存放客户端发过去的请求的ID和结果的对应关系

    private static final long LOCK_TIMEOUT_MILLIS = 3000;
    private final AtomicReference<List<String>> namesrvAddrList = new AtomicReference<List<String>>();
    private final AtomicReference<String> namesrvAddrChoosed = new AtomicReference<String>();

    private final Lock lockNamesrvChannel = new ReentrantLock();
    private final Lock lockChannelTables = new ReentrantLock();
    //构造方法

    protected final ConcurrentMap<Integer /* opaque */, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);
    public NettyClient(NettyClientConfig nettyClientConfig) {
        this.nettyClientConfig = nettyClientConfig;

        this.publicExecutor = Executors.newFixedThreadPool(nettyClientConfig.getClientWorkerThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyClientWorkerExecutor_" + this.threadIndex.incrementAndGet());
            }
        });
        this.eventLoopGroupWorker = new NioEventLoopGroup(8, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }
    //启动的初始化方法
    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyClientConfig.getClientWorkerThreads(),
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
                    }
                });
        this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(
                                defaultEventExecutorGroup,
                                new LengthFieldBasedFrameDecoder(65535,
                                        0,2,0,
                                        2),
                                new LengthFieldPrepender(2),
                                new KryoEncoder(), //序列化
                                new KryoDecoder(), //反序列化
                                new IdleStateHandler(0, 0, nettyClientConfig.getClientChannelMaxIdleTimeSeconds()),
                                new NettyClientHandler());
                    }
                });
    }
    //该类继承自SimpleChannelInboundHandler，是Netty客户端的事件处理器，用于处理接收到的数据。
    class NettyClientHandler extends SimpleChannelInboundHandler<KMessage> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, KMessage msg) throws Exception {
            //读到相关的数据后，进行响应处理
            final ResponseFuture responseFuture = responseTable.get(opaque.get());
            if (responseFuture != null) {
                responseFuture.putResponse(msg);
                responseTable.remove(opaque.get());
            }else{
                System.out.println("responseFuture is null");
            }
        }
    }
    //一个ChannelFuture的包装器，用于跟踪ChannelFuture的状态。
    static class ChannelWrapper {
        private final ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isOK() {
            return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
        }

        public boolean isWritable() {
            return this.channelFuture.channel().isWritable();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }




    @Override
    public void shutdown() {
        //这里把所有的连接都关闭了
        for (ChannelWrapper cw : this.channelTables.values()) {
            this.closeChannel(null, cw.getChannel());
        }
    }

    @Override
    public void updateNameServerAddressList(List<String> addrs) {

    }

    @Override
    public List<String> getNameServerAddressList() {
        return null;
    }
    //需要 定义统一的Netty通讯的request、response
    @Override
    public KMessage  invokeSync(String addr, KMessage request, long timeoutMillis)  {

        //todo  这里创建连接
        Channel channel=null;
        try {
            channel = getAndCreateChannel(addr);
        } catch (Exception e) {
            e.printStackTrace();
        }


        final int opaque = request.getOpaque();
        ChannelFuture channelFuture = this.bootstrap.connect(string2SocketAddress(addr));
        KMessage response =null;
        //opaque.getAndIncrement();  //这里用来计数（客户端里面请求计数从0开始~ 1,2,3,4,5）
        System.out.println("opaque"+request.getOpaque());
        final ResponseFuture responseFuture = new ResponseFuture(channel, opaque, null, timeoutMillis);
        this.responseTable.put(opaque, responseFuture);
        try {
            //这里消息就发出去
            ByteBuf sendBuf = Unpooled.buffer();
            KryoSerializer.serialize(request, sendBuf);
            channel.writeAndFlush(sendBuf).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    }else{
                        responseFuture.setSendRequestOK(false);
                    }
                    responseTable.remove(opaque);
                }
            });
            //这里等待响应
            response = responseFuture.waitResponse(100);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            this.responseTable.remove(opaque);
        }
        return request;
    }

    public static SocketAddress string2SocketAddress(final String addr) {
        int split = addr.lastIndexOf(":");
        String host = addr.substring(0, split);
        String port = addr.substring(split + 1);
        InetSocketAddress isa = new InetSocketAddress(host, Integer.parseInt(port));
        return isa;
    }

    public Channel getAndCreateChannel(final String addr) throws Exception {
        if (null == addr) {
            return getAndCreateNameserverChannel();
        }

        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        return this.createChannel(addr);
    }

    private Channel getAndCreateNameserverChannel() throws  Exception {
        String addr = this.namesrvAddrChoosed.get();
        if (addr != null) {
            ChannelWrapper cw = this.channelTables.get(addr);
            if (cw != null && cw.isOK()) {
                return cw.getChannel();
            }
        }

        final List<String> addrList = this.namesrvAddrList.get();
        if (this.lockNamesrvChannel.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                addr = this.namesrvAddrChoosed.get();
                if (addr != null) {
                    ChannelWrapper cw = this.channelTables.get(addr);
                    if (cw != null && cw.isOK()) {
                        return cw.getChannel();
                    }
                }

//                if (addrList != null && !addrList.isEmpty()) {
//                    for (int i = 0; i < addrList.size(); i++) {
//                        int index = this.namesrvIndex.incrementAndGet();
//                        index = Math.abs(index);
//                        index = index % addrList.size();
//                        String newAddr = addrList.get(index);
//
//                        this.namesrvAddrChoosed.set(newAddr);
//                        Channel channelNew = this.createChannel(newAddr);
//                        if (channelNew != null) {
//                            return channelNew;
//                        }
//                    }
//                    throw new Exception(addrList.toString());
//                }
            } finally {
                this.lockNamesrvChannel.unlock();
            }
        } else {
            System.out.println("getAndCreateNameserverChannel: try to lock name server, but timeout, {"+LOCK_TIMEOUT_MILLIS+"}ms");
        }

        return null;
    }

    private Channel createChannel(final String addr) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;
                cw = this.channelTables.get(addr);
                if (cw != null) {

                    if (cw.isOK()) {
                        return cw.getChannel();
                    } else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }

                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(string2SocketAddress(addr));
                    //log.info("createChannel: begin to connect remote host[{}] asynchronously", addr);
                    cw = new ChannelWrapper(channelFuture);
                    this.channelTables.put(addr, cw);
                }
            } catch (Exception e) {
               // log.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            //log.warn("createChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
        }

        if (cw != null) {
            ChannelFuture channelFuture = cw.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.nettyClientConfig.getConnectTimeoutMillis())) {
                if (cw.isOK()) {
                   // log.info("createChannel: connect remote host[{}] success, {}", addr, channelFuture.toString());
                    return cw.getChannel();
                } else {
                   // log.warn("createChannel: connect remote host[" + addr + "] failed, " + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                //log.warn("createChannel: connect remote host[{}] timeout {}ms, {}", addr, this.nettyClientConfig.getConnectTimeoutMillis(),
                        channelFuture.toString();
            }
        }

        return null;
    }

    public void closeChannel(final String addr, final Channel channel) {
        if (null == channel)
            return;

        final String addrRemote = addr;

        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    final ChannelWrapper prevCW = this.channelTables.get(addrRemote);

                   // log.info("closeChannel: begin close the channel[{}] Found: {}", addrRemote, prevCW != null);

                    if (null == prevCW) {
                       // log.info("closeChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    } else if (prevCW.getChannel() != channel) {
                      //  log.info("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.",
                             //   addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                       // log.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                    }

                    //RemotingUtil.closeChannel(channel);
                } catch (Exception e) {
                   // log.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
              //  log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
           // log.error("closeChannel exception", e);
        }
    }

    @Override
    public KMessage invokeAsync(String addr, KMessage request, long timeoutMillis) {
        return null;
    }

    @Override
    public void setCallbackExecutor(ExecutorService callbackExecutor) {

    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return null;
    }

}

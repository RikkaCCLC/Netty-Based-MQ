package com.msb.kmq.echo;

import com.msb.kmq.coder.KryoDecoder;
import com.msb.kmq.coder.KryoEncoder;
import com.msb.kmq.netty.NettyServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.InetSocketAddress;

/**
 * 类说明：Netty实现的客户端
 */
public class EchoClient {

    private final int port;
    private final String host;

    public EchoClient(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void start() throws InterruptedException {
        /*线程组*/
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            /*客户端启动必备*/
            Bootstrap b = new Bootstrap();
            b.group(group)/*把线程组传入*/
                    /*指定使用NIO进行网络传输*/
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host,port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                                 @Override
                                 public void initChannel(SocketChannel ch) throws Exception {
                                     ChannelPipeline pipeline = ch.pipeline();
                                     ch.pipeline().addLast(
                                             new LengthFieldBasedFrameDecoder(65535,
                                                     0,2,0,
                                                     2),
                                             new LengthFieldPrepender(2),
                                             new KryoEncoder(), //序列化
                                             new KryoDecoder(), //反序列化
                                             new EchoClientHandle());
                                 };
                    });
            /*连接到远程节点，阻塞直到连接完成*/
            ChannelFuture f = b.connect().sync();
            Channel channel = f.sync().channel();

        }finally {
            group.shutdownGracefully().sync();
        }

    }
    public static void main(String[] args) throws InterruptedException {
        int count =20;
        for(int i =0 ;i< count;i++) {
            new EchoClient(9999, "127.0.0.1").start();
        }
    }
}

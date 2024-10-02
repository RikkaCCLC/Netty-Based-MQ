package com.msb.kmq.echo;

import com.msb.kmq.coder.KryoDecoder;
import com.msb.kmq.coder.KryoEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.InetSocketAddress;

/**
 * 类说明：Netty实现的服务端
 */
public class EchoServer  {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        int port = 9999;
        EchoServer echoServer = new EchoServer(port);
        System.out.println("服务器即将启动");
        echoServer.start();
        System.out.println("服务器关闭");
    }

    public void start() throws InterruptedException {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        /*线程组*/
       // EventLoopGroup group = new NioEventLoopGroup();

        //   这个这个一个 线程组   干所有活
        EventLoopGroup Bossgroup = new NioEventLoopGroup();
        EventLoopGroup Workgroup = new NioEventLoopGroup();
        try {
            /*服务端启动必须*/
            ServerBootstrap b = new ServerBootstrap();
            b.group(Bossgroup,Workgroup)/*将2个线程组传入*/
                    .channel(NioServerSocketChannel.class)/*指定使用NIO<JDK中的selector模型实现的>进行网络传输*/
                    .localAddress(new InetSocketAddress(port))/*指定服务器监听端口*/
                    /*服务端每接收到一个连接请求，就会新启一个socket通信，也就是channel，
                    所以下面这段代码的作用就是为这个子channel增加handle*/
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            /*添加到该子channel的pipeline的尾部*/
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(65535,
                                            0,2,0,
                                            2),
                                    new LengthFieldPrepender(2),
                                    new KryoEncoder(), //序列化
                                    new KryoDecoder(), //反序列化
                                    serverHandler);
                        }
                    });
            ChannelFuture f = b.bind().sync();/*异步绑定到服务器，sync()会阻塞直到完成*/
            f.channel().closeFuture().sync();/*阻塞直到服务器的channel关闭*/

        } finally {
            //Bossgroup.shutdownGracefully().sync();/*优雅关闭线程组*/
            //Workgroup.shutdownGracefully().sync();/*优雅关闭线程组*/
        }

    }


}

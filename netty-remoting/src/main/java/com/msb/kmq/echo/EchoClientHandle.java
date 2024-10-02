package com.msb.kmq.echo;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.coder.KryoSerializer;
import com.msb.kmq.coder.TestKryoCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * 类说明：
 */
public class EchoClientHandle extends SimpleChannelInboundHandler<KMessage> {

    /*客户端读到数据以后，就会执行*/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, KMessage msg)
            throws Exception {
        System.out.println("client acccept:"+msg.toString());
    }

    /*连接建立以后*/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        TestKryoCodeC testC = new TestKryoCodeC();
        ByteBuf sendBuf = Unpooled.buffer();
        KMessage message = testC.getMessage();
        KryoSerializer.serialize(message, sendBuf);

        ctx.writeAndFlush(sendBuf);

        //ctx.fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();

        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}

package com.msb.kmq.echo;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.coder.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 * 类说明：业务处理
 */
@ChannelHandler.Sharable
/*不加这个注解那么在增加到childHandler时就必须new出来*/
public class EchoServerHandler extends SimpleChannelInboundHandler<KMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, KMessage kMessage) throws Exception {
        System.out.println("channelRead0:"+kMessage.toString());
        ctx.writeAndFlush(kMessage);
    }
}

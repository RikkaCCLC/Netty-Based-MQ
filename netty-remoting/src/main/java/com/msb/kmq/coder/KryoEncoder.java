package com.msb.kmq.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 类说明：序列化的Handler
 */
public class KryoEncoder  extends MessageToByteEncoder<KMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, KMessage message,
                          ByteBuf out) throws Exception {
        KryoSerializer.serialize(message, out);
        ctx.flush();
    }
}

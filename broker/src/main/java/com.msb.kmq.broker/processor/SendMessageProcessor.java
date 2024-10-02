package com.msb.kmq.broker.processor;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.netty.NettyRequestProcessor;
import io.netty.channel.ChannelHandlerContext;
//broker接收发送过来的消息核心处理器
public class SendMessageProcessor implements NettyRequestProcessor {
    public KMessage processRequest(ChannelHandlerContext ctx, KMessage request) throws Exception {
        //需要引入brokerController
        return null;
    }

    public boolean rejectRequest() {
        return false;
    }
}

package com.msb.kmq.broker.processor;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.netty.NettyRequestProcessor;
import io.netty.channel.ChannelHandlerContext;

//客户端拉取消息的处理器
public class PullMessageProcessor implements NettyRequestProcessor {
    public KMessage processRequest(ChannelHandlerContext ctx, KMessage request) throws Exception {
        //需要引入brokerController
        return null;
    }

    public boolean rejectRequest() {
        return false;
    }
}

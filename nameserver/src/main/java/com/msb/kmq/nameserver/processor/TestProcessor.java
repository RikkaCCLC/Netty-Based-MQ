package com.msb.kmq.nameserver.processor;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.netty.NettyRequestProcessor;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class TestProcessor implements NettyRequestProcessor {

    //核心处理方法（简单的demo  如果有请求进来了，我们把body改为111111111 响应回去）
    public KMessage processRequest(ChannelHandlerContext ctx, KMessage request) throws Exception {
        if(ctx == null ||request ==null){
            return null;
        }
        Map<String ,Object> attachment = request.getkHeader().getAttachment();
        attachment.get("topic");

        request.setBody("22222222222222");
        //这里把请求当做响应转发
        return request;
    }

    public boolean rejectRequest() {
        return false;
    }

}

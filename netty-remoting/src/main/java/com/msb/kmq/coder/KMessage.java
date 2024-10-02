package com.msb.kmq.coder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//消息的实体类
public  final class KMessage {
    public  KMessage() {
    }

    public static KMessage createRequestCommand(int type,int code) {
        KMessage cmd = new KMessage();
        KHeader kHeader = new KHeader();
        kHeader.setType(type);  //type代表消息类型（1:nameServer的）
        kHeader.setCode(code);  //code代表消息的具体类型(比如注册的，比如获取路由的之类的)
        cmd.kHeader = kHeader;
        return cmd;
    }
    public static KMessage createRequestCommand(int type,int code, Map attachment) {
        KMessage cmd = new KMessage();
        KHeader kHeader = new KHeader();
        kHeader.setType(type);  //type代表消息类型（1:nameServer的）
        kHeader.setCode(code);  //code代表消息的具体类型(比如注册的，比如获取路由的之类的)
        kHeader.setAttachment(attachment);
        cmd.kHeader = kHeader;
        return cmd;
    }

    private  KHeader kHeader;
    private Object body;
    private static AtomicInteger requestId = new AtomicInteger(0);
    private int opaque = requestId.getAndIncrement();
    public int getOpaque() {
        return opaque;
    }
    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }
    public KHeader getkHeader() {
        return kHeader;
    }
    public void setkHeader(KHeader kHeader) {
        this.kHeader = kHeader;
    }
    public Object getBody() {
        return body;
    }
    public void setBody(Object body) {
        this.body = body;
    }
    @Override
    public String toString(){
        return "KMessage[KHeader = "+kHeader.toString()+"][body="+body.toString()+"]"  ;
    }
}

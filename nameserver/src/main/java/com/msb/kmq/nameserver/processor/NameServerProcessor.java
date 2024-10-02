package com.msb.kmq.nameserver.processor;

import com.msb.kmq.coder.KMessage;
import com.msb.kmq.nameserver.NamesrvController;
import com.msb.kmq.nameserver.data.QueueData;
import com.msb.kmq.netty.NettyRequestProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NameServerProcessor implements NettyRequestProcessor {
    final NamesrvController namesrvController;
    public NameServerProcessor(NamesrvController controller) {
        this.namesrvController =controller;
    }

    //核心处理方法
    public KMessage processRequest(ChannelHandlerContext ctx, KMessage request) throws Exception {
        if(ctx == null ||request ==null){
            return null;
        }
        //获取Channel
        Channel channel =ctx.channel();

        //根据不同的消息类型来进行对应的处理
        switch (  request.getkHeader().getCode()){
            case 101:  //注册 Broker
                Map attachment =request.getkHeader().getAttachment();

                String clusterName=attachment.get("clusterName").toString();
                String brokerAddr=attachment.get("brokerAddr").toString();
                String brokerName=attachment.get("brokerName").toString();
                long brokerId=Long.parseLong(attachment.get("brokerId").toString());

                String masterAddr=this.namesrvController.getRouteInfoManager().registerBroker(clusterName,brokerAddr,brokerName,brokerId,channel);
                request.getkHeader().getAttachment().put("masterAddr",masterAddr);
                return request;
            case  102: //注销 Broker
                Map attachment2 =request.getkHeader().getAttachment();

                String clusterName2=attachment2.get("clusterName").toString();
                String brokerAddr2=attachment2.get("brokerAddr").toString();
                String brokerName2=attachment2.get("brokerName").toString();
                long brokerId2=Long.parseLong(attachment2.get("brokerId").toString());

                this.namesrvController.getRouteInfoManager().unregisterBroker(clusterName2,brokerAddr2,brokerName2,brokerId2);
                request.getkHeader().getAttachment().put("del","ok");
                return request;
            case  201: //向 NameServer注册Topic

                Map attachment3 =request.getkHeader().getAttachment();
                String topic=attachment3.get("topic").toString();
                String clusterName3=attachment3.get("clusterName").toString();
                String brokerAddr3=attachment3.get("brokerAddr").toString();
                int QueueNums=Integer.parseInt(attachment3.get("QueueNums").toString());

                 this.namesrvController.getRouteInfoManager().registerTopic(  brokerAddr3, topic,QueueNums);
                request.getkHeader().getAttachment().put("del","ok");

                return request;
            case  202: //获取Topic（202）
                Map attachment4 =request.getkHeader().getAttachment();
                String topic4=attachment4.get("topic").toString();
                List<QueueData> queueDataList=this.namesrvController.getRouteInfoManager().getTopicQueueData(topic4);
                request.getkHeader().getAttachment().put("queueDataList",queueDataList);
                return request;
            default:
                break;
        }


        return null;
    }



    public boolean rejectRequest() {
        return false;
    }

    public KMessage getConfig(ChannelHandlerContext ctx, KMessage request){
        KMessage reponse = new KMessage();
        return reponse;
    }
}

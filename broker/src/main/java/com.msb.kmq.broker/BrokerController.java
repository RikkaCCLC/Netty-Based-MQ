package com.msb.kmq.broker;

import com.msb.kmq.broker.processor.ClientManagerProcessor;
import com.msb.kmq.broker.processor.PullMessageProcessor;
import com.msb.kmq.broker.processor.QueryMessageProcessor;
import com.msb.kmq.broker.processor.SendMessageProcessor;
import com.msb.kmq.netty.NettyClient;
import com.msb.kmq.netty.NettyClientConfig;
import com.msb.kmq.netty.NettyServer;
import com.msb.kmq.netty.NettyServerConfig;

import java.util.concurrent.Executors;

public class BrokerController {
    private final BrokerConfig brokerConfig;
    private final NettyServerConfig nettyServerConfig; //Netty的服务端的网络配置
    private final NettyClientConfig nettyClientConfig; //Netty的客户端的网络配置
    NettyServer BrokerNettyServer;
    NettyClient BrokernettyClient;


    //构造方法
    public BrokerController(BrokerConfig brokerConfig, NettyServerConfig nettyServerConfig, NettyClientConfig nettyClientConfig) {
        this.brokerConfig = brokerConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.nettyClientConfig = nettyClientConfig;
    }

    //init方法
    public boolean init(){
        //初始化NettyServer
        BrokerNettyServer = new NettyServer(nettyServerConfig);
        //注册针对请求的processor
        BrokerNettyServer.registerProcessor(2, new SendMessageProcessor(), Executors.newCachedThreadPool());
        //注册broker接收发送过来的消息核心处理器
        BrokerNettyServer.registerProcessor(3, new ClientManagerProcessor(), Executors.newCachedThreadPool());
        //注册客户端拉取消息的处理器
        BrokerNettyServer.registerProcessor(4, new PullMessageProcessor(), Executors.newCachedThreadPool());
        //注册查消息核心处理器
        BrokerNettyServer.registerProcessor(5, new QueryMessageProcessor(), Executors.newCachedThreadPool());
        //初始化NettyClient
        BrokernettyClient = new NettyClient(nettyClientConfig);
        return true;
    }

    //启动的方法： 网络通讯的服务、其他之类的
    public void  start(){
        BrokerNettyServer.start();
        BrokernettyClient.start();
    }



}

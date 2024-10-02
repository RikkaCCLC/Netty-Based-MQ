package com.msb.kmq.nameserver;

import com.msb.kmq.nameserver.data.RouteInfoManager;
import com.msb.kmq.nameserver.processor.NameServerProcessor;
import com.msb.kmq.nameserver.processor.TestProcessor;
import com.msb.kmq.netty.NettyServer;
import com.msb.kmq.netty.NettyServerConfig;

import java.util.concurrent.Executors;

public class NamesrvController {
    private final NamesrvConfig namesrvConfig; //服务注册的配置
    private final NettyServerConfig nettyServerConfig; //Netty的服务端的网络配置
    private final RouteInfoManager routeInfoManager;
    NettyServer NamesrvNettyServer;
    public NamesrvController(NamesrvConfig namesrvConfig, NettyServerConfig nettyServerConfig) {
        this.namesrvConfig = namesrvConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.routeInfoManager = new RouteInfoManager(namesrvConfig, this);
    }
    public boolean init(){
        //初始化NettyServer
        NamesrvNettyServer = new NettyServer(nettyServerConfig);
        //注册针对请求的processor
        NamesrvNettyServer.registerProcessor(1, new NameServerProcessor(this), Executors.newCachedThreadPool());
        return true;
    }
    public void  start(){
        NamesrvNettyServer.start();
    }
    public void  shutdown(){

    }
    public RouteInfoManager getRouteInfoManager() {
        return routeInfoManager;
    }
}

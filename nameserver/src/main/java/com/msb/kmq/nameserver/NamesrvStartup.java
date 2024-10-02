package com.msb.kmq.nameserver;

import com.msb.kmq.netty.NettyServerConfig;

//启动类
public class NamesrvStartup {
    private static NamesrvConfig namesrvConfig = null;
    private static NettyServerConfig nettyServerConfig = null;


    public static void main(String[] args) {
        namesrvConfig = new NamesrvConfig();
        nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setListenPort(9876);
        NamesrvController controller =new NamesrvController(namesrvConfig,nettyServerConfig);
        controller.init(); //初始化
        controller.start(); //启动
        //JVM关闭的钩子 方法
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                controller.shutdown();
                //进行日志的记录。 其他相关。
            }
        }));
    }
}

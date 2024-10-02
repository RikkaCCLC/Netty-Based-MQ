package com.msb.kmq.sotrage;

public class StorageController {
    //定义配置
    private  String ServerConfig;

    public StorageController(String serverConfig) {
        ServerConfig = serverConfig;
    }
    //初始化的方法：
    public boolean init(){
        return false;
    }

    //启动的方法： 网络通讯的服务、其他之类的
    public void start() {

    }
}

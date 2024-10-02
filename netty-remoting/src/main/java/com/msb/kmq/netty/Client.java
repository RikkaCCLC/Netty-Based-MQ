package com.msb.kmq.netty;

import com.msb.kmq.coder.KMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface Client {
    //启动方法和关闭方法
    void start();
    void shutdown();
    //nameserver地址管理
    void updateNameServerAddressList( List<String> addrs);
    List<String> getNameServerAddressList();
    //同步调用和异步调用
    KMessage invokeSync(String addr, KMessage request, long timeoutMillis) ;
    KMessage invokeAsync( String addr,  KMessage request,  long timeoutMillis) ;
    //设置执行回调任务的线程池
     void setCallbackExecutor( ExecutorService callbackExecutor);
     ExecutorService getCallbackExecutor();


}

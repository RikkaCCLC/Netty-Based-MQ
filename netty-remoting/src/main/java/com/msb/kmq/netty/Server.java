package com.msb.kmq.netty;

import com.msb.kmq.coder.KMessage;
import javafx.util.Pair;

import java.util.concurrent.ExecutorService;
//KMQ的远程服务通信的服务端的接口
public interface Server {
    void start();

    void shutdown();
    //注册处理器：用于注册请求处理器，被用作处理客户端发过来的请求
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor,
                           final ExecutorService executor);
    Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(final int requestCode);

}

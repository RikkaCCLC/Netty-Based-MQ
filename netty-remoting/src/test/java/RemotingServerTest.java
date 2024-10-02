/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.msb.kmq.coder.KHeader;
import com.msb.kmq.coder.KMessage;
import com.msb.kmq.netty.NettyClient;
import com.msb.kmq.netty.NettyClientConfig;
import com.msb.kmq.netty.NettyServer;
import com.msb.kmq.netty.NettyServerConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import java.util.concurrent.Executors;

//一个专门用于测试Remoting的一个测试类
public class RemotingServerTest {
    private static NettyServer nettyServer;
    private static NettyClient nettyClient;

    public static NettyServer createRemotingServer() throws InterruptedException {
        NettyServerConfig config = new NettyServerConfig();
        NettyServer remotingServer = new NettyServer(config);
        remotingServer.registerProcessor(1, new TestProcessor(), Executors.newCachedThreadPool());
        remotingServer.start();
        return remotingServer;
    }

    public static NettyClient createRemotingClient() {
        return createRemotingClient(new NettyClientConfig());
    }

    public static NettyClient createRemotingClient(NettyClientConfig nettyClientConfig) {
        NettyClient client = new NettyClient(nettyClientConfig);
        client.start();
        return client;
    }

    @BeforeClass
    public static void serverRun() throws InterruptedException {
        nettyServer = createRemotingServer(); //启动服务端
        nettyClient = createRemotingClient();//启动客户端
    }



    @Test
    public void testInvokeSync() throws Exception {
        KMessage request = new KMessage();
        KHeader kHeader = new KHeader();
        kHeader.setType(1);
        kHeader.setPriority((byte) 7);
        request.setkHeader(kHeader);
        request.setBody("hello,KMQ!!");

        //使用remotingClient发起同步调用
        KMessage response = nettyClient.invokeSync("127.0.0.1:8888" , request, 1000 * 3);
        KMessage response2 = nettyClient.invokeSync("127.0.0.1:8888" , request, 1000 * 3);
        System.out.println("response:"+response.toString());
        System.out.println("response2:"+response2.toString());
    }

}


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
import com.msb.kmq.nameserver.NamesrvConfig;
import com.msb.kmq.nameserver.NamesrvController;
import com.msb.kmq.netty.NettyClient;
import com.msb.kmq.netty.NettyClientConfig;
import com.msb.kmq.netty.NettyServerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class NameServerInstanceTest {
    protected NamesrvController nameSrvController = null;
    protected NettyServerConfig nettyServerConfig = new NettyServerConfig();
    protected NamesrvConfig namesrvConfig = new NamesrvConfig();
    NettyClient client = new NettyClient(new NettyClientConfig());


    @Before
    public void startup() throws Exception {
        nettyServerConfig.setListenPort(9876);
        nameSrvController = new NamesrvController(namesrvConfig, nettyServerConfig);
        boolean initResult = nameSrvController.init();
        client.start();
        nameSrvController.start();
    }

    @Test
    public void testBrokerProcessor() throws Exception{ //模拟broker->nameServer上的
        //testNameServerProcessor
        testNameServerProcessor("cluster1","broker1",0);

        //testNameServerProcessor2("cluster1","broker2",1);

        //testNameServerProcessor3("cluster1","broker3",2);
    }
    /**
     */


    public void testNameServerProcessor(String cluster,String brokerName,int brokerId) { //模拟注册broker->nameServer上的
        Map<String ,Object> attachment = new HashMap<String, Object>();
        attachment.put("clusterName",cluster);
        attachment.put("brokerAddr","192.168.0.1:10911");
        attachment.put("brokerName",brokerName);
        attachment.put("brokerId",brokerId);
        KMessage request = KMessage.createRequestCommand(1,101,attachment);
        request.setBody("12312312312");
        //client.start();
        //使用client发起同步调用
        KMessage response = client.invokeSync("127.0.0.1:9876" , request, 1000 * 3);
        //client.shutdown();
        System.out.println("response:"+response.toString());
    }

    //模拟客户端从nameServer上获取broker的信息
    public void testGetBrokerInfo() {
        KMessage request = new KMessage();
        KHeader kHeader = new KHeader();
        kHeader.setType(1);  //type代表消息类型（1:nameServer的）
        kHeader.setCode(102);
        Map<String ,Object> attachment = new HashMap<String, Object>();
        attachment.put("clusterName","c1");
        attachment.put("brokerName","brokerA");
        kHeader.setAttachment(attachment);
        request.setkHeader(kHeader);
        request.setBody("");

        //使用client发起同步调用
        KMessage response = client.invokeSync("127.0.0.1:9876" , request, 1000 * 3);
        System.out.println("response:"+response.toString());
    }
    @After
    public void shutdown() throws Exception {
        if (nameSrvController != null) {
            nameSrvController.shutdown();
        }
    }
}

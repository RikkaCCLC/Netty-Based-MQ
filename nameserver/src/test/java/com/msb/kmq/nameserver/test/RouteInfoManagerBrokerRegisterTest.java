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
package com.msb.kmq.nameserver.test;


import com.msb.kmq.nameserver.NamesrvConfig;
import com.msb.kmq.nameserver.data.BrokerData;
import com.msb.kmq.nameserver.data.RouteInfoManager;
import com.msb.kmq.nameserver.data.TopicRouteData;
import io.netty.channel.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class RouteInfoManagerBrokerRegisterTest {
    private static RouteInfoManager routeInfoManager;
    public static String clusterName = "cluster";
    public static String brokerPrefix = "broker";
    public static String topicPrefix = "topic";
    public static int brokerPerName = 3;
    public static int brokerNumber = 3;

    public static Map<String/* brokerName */, BrokerData> brokerAddrTable;//broker信息(broker信息的映射表)


    @Before
    public void setup() {
        routeInfoManager = new RouteInfoManager(new NamesrvConfig(), null);
    }

    @After
    public void terminate() {
        routeInfoManager.printAllPeriodically();
    }

    @Test
    public void testRegisterBroker() {

        for (int j = 0; j < brokerNumber; j++) {
            String brokerName = brokerPrefix+"-"+j;
            routeInfoManager.registerBroker("default-cluster",
                    "127.0.0.1:10911", brokerName, j ,null);
            for (int i = 0; i < brokerPerName; i++) {
                String topicName = topicPrefix+"-"+j+"-"+i;
                routeInfoManager.registerTopic( "127.0.0.1:10911", topicName, 4);
            }
        }
        TopicRouteData data =routeInfoManager.pickupTopicRouteData("topic-1-1");
        data.printAllPeriodically();

    }

}

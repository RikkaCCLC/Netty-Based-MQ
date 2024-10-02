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
package com.msb.kmq.nameserver.data;

import com.msb.kmq.nameserver.NamesrvConfig;
import com.msb.kmq.nameserver.NamesrvController;
import com.msb.kmq.netty.NettyServerConfig;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
//Nameserve的核心类：用于broker和topic等等之类的管理

@Slf4j
public class RouteInfoManager {
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); //读写锁（因为ConcurrentHashMap  它不是强一致性，最终一致性）
    private final Map<String/* topic */, List<QueueData>> topicQueueTable;//主题队列信息（主题和队列的映射表）
    private final Map<String/* brokerName */, BrokerData> brokerAddrTable;//broker信息(broker信息的映射表)
    //broker地址及对应对应broker存活信息(心跳信息)
    private final HashMap<String/* brokerAddr */, BrokerLiveInfo> brokerLiveTable; //心跳信息(Broker 地址 心跳的信息映射表)

    private final  NamesrvController namesrvController;

    private final NamesrvConfig namesrvConfig;

    public RouteInfoManager(final NamesrvConfig namesrvConfig,  NamesrvController namesrvController) {
        this.topicQueueTable = new ConcurrentHashMap<>(1024);//主题队列信息（主题和队列的映射表）
        this.brokerAddrTable = new ConcurrentHashMap<>(128);//broker信息(broker信息的映射表)
        this.brokerLiveTable = new HashMap<String, BrokerLiveInfo>(256);//心跳信息(Broker 地址 心跳的信息映射表)
        this.namesrvConfig = namesrvConfig;
        this.namesrvController = namesrvController;
    }



    //注册Broker核心方法(可以重复去做。心跳也是从这里进行)  返回master节点的地址
    public String registerBroker(
            final String clusterName,
            final String brokerAddr,
            final String brokerName,
            final long brokerId,
            final Channel channel
    ) {
        String masterAddr = "";
        try {
            //注册路由信息使用写锁
            this.lock.writeLock().lockInterruptibly();
            //维护brokerAddrTable
            BrokerData brokerData = this.brokerAddrTable.get(brokerName);
            //每次更新brokerLiveTable信息
            BrokerLiveInfo prevBrokerLiveInfo = this.brokerLiveTable.put(brokerAddr, new BrokerLiveInfo(
                    System.currentTimeMillis(), channel));
            //第一次注册,则创建brokerData
            if (null == brokerData) {
                HashMap map = new HashMap<Long, String>();
                map.put(brokerId, brokerAddr);//brokerAddrs里面存放的是brokerid和brokerAddr
                if (brokerId == 0) masterAddr = brokerAddr;  //业务涉及上  brokerId=0 它就是主节点
                brokerData = new BrokerData(clusterName, brokerName, map);
                this.brokerAddrTable.put(brokerName, brokerData);
            } else {
                //非第一次注册,更新Broker
                Map<Long, String> brokerAddrsMap = brokerData.getBrokerAddrs();
                Iterator<Entry<Long, String>> it = brokerAddrsMap.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Long, String> item = it.next();
                    if (item.getKey() == 0) masterAddr = item.getValue();//这里返回主节点
                    if (null != brokerAddr && !brokerAddr.equals(item.getValue()) && brokerId == item.getKey()) {
                        brokerAddrsMap.put(item.getKey(), brokerAddr);//把里面的数据进行更新
                    }
                }
            }
        } catch (Exception e) {
            log.error("registerBroker Exception:", e);
        } finally {
            this.lock.writeLock().unlock();
        }
        return masterAddr;
    }

    //取消注册Broker的方法，这里是broker挂掉了，或者是broker主动取消注册
    public void unregisterBroker(
            final String clusterName,
            final String brokerAddr,
            final String brokerName,
            final long brokerId
    ) {
        try {
            //取消注册路由信息使用写锁
            this.lock.writeLock().lockInterruptibly();
            //维护brokerAddrTable
            BrokerData brokerData = this.brokerAddrTable.get(brokerName);
            //这里是如果是非空的
            if (brokerData != null) {
                this.brokerAddrTable.remove(brokerName);
            }
        } catch (Exception e) {
            log.error("unregisterBroker Exception:", e);
        } finally {
            this.lock.writeLock().unlock();
        }
    }



    //这里创建队列还有其他关系
    private void createAndUpdateQueueData(final String brokerName, final String topicName, int QueueNums) {
        QueueData queueData = new QueueData();
        queueData.setBrokerName(brokerName);
        queueData.setQueueNums(QueueNums);

        List<QueueData> queueDataList = this.topicQueueTable.get(topicName);
        if (null == queueDataList) {  //这里是第一次创建的
            queueDataList = new LinkedList<QueueData>();
            queueDataList.add(queueData);
            this.topicQueueTable.put(topicName, queueDataList);
        } else { //这里就是需要进行覆盖处理的
            boolean addNewOne = true;
            Iterator<QueueData> it = queueDataList.iterator();
            while (it.hasNext()) {
                QueueData qd = it.next();
                if (qd.getBrokerName().equals(brokerName)) {
                    if (qd.equals(queueData)) {
                        addNewOne = false;
                    } else {
                        it.remove();
                    }
                }
            }
            if (addNewOne) {
                queueDataList.add(queueData);
            }
        }
    }

    //注册Topic（201）
    public void registerTopic( final String brokerAddr, final String topicName, int QueueNums) {
        try {
            this.lock.writeLock().lockInterruptibly();
            //维护topicQueueTable
            this.createAndUpdateQueueData(brokerAddr, topicName, QueueNums);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.lock.writeLock().unlock();
        }
    }


    //获取Topic（202）
    public List<QueueData> getTopicQueueData(final String topicName) {
        try {
            this.lock.readLock().lockInterruptibly();
            return this.topicQueueTable.get(topicName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.lock.readLock().unlock();
        }
        return null;
    }


    //从NameServer返回TopicRouteData
    public TopicRouteData pickupTopicRouteData(final String topic) {
        TopicRouteData topicRouteData = new TopicRouteData();
        boolean foundQueueData = false;
        boolean foundBrokerData = false;
        Set<String> brokerNameSet = new HashSet<String>();
        List<BrokerData> brokerDataList = new LinkedList<BrokerData>();
        topicRouteData.setBrokerDatas(brokerDataList);

        try {
            try {
                //读锁
                this.lock.readLock().lockInterruptibly();
                List<QueueData> queueDataList = this.topicQueueTable.get(topic);
                if (queueDataList != null) {
                    topicRouteData.setQueueDatas(queueDataList);
                    foundQueueData = true;

                    Iterator<QueueData> it = queueDataList.iterator();
                    while (it.hasNext()) {
                        QueueData qd = it.next();
                        brokerNameSet.add(qd.getBrokerName());
                    }

                    for (String brokerName : brokerNameSet) {
                        BrokerData brokerData = this.brokerAddrTable.get(brokerName);
                        if (null != brokerData) {
                            BrokerData brokerDataClone = new BrokerData(brokerData.getCluster(), brokerData.getBrokerName(), (HashMap<Long, String>) brokerData
                                    .getBrokerAddrs().clone());
                            brokerDataList.add(brokerDataClone);
                            foundBrokerData = true;
                        }
                    }
                }
            } finally {
                //释放读锁
                this.lock.readLock().unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (foundBrokerData && foundQueueData) {
            return topicRouteData;
        }
        return topicRouteData;
    }

    //超过120s，则认为Broker失效的方法
    public void scanNotActiveBroker() {
        try {
            this.lock.writeLock().lockInterruptibly();
            //遍历brokerLiveTable
            Iterator<Entry<String, BrokerLiveInfo>> it = this.brokerLiveTable.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, BrokerLiveInfo> next = it.next();
                  long last = next.getValue().getLastUpdateTimestamp();
                //超过120s，则认为Broker失效
                if ((last + 120) < System.currentTimeMillis()) {
                    //从brokerLiveTable中移除
                    it.remove();
                    //从brokerAddrTable中移除
                    this.brokerAddrTable.remove(next.getKey());
                    log.info("The broker's channel expired, " + next.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    //打印相关信息
    public void printAllPeriodically() {
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                log.info("--------------------------------------------------------");
                {
                    log.info("topicQueueTable SIZE: {}",this.topicQueueTable.size());
                    for (Entry<String, List<QueueData>> next : this.topicQueueTable.entrySet()) {
                        log.info("topicQueueTable Topic: {} {}", next.getKey(), next.getValue());
                    }
                }

                {
                    log.info("brokerAddrTable SIZE: {}", this.brokerAddrTable.size());
                    for (Entry<String, BrokerData> next : this.brokerAddrTable.entrySet()) {
                        log.info("brokerAddrTable brokerName: {} {}", next.getKey(), next.getValue());
                    }
                }

                {
                    log.info("brokerLiveTable SIZE: {}", this.brokerLiveTable.size());
                    for (Entry<String, BrokerLiveInfo> next : this.brokerLiveTable.entrySet()) {
                        log.info("brokerLiveTable brokerAddr: {} {}", next.getKey(), next.getValue());
                    }
                }

            } finally {
                this.lock.readLock().unlock();
            }
        } catch (Exception e) {
            log.error("printAllPeriodically Exception", e);
        }
    }

}
//BrokerLiveInfo 代表Broker的心跳信息
class BrokerLiveInfo {
    private long lastUpdateTimestamp;// 上次broker心跳检查时的时间
    private Channel channel;// Broker与NameServer链接通道

    public BrokerLiveInfo(long lastUpdateTimestamp, Channel channel) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.channel = channel;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}

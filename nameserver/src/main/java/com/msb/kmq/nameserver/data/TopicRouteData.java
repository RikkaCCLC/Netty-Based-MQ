package com.msb.kmq.nameserver.data;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
//主题信息：BrokerData（集群名称、节点名称、节点地址信息）、queueDatas（节点名称、队列数量）
@Slf4j
public class TopicRouteData implements Comparable<TopicRouteData> {
    private String TopicConf;
    private List<QueueData> queueDatas;
    private List<BrokerData> brokerDatas;

    public TopicRouteData() {
        queueDatas = new ArrayList<>();
        brokerDatas = new ArrayList<>();
    }

    public TopicRouteData(TopicRouteData topicRouteData) {
        this.queueDatas = new ArrayList<>();
        this.brokerDatas = new ArrayList<>();
        this.TopicConf = topicRouteData.TopicConf;
        if (topicRouteData.queueDatas != null) {
            this.queueDatas.addAll(topicRouteData.queueDatas);
        }
        if (topicRouteData.brokerDatas != null) {
            this.brokerDatas.addAll(topicRouteData.brokerDatas);
        }
    }

    public List<QueueData> getQueueDatas() {
        return queueDatas;
    }

    public void setQueueDatas(List<QueueData> queueDatas) {
        this.queueDatas = queueDatas;
    }

    public List<BrokerData> getBrokerDatas() {
        return brokerDatas;
    }

    public void setBrokerDatas(List<BrokerData> brokerDatas) {
        this.brokerDatas = brokerDatas;
    }

    public String getTopicConf() {
        return TopicConf;
    }

    public void setTopicConf(String orderTopicConf) {
        this.TopicConf = orderTopicConf;
    }

    //打印相关信息
    public void printAllPeriodically() {
        try {
                log.info("--------------------------------------------------------");
                {
                    log.info("TopicConf: {}",TopicConf);
                }

                {
                    log.info("queueDatas SIZE: {}", this.queueDatas.size());


                    //遍历queueDatas ，并且log打印
                    for (QueueData next : this.queueDatas) {
                        log.info("QueueData : {} {}", next.getBrokerName(), next.getQueueNums());
                    }
                }

                {
                    log.info("brokerDatas SIZE: {}", this.brokerDatas.size());
                    for (BrokerData next : this.brokerDatas) {
                        log.info("brokerDatas: {} {} {}", next.getCluster(),next.getBrokerName(), next.getBrokerAddrs());
                    }
                }
        } catch (Exception e) {
            log.error("printAllPeriodically Exception", e);
        }
    }

    @Override
    public int compareTo(TopicRouteData o) {
        return 0;
    }
}

package com.msb.kmq.nameserver.data;
import java.util.HashMap;


//主要保存服务器相关的信息（集群名称、节点名称、节点地址信息）
public class BrokerData implements Comparable<BrokerData> {
    private String cluster;  //集群名称
    private String brokerName; //节点名称
    private HashMap<Long, String> brokerAddrs; //节点地址信息


    public BrokerData(BrokerData brokerData) {
        this.cluster = brokerData.cluster;
        this.brokerName = brokerData.brokerName;
        if (brokerData.brokerAddrs != null) {
            this.brokerAddrs = new HashMap<>(brokerData.brokerAddrs);
        }
    }

    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = brokerAddrs;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public HashMap<Long, String> getBrokerAddrs() {
        return brokerAddrs;
    }

    public void setBrokerAddrs(HashMap<Long, String> brokerAddrs) {
        this.brokerAddrs = brokerAddrs;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public int compareTo(BrokerData o) {
        return 0;
    }
}

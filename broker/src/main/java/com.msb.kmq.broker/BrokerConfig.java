package com.msb.kmq.broker;

import java.io.File;

public class BrokerConfig {
    private String kvConfigPath = System.getProperty("user.home") + File.separator + "broker" + File.separator + "kvConfig.json";
    private String configStorePath = System.getProperty("user.home") + File.separator + "broker" + File.separator + "broker.properties";
    private String productEnvName = "center";
    private boolean clusterTest = false;
    private boolean orderMessageEnable = false;
    private boolean returnOrderTopicConfigToBroker = true;

    /**
     * Indicates the nums of thread to handle client requests, like GET_ROUTEINTO_BY_TOPIC.
     */
    private int clientRequestThreadPoolNums = 8;
    /**
     * Indicates the nums of thread to handle broker or operation requests, like REGISTER_BROKER.
     */
    private int defaultThreadPoolNums = 16;
    /**
     * Indicates the capacity of queue to hold client requests.
     */
    private int clientRequestThreadPoolQueueCapacity = 50000;
    /**
     * Indicates the capacity of queue to hold broker or operation requests.
     */
    private int defaultThreadPoolQueueCapacity = 10000;
    /**
     * Interval of periodic scanning for non-active broker;
     */
    private long scanNotActiveBrokerInterval = 5 * 1000;

    private int unRegisterBrokerQueueCapacity = 3000;

    /**
     * Support acting master or not.
     *
     * The slave can be an acting master when master node is down to support following operations:
     * 1. support lock/unlock message queue operation.
     * 2. support searchOffset, query maxOffset/minOffset operation.
     * 3. support query earliest msg store time.
     */
    private boolean supportActingMaster = false;

    private volatile boolean enableAllTopicList = true;
}

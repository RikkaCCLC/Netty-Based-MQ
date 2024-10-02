package com.msb.kmq.protocol;
//请求消息类型
public class RequestCode {
    //发送消息相关
    //客户端发送消息
    public static final int SEND_MESSAGE = 10;
    //客户端拉取消息
    public static final int PULL_MESSAGE = 11;
    //客户端查询消息
    public static final int QUERY_MESSAGE = 12;
    //客户端查询broke的偏移量
    public static final int QUERY_BROKER_OFFSET = 13;
    //客户端查询消费者的偏移量
    public static final int QUERY_CONSUMER_OFFSET = 14;
    //客户端修改消费者的偏移量
    public static final int UPDATE_CONSUMER_OFFSET = 15;
    //创建或修改主题
    public static final int UPDATE_AND_CREATE_TOPIC = 17;
    //拿到所有的主题配置信息
    public static final int GET_ALL_TOPIC_CONFIG = 21;
    //拿到指定的主题配置信息
    public static final int GET_TOPIC_CONFIG_LIST = 22;


    //nameserver相关的

    //注册broker
    public static final int REGISTER_BROKER = 103;
    //注销broker
    public static final int UNREGISTER_BROKER = 104;
    //通过主题拿到对应的路由信息
    public static final int GET_ROUTEINFO_BY_TOPIC = 105;
    //通过拿到集群中的broker信息
    public static final int GET_BROKER_CLUSTER_INFO = 106;

    //删除主题到nameserver
    public static final int DELETE_TOPIC_IN_NAMESRV = 216;
    //注册主题到nameserver
    public static final int REGISTER_TOPIC_IN_NAMESRV = 217;


}

package com.msb.kmq.broker;
//KMQ中Broker的启动类
//还是喜欢使用controller
public class BrokerStartup {
    public static void main(String[] args) {
//        final BrokerController brokerController = new BrokerController();
//        //brokerController.init();
//        ////....
//        brokerController.init();
//        brokerController.start();
        //init()
        //start()
        //JVM关闭的钩子 方法
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                //进行日志的记录。 其他相关。
            }
        }));

    }

    public static void shutdown(){

    }


}

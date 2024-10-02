package com.msb.kmq.sotrage;
//KMQ中Storage的启动类
public class StorageStartup {
    public static void main(String[] args) {
        final StorageController brokerController = new StorageController("123123");
        ////....
        brokerController.init();
        brokerController.start();
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

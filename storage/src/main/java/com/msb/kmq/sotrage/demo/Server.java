package com.msb.kmq.sotrage.demo;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(8888);
        while (true){
            Socket socket = serverSocket.accept();
            DataInputStream dataInputStream= new DataInputStream(socket.getInputStream());
            //使用文件
            File file = new File("D:\\kmq-data", "data"); //第一次就创建
            FileOutputStream fileOutputStream = new FileOutputStream("D:\\kmq-data\\data");
            long start =  System.currentTimeMillis();
            int byteCount =0;
            byte[] bytes = new byte[1024];
            while (true){
                int readCount = dataInputStream.read(bytes,0,bytes.length);  //这里是一次内核到应用拷贝
                fileOutputStream.write(bytes); //这里是一次应用到内核的拷贝
                byteCount =byteCount+readCount;
                if(readCount == -1){
                    System.out.println("服务端总计接受到了"+byteCount+"字节的数据");
                    break;
                }
            }
            long end =  System.currentTimeMillis();
            System.out.println("time"+ (end-start));
            fileOutputStream.close();
        }
    }
}

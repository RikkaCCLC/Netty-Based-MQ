package com.msb.kmq.sotrage.demo;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ServerMmap {
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(8888);
        while (true){
            Socket socket = serverSocket.accept();
            DataInputStream dataInputStream= new DataInputStream(socket.getInputStream());
            //使用文件
            File file = new File("D:\\kmq-data", "data");
            FileChannel fileChannel = new RandomAccessFile(file,"rw").getChannel();
            MappedByteBuffer mmap= fileChannel.map(FileChannel.MapMode.READ_WRITE,0,1024);//这里是mmap的零拷贝(应用启动的时候)
            long start =  System.currentTimeMillis();
            int byteCount =0;
            byte[] bytes = new byte[1024];
            while (true){
                int readCount = dataInputStream.read(bytes,0,bytes.length);  //这里是一次内核到应用拷贝
                mmap.put(bytes);//这里是没有CPU拷贝的。
                byteCount =byteCount+readCount;
                if(readCount == -1){
                    System.out.println("服务端总计接受到了"+byteCount+"字节的数据");
                    break;
                }
            }
            long end =  System.currentTimeMillis();
            System.out.println("time"+ (end-start));
        }
    }
}

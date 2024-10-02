package com.msb.kmq.sotrage.demo;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws Exception {
        Socket socket =new Socket("localhost",8888);


        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeUTF("立即那大按时打卡有死的哈桑道具卡回到家奥斯卡的1111123123123sadsjaslkdjaklsdjlkncvqw");

        System.out.println("");

    }
}

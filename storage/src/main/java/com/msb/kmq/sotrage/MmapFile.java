package com.msb.kmq.sotrage;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

//这里就是零拷贝的具体实现：   file、byteBuffer
public class MmapFile {
    private ByteBuffer  byteBuffer = null;
    private File file;
    private String fileName;
    private int fileSize;
    private FileChannel fileChannel;

    public MmapFile(String fileName,int fileSize) {
        this.fileName = fileName;//文件的全路径
        this.fileSize = fileSize;
        //零拷贝
        try {
            this.file = new File(fileName);
            this.fileChannel = new RandomAccessFile(this.file,"rw").getChannel();
            this.byteBuffer =fileChannel.map(FileChannel.MapMode.READ_WRITE,0,this.fileSize);
        }catch (Exception e){

        }finally {
            //
        }
    }
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void apppend(ByteBuffer  byteBuffer,long FromOffset){
        int size=0;
        int topicLen =0;
        byte[]  topicName = new byte[topicLen];
        int queueId =0;
        long queueOffset=0;
        long timestamp=0;
        byte[] body = new byte[1000];

        //这里根据  xxxx之获取到  byteBuffer
        ByteBuffer  writeBuffer = byteBuffer;
        writeBuffer.putInt(size);
        writeBuffer.putInt(topicLen);
        writeBuffer.put(topicName);
        writeBuffer.putInt(queueId);
        writeBuffer.putLong(queueOffset);
        writeBuffer.putLong(timestamp);
        writeBuffer.put(body);
    }
}

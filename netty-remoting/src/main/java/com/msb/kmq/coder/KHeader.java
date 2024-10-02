package com.msb.kmq.coder;

import java.util.HashMap;
import java.util.Map;

//消息头
public class KHeader {
    private int crcCode ;//CRC校验
    private  int length; //消息长度
    private  long sessionID;//会话ID
    private  int code;    //消息请求类型
    private  int type;//消息类型(枚举来处理)
    private Map<String ,Object> attachment = new HashMap<String, Object>();//附加信息
    private byte priority;//消息优先级

    public int getCrcCode() {
        return crcCode;
    }

    public void setCrcCode(int crcCode) {
        this.crcCode = crcCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getSessionID() {
        return sessionID;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public int getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
    public void setType(int type) {
        this.type = type;
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }
    @Override
    public String toString() {
        return "MyHeader [crcCode=" + crcCode + ", length=" + length
                + ", sessionID=" + sessionID + ", type=" + type + ", priority="
                + priority + ", attachment=" + attachment + "]";
    }
}

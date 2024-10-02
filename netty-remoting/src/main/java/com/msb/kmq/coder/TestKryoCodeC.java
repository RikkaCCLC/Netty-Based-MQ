package com.msb.kmq.coder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 类说明：序列化器的测试类
 */
public class TestKryoCodeC {

    public KMessage getMessage() {
		KMessage myMessage = new KMessage();
		KHeader kHeader = new KHeader();
		kHeader.setLength(123);
		kHeader.setSessionID(99999);
		kHeader.setType((byte) 1);
		kHeader.setPriority((byte) 7);
		Map<String, Object> attachment = new HashMap<String, Object>();
		for (int i = 0; i < 10; i++) {
			attachment.put("ciyt --> " + i, "king " + i);
		}
		kHeader.setAttachment(attachment);
		myMessage.setkHeader(kHeader);
		myMessage.setBody("abcdefg-----------------------AAAAAA".getBytes(StandardCharsets.UTF_8));
		return myMessage;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
		TestKryoCodeC testC = new TestKryoCodeC();

		for (int i = 0; i < 5; i++) {
			ByteBuf sendBuf = Unpooled.buffer();
			KMessage message = testC.getMessage();
            System.out.println("Encode:"+message + "[body ] "
                    + message.getBody());
            KryoSerializer.serialize(message, sendBuf);
			KMessage decodeMsg = (KMessage)KryoSerializer.deserialize(sendBuf);
			System.out.println("Decode:"+decodeMsg + "<body > "
					+ decodeMsg.getBody());
			System.out
				.println("-------------------------------------------------");
		}

    }

}

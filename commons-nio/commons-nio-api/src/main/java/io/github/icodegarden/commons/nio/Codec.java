package io.github.icodegarden.commons.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class Codec {
	
	public static final int HEADER = 16;
	private static final byte REQUEST = (byte) (1 << 7);
	private static final byte TWOWAT = (byte) (1 << 6);
	private static final byte EVENT = (byte) (1 << 5);
	private static final byte HOLD = 0;
	
	public static ByteBuffer encode(ExchangeMessage message) throws IOException {
		byte i3 = 0;
		if (message.isRequest()) {
			i3 |= REQUEST;
		}
		if (message.isTwoWay()) {
			i3 |= TWOWAT;
		}
		if (message.isEvent()) {
			i3 |= EVENT;
		}
		
		int serializerType = message.getSerializerType();
		
		i3 |= serializerType;
		
		Serializer<Object> serializer = SerializerType.get(serializerType).getSerializer();
		
		byte[] bytes = serializer.serialize(message.getBody());
		ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER + bytes.length);

		byteBuffer.put(HOLD);// 1低位
		byteBuffer.put(HOLD);// 2高位

		byteBuffer.put(i3);// 3
		byteBuffer.put(HOLD);// 4预留
		byteBuffer.putLong(message.getRequestId());// 5-12
		byteBuffer.putInt(bytes.length);// 13-16

		byteBuffer.put(bytes);
		return byteBuffer;
	}
	
	public static ExchangeMessage decode(ByteBuffer headerBuffer, ByteBuffer bodyBuffer) {
		byte i3 = headerBuffer.get(2);// 3
		int serializerType = i3 & 31;//16-23 & 31（00011111）
		
		Deserializer<?> deserializer = SerializerType.get(serializerType).getDeserializer();
		
		byte[] bytes = bodyBuffer.array();// 不用flip；直接引用内部bytes，避免创建新bytes复制

		Object object = deserializer.deserialize(bytes);

		long requestId = headerBuffer.getLong(4);

		ExchangeMessage message = new ExchangeMessage();
		message.setRequest((i3 & REQUEST) == REQUEST);
		message.setTwoWay((i3 & TWOWAT) == TWOWAT);
		message.setEvent((i3 & EVENT) == EVENT);
		message.setSerializerType(serializerType);
		message.setRequestId(requestId);
		message.setBody(object);

		return message;
	}
}

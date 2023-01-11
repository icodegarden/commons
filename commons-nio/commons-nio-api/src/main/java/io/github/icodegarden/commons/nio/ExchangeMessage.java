package io.github.icodegarden.commons.nio;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 该类不会直接用于tcp传输，传输的是body字段
 * 
 * @author Fangfang.Xu
 *
 */
public class ExchangeMessage {

	private static final AtomicLong ID = new AtomicLong(0);

	private boolean request;// 1 request 0 response
	private boolean twoWay;// 1y 0n
	private boolean event;// 1y 0n
	private byte serializerType;
	private long requestId;

	private Object body;

	public static ExchangeMessage heartbeat(boolean request, boolean twoWay) {
		/**
		 * heartbeat的body仅仅是个boolean，不会涉及变更兼容性，可以试用Kryo
		 */
		return new ExchangeMessage(request, twoWay, true, SerializerType.Kryo.getValue(), true);
	}

	public ExchangeMessage(boolean request, boolean twoWay, boolean event, byte serializerType, Object body) {
		this.request = request;
		this.twoWay = twoWay;
		this.event = event;
		this.serializerType = serializerType;
		this.body = body;

		initRequestId();
	}

	private void initRequestId() {
		this.requestId = ID.incrementAndGet();
		if (requestId == Long.MAX_VALUE - 1000000) {// 并发中只有一个线程会触发，重置为0
			ID.set(0);
			initRequestId();
		}
	}

	public boolean isRequest() {
		return request;
	}

	public void setRequest(boolean request) {
		this.request = request;
	}

	public boolean isTwoWay() {
		return twoWay;
	}

	public void setTwoWay(boolean twoWay) {
		this.twoWay = twoWay;
	}

	public boolean isEvent() {
		return event;
	}

	public void setEvent(boolean event) {
		this.event = event;
	}

	public byte getSerializerType() {
		return serializerType;
	}

	public void setSerializerType(byte serializerType) {
		this.serializerType = serializerType;
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "ExchangeMessage [request=" + request + ", twoWay=" + twoWay + ", event=" + event + ", requestId="
				+ requestId + ", body=" + body + "]";
	}

}

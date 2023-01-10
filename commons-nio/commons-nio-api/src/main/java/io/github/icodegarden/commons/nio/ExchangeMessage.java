package io.github.icodegarden.commons.nio;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExchangeMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final AtomicLong ID = new AtomicLong(0);

	private boolean request;// 1 request 0 response
	private boolean twoWay;// 1y 0n
	private boolean event;// 1y 0n
	private int serializerType;
	private long requestId;

	private Object body;

	public ExchangeMessage() {
	}

	public ExchangeMessage(boolean request, boolean twoWay, boolean event, Object body) {
		this(request, twoWay, event, SerializerType.Kryo.getValue(), body);
	}

	public ExchangeMessage(boolean request, boolean twoWay, boolean event, int serializerType, Object body) {
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

	public int getSerializerType() {
		return serializerType;
	}

	public void setSerializerType(int serializerType) {
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

package io.github.icodegarden.commons.nio;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExchangeMessage {

	private static final AtomicLong ID = new AtomicLong(0);

	private boolean request;// 1 request 0 response
	private boolean twoWay;// 1y 0n
	private boolean event;// 1y 0n
	private long requestId;

	private Object body;

	public ExchangeMessage() {
	}

	public ExchangeMessage(boolean request, boolean twoWay, boolean event, Object body) {
		this.request = request;
		this.twoWay = twoWay;
		this.event = event;
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

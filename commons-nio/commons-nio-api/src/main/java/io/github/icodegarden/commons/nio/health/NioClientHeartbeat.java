package io.github.icodegarden.commons.nio.health;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.NioClient;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NioClientHeartbeat implements Heartbeat {
	private static final Logger log = LoggerFactory.getLogger(NioClientHeartbeat.class);

	private final String name;
	private final NioClient nioClient;
	private final Channel channel;
	private long lastSend = System.currentTimeMillis();
	private long lastReceive = System.currentTimeMillis();

	public NioClientHeartbeat(String name, NioClient nioClient,Channel channel) {
		this.name = name;
		this.nioClient = nioClient;
		this.channel = channel;
	}

	@Override
	public void send() throws IOException {
		ExchangeMessage message = new ExchangeMessage(true, true, true, true);
		if (log.isDebugEnabled()) {
			log.debug("{} send beat...,message:{}", name, message);
		}
		channel.write(message);
		lastSend = System.currentTimeMillis();
	}
	
	public void refreshLastSend() {
		lastSend = System.currentTimeMillis();
	}

	@Override
	public void receive() {
		if (log.isDebugEnabled()) {
			log.debug("{} received beat...", name);
		}
		lastReceive = System.currentTimeMillis();
	}

	@Override
	public void response() throws IOException {
		throw new IOException("unuse");// 客户端不需要响应
//			ExchangeMessage message = new ExchangeMessage(false, false, true, true);
//			ByteBuffer buffer = codec.encode(message);
//			buffer.flip();// 必须，flip后才能读
//			while (buffer.hasRemaining()) {
//				javaNioClient.getSocketChannel().write(buffer);
//			}
//			lastSend = System.currentTimeMillis();
	}

	@Override
	public long lastSend() {
		return lastSend;
	}

	@Override
	public long lastReceive() {
		return lastReceive;
	}

	@Override
	public void close() throws IOException {
		nioClient.close();
	}

	@Override
	public void reconnect() throws IOException {
		nioClient.reconnect();
	}

	@Override
	public boolean isClosed() {
		return nioClient.isClosed();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", nioClient=" + nioClient + "]";
	}

}
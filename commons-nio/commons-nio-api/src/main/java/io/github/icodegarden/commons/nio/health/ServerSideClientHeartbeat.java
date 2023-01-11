package io.github.icodegarden.commons.nio.health;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ServerSideClientHeartbeat implements Heartbeat {
	private static final Logger log = LoggerFactory.getLogger(ServerSideClientHeartbeat.class);
	
	private final String name;
	private final Channel channel;
	private long lastSend = System.currentTimeMillis();
	private long lastReceive = System.currentTimeMillis();
	private volatile boolean closed = false;

	public ServerSideClientHeartbeat(String name, Channel channel) {
		this.name = name;
		this.channel = channel;
	}

	@Override
	public void send() throws IOException {
		throw new IOException("unuse");// 客户端不需要主动发送
//			ExchangeMessage message = new ExchangeMessage(true, true, true, true);
//			if (log.isDebugEnabled()) {
//				log.debug("{} send beat...,message:{}", name, message);
//			}
//			socketChannels.write(socketChannel, message);
//			lastSend = System.currentTimeMillis();
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
		ExchangeMessage message = ExchangeMessage.heartbeat(false, false);
		if (log.isDebugEnabled()) {
			log.debug("{} response beat...,message:{}", name, message);
		}
		channel.write(message);
		lastSend = System.currentTimeMillis();
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
		channel.close();
		closed = true;
	}

	@Override
	public void reconnect() throws IOException {
//			closed = false;
		throw new IOException("NOT SUPPORT");
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public String toString() {
		return "[name=" + name + ", channel=" + channel + "]";
	}
}
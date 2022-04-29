package io.github.icodegarden.commons.nio.concurrent;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.health.Heartbeat;

/**
 * <p>
 * 读完同步处理与响应
 * <p>
 * 读完线程池处理与响应
 * <p>
 * 读写线程处理，响应报文池化
 * 
 * @author Fangfang.Xu
 */
public abstract class MessageHandlerStrategy {
	private static final Logger log = LoggerFactory.getLogger(MessageHandlerStrategy.class);

	private final Heartbeat heartbeat;
	private final MessageHandler messageHandler;
	private final Channel channel;

	public MessageHandlerStrategy(Heartbeat heartbeat, MessageHandler messageHandler, Channel channel) {
		this.messageHandler = messageHandler;
		this.heartbeat = heartbeat;
		this.channel = channel;
	}

	protected ExchangeMessage doBiz(ExchangeMessage message) {
		ExchangeMessage resp;
		if (message.isTwoWay()) {
			if (message.isEvent()) {
				heartbeat.receive();
				resp = message;
			} else {
				Object reply = messageHandler.reply(message.getBody());
				ExchangeMessage response = new ExchangeMessage();
				response.setRequest(false);
				response.setTwoWay(false);
				response.setEvent(false);
				response.setRequestId(message.getRequestId());
				response.setBody(reply);
//					this.response = response;
				resp = response;
			}
		} else {
			if (message.isEvent()) {
				heartbeat.receive();
			} else {
				messageHandler.receive(message.getBody());
			}
			return null;
		}
		return resp;
	}

	protected void sendResponse(ExchangeMessage response) throws IOException {
		if (response.isEvent()) {
			heartbeat.response();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("server send response:{}", response);
			}
			channel.write(response);
		}
	}

	/**
	 * @param message the read result
	 */
	public abstract void handle(ExchangeMessage message) throws IOException;

}
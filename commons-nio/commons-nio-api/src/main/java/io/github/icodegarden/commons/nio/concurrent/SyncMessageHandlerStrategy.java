package io.github.icodegarden.commons.nio.concurrent;

import java.io.IOException;

import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.health.Heartbeat;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SyncMessageHandlerStrategy extends MessageHandlerStrategy {

	public SyncMessageHandlerStrategy(Heartbeat heartbeat, MessageHandler messageHandler, Channel channel) {
		super(heartbeat, messageHandler, channel);
	}

	@Override
	public void handle(ExchangeMessage message) throws IOException {
		ExchangeMessage response = doBiz(message);
		if (response == null) {
			return;
		}
		sendResponse(response);
	}
}
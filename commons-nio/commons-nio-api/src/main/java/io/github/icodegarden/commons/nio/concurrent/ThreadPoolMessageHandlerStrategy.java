package io.github.icodegarden.commons.nio.concurrent;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.health.Heartbeat;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ThreadPoolMessageHandlerStrategy extends MessageHandlerStrategy {
	private static final Logger log = LoggerFactory.getLogger(ThreadPoolMessageHandlerStrategy.class);

	private final ThreadPoolExecutor threadpool;

	private final SyncMessageHandlerStrategy sync;

	public ThreadPoolMessageHandlerStrategy(ThreadPoolExecutor threadpool, Heartbeat heartbeat,
			MessageHandler messageHandler, Channel channel) {
		super(heartbeat, messageHandler, channel);
		this.threadpool = threadpool;
		sync = new SyncMessageHandlerStrategy(heartbeat, messageHandler, channel);
	}

	@Override
	public void handle(ExchangeMessage message) throws IOException {
		threadpool.execute(() -> {
			try {
				sync.handle(message);
			} catch (IOException e) {
				log.error("ex in handle message", e);
			}
		});
	}
}
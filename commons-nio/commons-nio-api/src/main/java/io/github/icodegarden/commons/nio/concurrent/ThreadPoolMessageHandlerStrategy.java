package io.github.icodegarden.commons.nio.concurrent;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	
	private static final ThreadPoolExecutor THREADPOOL = new ThreadPoolExecutor(20, 200, 120, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(100), new ThreadFactory() {
				protected final AtomicInteger mThreadNum = new AtomicInteger(1);

				@Override
				public Thread newThread(Runnable runnable) {
					String name = "Nio-ServerSide-MessageHandlerStrategy-" + mThreadNum.getAndIncrement();
					Thread ret = new Thread(runnable, name);
					return ret;
				}
			}, new ThreadPoolExecutor.CallerRunsPolicy());
	
	private final SyncMessageHandlerStrategy sync ;
	
	public ThreadPoolMessageHandlerStrategy(Heartbeat heartbeat, MessageHandler messageHandler,Channel channel) {
		super(heartbeat, messageHandler,channel);
		sync = new SyncMessageHandlerStrategy(heartbeat, messageHandler,channel);
	}

	@Override
	public void handle(ExchangeMessage message) throws IOException {
		THREADPOOL.execute(() -> {
			try {
				sync.handle(message);
			} catch (IOException e) {
				log.error("ex in handle message", e);
			}
		});
	}
}
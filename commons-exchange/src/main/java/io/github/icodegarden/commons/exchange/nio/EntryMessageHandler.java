package io.github.icodegarden.commons.exchange.nio;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.exchange.InstanceExchangeResult;
import io.github.icodegarden.commons.exchange.ParallelShardObject;
import io.github.icodegarden.commons.exchange.ReasonExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.BodyObject;
import io.github.icodegarden.commons.lang.ShardObject;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.MessageHandlerProvider;

/**
 * 入口MessageHandler
 * 
 * @author Fangfang.Xu
 *
 */
public class EntryMessageHandler implements MessageHandler<Object, InstanceExchangeResult> {
	private static final Logger log = LoggerFactory.getLogger(EntryMessageHandler.class);

	private volatile boolean closed;
	private AtomicLong processingCount = new AtomicLong(0);

	private final MessageHandler<BodyObject, ReasonExchangeResult> messageHandler;
	private final List<MessageHandlerProvider<BodyObject, ReasonExchangeResult>> providers;

	public EntryMessageHandler(MessageHandler<BodyObject, ReasonExchangeResult> messageHandler) {
		this.messageHandler = messageHandler;
		this.providers = new LinkedList<>();
	}

	public EntryMessageHandler(List<MessageHandlerProvider<BodyObject, ReasonExchangeResult>> providers) {
		this.messageHandler = null;
		this.providers = new LinkedList<>(providers);
	}

	public void addMessageHandlerProvider(MessageHandlerProvider<BodyObject, ReasonExchangeResult> provider) {
		providers.add(provider);
	}

	@Override
	public InstanceExchangeResult reply(Object obj) {
		if (closed) {
			return InstanceExchangeResult.server(false, null,
					ExchangeFailedReason.serverRejected("Executor Closed", null));
		}

		processingCount.incrementAndGet();

		try {
			BodyObject bodyObject = null;

			if (obj instanceof ParallelShardObject) {
				ParallelShardObject parallelShardObject = ((ParallelShardObject) obj);
				if (parallelShardObject.getObj() != null && parallelShardObject.getObj() instanceof BodyObject) {
					bodyObject = (BodyObject) parallelShardObject.getObj();
					Object body = bodyObject.getBody();
					if (body instanceof ShardObject) {
						((ShardObject) body).setShard(parallelShardObject.getShard());
						((ShardObject) body).setShardTotal(parallelShardObject.getShardTotal());
					}
				}
			} else {
				if (!(obj instanceof BodyObject)) {
					return null;
				}
				bodyObject = (BodyObject) obj;
			}

			ReasonExchangeResult result2 = null;
			if (bodyObject != null) {
				if (!CollectionUtils.isEmpty(providers)) {
					/*
					 * 优先用providers
					 */
					final Object msg = bodyObject;
					Optional<MessageHandlerProvider<BodyObject, ReasonExchangeResult>> optional = providers.stream()
							.filter(provider -> provider.supports(msg)).findFirst();
					if (optional.isPresent()) {
						result2 = optional.get().getMessageHandler().reply(bodyObject);
					} else {
						result2 = messageHandler.reply(bodyObject);
					}
				} else {
					result2 = messageHandler.reply(bodyObject);
				}
			} else {
				result2 = new ReasonExchangeResult(true, null, null);
			}

			if (!result2.isSuccess()) {
				log.warn("receive then handle obj failed, reason:{}", result2.getExchangeFailedReason());
			}

			return InstanceExchangeResult.server(result2.isSuccess(), result2.response(),
					result2.getExchangeFailedReason());
		} catch (Exception e) {
			log.error("ex on receive obj:{}", obj, e);
			return InstanceExchangeResult.server(false, null, ExchangeFailedReason.serverException(e.getMessage(), e));
		} finally {
			if (processingCount.decrementAndGet() <= 0) {
				synchronized (this) {
					this.notify();
				}
			}
		}
	}

	@Override
	public void receive(Object obj) {
	}

	/**
	 * 阻塞直到任务处理完毕或超时
	 * 
	 * @param blockTimeoutMillis
	 */
	public void closeBlocking(long blockTimeoutMillis) {
		closed = true;
		if (processingCount.get() > 0) {
			synchronized (this) {
				try {
					this.wait(blockTimeoutMillis);
				} catch (InterruptedException ignore) {
				}
			}
		}
	}
}

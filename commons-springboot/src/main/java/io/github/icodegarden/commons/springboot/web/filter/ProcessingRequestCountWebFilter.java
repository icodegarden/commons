package io.github.icodegarden.commons.springboot.web.filter;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import reactor.core.publisher.Mono;

/**
 * 适用于spring-webflux<br>
 * 
 * spring gateway不需要这个，因为gateway支持server.shutdown: graceful，自动等待请求处理完<br>
 * 
 * 可以统计处理中的请求数，可以优雅停机<br>
 * 这个类的shutdownOrder数值应该配置的比 服务注销 的GracefullyShutdown数值大
 * 
 * @author Fangfang.Xu
 *
 */
public class ProcessingRequestCountWebFilter implements WebFilter, Ordered, GracefullyShutdown {

	private static final Logger log = LoggerFactory.getLogger(ProcessingRequestCountWebFilter.class);

	private int order = HIGHEST_PRECEDENCE;

	private AtomicLong count = new AtomicLong(0);
	private volatile boolean closed;
	private final int gracefullyShutdownOrder;
	/**
	 * 服务列表刷新间隔
	 */
	private final long instanceRefreshIntervalMs;
	/**
	 * 等待Processing处理完毕的时间
	 */
	private long maxProcessingWaitMs = 10000;

	public ProcessingRequestCountWebFilter(int gracefullyShutdownOrder, long instanceRefreshIntervalMs) {
		this.gracefullyShutdownOrder = gracefullyShutdownOrder;
		this.instanceRefreshIntervalMs = instanceRefreshIntervalMs;
	}

	public void setMaxProcessingWaitMs(long maxProcessingWaitMs) {
		this.maxProcessingWaitMs = maxProcessingWaitMs;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		if (closed) {
			return Mono.error(new IllegalStateException(shutdownName() + " was closed"));
		}

		count.incrementAndGet();

		return chain.filter(exchange).doFinally(signalType -> {
			count.decrementAndGet();

//			long c = count.decrementAndGet();
//			if (c <= 0) {
//				synchronized (this) {
//					this.notify();
//				}
//			}
		});
	}

	@Override
	public String shutdownName() {
		return "Processing-Request-Count-WebFilter";
	}

	/**
	 * 等待足够的时间处理完毕来自web接口的请求或超时<br>
	 * 因为先进行 服务注销，因此一段时间后不会再有新的请求进来
	 */
	@Override
	public void shutdown() {
		log.info("gracefully shutdown wait instanceRefresh ms:{}", instanceRefreshIntervalMs);
		try {
			Thread.sleep(instanceRefreshIntervalMs);
		} catch (InterruptedException ignore) {
		}

//		if (count.get() > 0) {
//			synchronized (this) {
//				try {
//					log.info("gracefully shutdown max wait ms:{}", maxProcessingWaitMs);
//
//					this.wait(maxProcessingWaitMs);
//				} catch (InterruptedException ignore) {
//					Thread.currentThread().interrupt();
//				}
//			}
//		}

		/**
		 * 相比上面的方式不用在 finally中this.notify(); 效率好一点点
		 */
		long waitMs = 0;
		int sleepMs = 1000;
		while (count.get() > 0 && waitMs++ < maxProcessingWaitMs) {
			try {
				Thread.sleep(sleepMs);
			} catch (InterruptedException ignore) {
			}
		}

		closed = true;
	}

	@Override
	public int shutdownOrder() {
		return gracefullyShutdownOrder;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
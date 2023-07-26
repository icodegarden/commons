package io.github.icodegarden.commons.springboot.web.filter;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.springboot.autoconfigure.CommonsWebAutoConfiguration;
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
public class ReactiveProcessingRequestCountFilter extends AbstractProcessingRequestCount implements WebFilter, Ordered {

	private int order = CommonsWebAutoConfiguration.FILTER_ORDER_PROCESSING_REQUEST_COUNT;

	public ReactiveProcessingRequestCountFilter(int gracefullyShutdownOrder, long instanceRefreshIntervalMs) {
		super(gracefullyShutdownOrder, instanceRefreshIntervalMs);
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

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
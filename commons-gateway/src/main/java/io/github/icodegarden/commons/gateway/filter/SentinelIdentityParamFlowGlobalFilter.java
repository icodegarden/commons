package io.github.icodegarden.commons.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;

import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import reactor.core.publisher.Mono;

/**
 * 对不同身份的流控<br>
 * 由于SentinelGatewayFilter不能支持对不同身份（jwt、app）的流控，因此设立这个Filter<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class SentinelIdentityParamFlowGlobalFilter implements GlobalFilter, Ordered {

	public static final String RESOURCE_NAME = "identityParamFlow";

	private final int order;

	public SentinelIdentityParamFlowGlobalFilter() {
		this(-2);
	}

	public SentinelIdentityParamFlowGlobalFilter(int order) {
		this.order = order;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		/**
		 * 认证通过时设置的
		 */
		String identityId = exchange.getRequest().getHeaders()
				.getFirst(GatewayPreAuthenticatedAuthenticationFilter.HEADER_USERID);
		if (identityId == null) {
			identityId = exchange.getRequest().getHeaders()
					.getFirst(GatewayPreAuthenticatedAuthenticationFilter.HEADER_APPID);
		}
		
		final String param = identityId;

		return Mono.fromCallable(() -> {
			Entry entry = null;
			try {
				entry = SphU.entry(RESOURCE_NAME, EntryType.IN, 1, param);
				/**
				 * pass
				 */
//                chain.filter(exchange);
				return null;
			} finally {
				if (entry != null) {
					entry.exit(1, param);
				}
			}
		}).and(chain.filter(exchange));
	}

	@Override
	public int getOrder() {
		return order;
	}
}

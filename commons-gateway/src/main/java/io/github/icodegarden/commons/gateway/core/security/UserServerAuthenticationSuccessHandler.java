package io.github.icodegarden.commons.gateway.core.security;

import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.springboot.loadbalancer.FlowTagLoadBalancer;
import io.github.icodegarden.commons.springboot.security.User;
import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class UserServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
		return Mono.defer(() -> {
			WebFilterChain chain = webFilterExchange.getChain();
			ServerWebExchange exchange = webFilterExchange.getExchange();

			User principal = (User) authentication.getPrincipal();
			Map<String, Object> details = (Map) authentication.getDetails();

			ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
				httpHeaders.add(GatewayPreAuthenticatedAuthenticationFilter.HEADER_USERID, principal.getUserId());
				httpHeaders.add(GatewayPreAuthenticatedAuthenticationFilter.HEADER_USERNAME, principal.getUsername());
				if (details != null) {
					String flowTagRequired = (String) details.get(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_REQUIRED);
					String flowTagFirst = (String) details.get(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_FIRST);
					httpHeaders.add(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_REQUIRED, flowTagRequired);
					httpHeaders.add(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_FIRST, flowTagFirst);
				}
			}).build();

			return chain.filter(exchange.mutate().request(request).build());
		});
	}
}
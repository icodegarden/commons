package io.github.icodegarden.commons.gateway.core.security;

import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.gateway.core.security.signature.App;
import io.github.icodegarden.commons.gateway.spi.AppProvider;
import io.github.icodegarden.commons.gateway.util.CommonsGatewayUtils;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.springboot.loadbalancer.FlowTagLoadBalancer;
import io.github.icodegarden.commons.springboot.security.User;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AppServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

	public static final String HTTPHEADER_APPKEY = "X-Auth-AppKey";

	private final AppProvider appProvider;
	private final boolean passHeaderAppKey;

	public AppServerAuthenticationSuccessHandler(AppProvider appProvider, boolean passHeaderAppKey) {
		this.appProvider = appProvider;
		this.passHeaderAppKey = passHeaderAppKey;
	}

	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
		return Mono.defer(() -> {
			WebFilterChain chain = webFilterExchange.getChain();
			ServerWebExchange exchange = webFilterExchange.getExchange();

			User principal = (User) authentication.getPrincipal();
			Map<String, Object> details = (Map) authentication.getDetails();

			ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
				OpenApiRequestBody requestBody = CommonsGatewayUtils.getOpenApiRequestBody(exchange);
				httpHeaders.add(WebUtils.HEADER_REQUEST_ID, requestBody.getRequest_id());
				
				httpHeaders.add(WebUtils.HEADER_APPID, principal.getUserId());
				httpHeaders.add(WebUtils.HEADER_APPNAME, principal.getUsername());
				if (details != null) {
					String flowTagRequired = (String) details.get(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_REQUIRED);
					String flowTagFirst = (String) details.get(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_FIRST);
					httpHeaders.add(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_REQUIRED, flowTagRequired);
					httpHeaders.add(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_FIRST, flowTagFirst);
				}

				if (passHeaderAppKey) {
					App app = appProvider.getApp(requestBody.getApp_id());
					httpHeaders.add(HTTPHEADER_APPKEY, app.getAppKey());
				}
			}).build();

			return chain.filter(exchange.mutate().request(request).build());
		});
	}
}
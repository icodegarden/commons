package io.github.icodegarden.commons.springboot.web.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.springboot.autoconfigure.CommonsWebAutoConfiguration;
import io.github.icodegarden.commons.springboot.security.Authentication;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.security.SimpleAuthentication;
import io.github.icodegarden.commons.springboot.security.SimpleUser;
import io.github.icodegarden.commons.springboot.web.util.ReactiveWebUtils;
import io.github.icodegarden.commons.springboot.web.util.matcher.ReactiveAntPathRequestMatcher;
import io.github.icodegarden.commons.springboot.web.util.matcher.ReactiveOrRequestMatcher;
import io.github.icodegarden.commons.springboot.web.util.matcher.ReactiveRequestMatcher;
import reactor.core.publisher.Mono;

/**
 * 适用于spring-webflux<br>
 * 
 * 网关预认证的身份filter<br>
 * 
 * @author Fangfang.Xu
 */
public class ReactiveGatewayPreAuthenticatedAuthenticationFilter implements WebFilter, Ordered {

	private int order = CommonsWebAutoConfiguration.FILTER_ORDER_GATEWAY_PRE_AUTHENTICATED_AUTHENTICATION;

	private ReactiveOrRequestMatcher shouldAuthOpenapiMatcher;
	private ReactiveOrRequestMatcher shouldAuthInternalApiMatcher;

	/**
	 * 默认认为POST /openapi/**、/api/** 应该已经pre认证了
	 */
	public ReactiveGatewayPreAuthenticatedAuthenticationFilter() {
		setShouldAuthOpenapi(Arrays.asList(new AntPath("/openapi/**", "POST")));
		setShouldAuthInternalApi(Arrays.asList(new AntPath("/api/**", null), new AntPath("/internalapi/**", null),
				new AntPath("/innerapi/**", null)));
	}

	/**
	 * path以 / 开头,method可以null表示无感<br>
	 * path例如/**、/xxxx/**、/file/api/v1/softwareParts/{id}/files/{filename}/upload
	 * 
	 * @param antPaths
	 */
	public void setShouldAuthOpenapi(Collection<AntPath> antPaths) {
		List<ReactiveRequestMatcher> ants = antPaths.stream()
				.map(antPath -> new ReactiveAntPathRequestMatcher(antPath.getPattern(), antPath.getHttpMethod()))
				.collect(Collectors.toList());
		shouldAuthOpenapiMatcher = new ReactiveOrRequestMatcher(ants);
	}

	/**
	 * path以 / 开头,method可以null表示无感<br>
	 * path例如/**、/xxxx/**、/file/api/v1/softwareParts/{id}/files/{filename}/upload
	 * 
	 * @param antPaths
	 */
	public void setShouldAuthInternalApi(Collection<AntPath> antPaths) {
		List<ReactiveRequestMatcher> ants = antPaths.stream()
				.map(antPath -> new ReactiveAntPathRequestMatcher(antPath.getPattern(), antPath.getHttpMethod()))
				.collect(Collectors.toList());
		shouldAuthInternalApiMatcher = new ReactiveOrRequestMatcher(ants);
	}

	private boolean shouldAuthOpenapi(ServerWebExchange exchange) {
		return shouldAuthOpenapiMatcher.matches(exchange);
	}

	private boolean shouldAuthInternalApi(ServerWebExchange exchange) {
		return shouldAuthInternalApiMatcher.matches(exchange);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		String appId = request.getHeaders().getFirst(ReactiveWebUtils.HEADER_APPID);
		if (appId == null && shouldAuthOpenapi(exchange)) {
			/**
			 * 不需要以ApiResponse返回，因为这是gateway犯的错
			 */
			exchange.getResponse().setRawStatusCode(401);
			DefaultDataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance
					.wrap("Access Denied, Unauthorized, App No Principal".getBytes());
			return exchange.getResponse().writeWith(Mono.just(dataBuffer));
		}

		String userId = null;
		if (appId == null) {
			userId = request.getHeaders().getFirst(ReactiveWebUtils.HEADER_USERID);
			if (userId == null && shouldAuthInternalApi(exchange)) {
				/**
				 * 不需要以ApiResponse返回，因为这是gateway犯的错
				 */
				exchange.getResponse().setRawStatusCode(401);
				DefaultDataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance
						.wrap("Access Denied, Unauthorized, User No Principal".getBytes());
				return exchange.getResponse().writeWith(Mono.just(dataBuffer));
			}
		}

		Authentication authentication = null;
		if (appId != null) {
			String appName = request.getHeaders().getFirst(ReactiveWebUtils.HEADER_APPNAME);

			SimpleUser user = new SimpleUser(appId, appName, "", Collections.emptyList());
			authentication = new SimpleAuthentication(user, Collections.emptyList());
		} else if (userId != null) {
			String userName = request.getHeaders().getFirst(ReactiveWebUtils.HEADER_USERNAME);

			SimpleUser user = new SimpleUser(userId, userName, "", Collections.emptyList());
			authentication = new SimpleAuthentication(user, Collections.emptyList());
		}
		if (authentication != null) {
			SecurityUtils.setAuthentication(authentication);
		}

		ReactiveWebUtils.setExchange(exchange);

		return chain.filter(exchange).doFinally(s -> {
			SecurityUtils.setAuthentication(null);
			ReactiveWebUtils.setExchange(null);
		});
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public static class AntPath {
		private final String pattern;
		private final String httpMethod;

		public AntPath(String pattern, @Nullable String httpMethod) {
			Assert.hasText(pattern, "pattern must not empty");
			this.pattern = pattern;
			this.httpMethod = httpMethod != null ? httpMethod.toUpperCase() : null;
		}

		public String getPattern() {
			return pattern;
		}

		public String getHttpMethod() {
			return httpMethod;
		}

		@Override
		public String toString() {
			return "AntPath [pattern=" + pattern + ", httpMethod=" + httpMethod + "]";
		}
	}
}

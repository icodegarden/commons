package io.github.icodegarden.commons.gateway.core.security.jwt;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.gateway.core.security.AuthMatcher;
import io.github.icodegarden.commons.gateway.spi.AuthWebFilter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class JWTAuthenticationWebFilter implements AuthWebFilter {

	private final AuthMatcher authMatcher;
	private final AuthenticationWebFilter authenticationWebFilter;

	public JWTAuthenticationWebFilter(AuthMatcher authMatcher, ReactiveAuthenticationManager authenticationManager,
			ServerAuthenticationConverter serverAuthenticationConverter,
			ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
			ServerAuthenticationFailureHandler authenticationFailureHandler) {
		this.authMatcher = authMatcher;

		authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);

		authenticationWebFilter.setServerAuthenticationConverter(serverAuthenticationConverter);

		authenticationWebFilter.setAuthenticationSuccessHandler(serverAuthenticationSuccessHandler);

		/**
		 * 需要设置，默认使用的是HttpBasicServerAuthenticationEntryPoint
		 */
		authenticationWebFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String requestPath = exchange.getRequest().getURI().getPath();

		if (!authMatcher.isAuthPath(exchange)) {
			/**
			 * 不需要认证的请求只透传，不处理
			 */
			if (log.isDebugEnabled()) {
				log.debug("request path:{} not a AuthPath, ignore authentication", requestPath);
			}
			return chain.filter(exchange);
		}

		return authenticationWebFilter.filter(exchange, chain);
	}

}

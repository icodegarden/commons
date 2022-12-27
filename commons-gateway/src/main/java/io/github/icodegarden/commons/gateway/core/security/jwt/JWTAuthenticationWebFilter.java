package io.github.icodegarden.commons.gateway.core.security.jwt;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.gateway.spi.AuthWebFilter;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JWTAuthenticationWebFilter implements AuthWebFilter {

	private final AuthenticationWebFilter authenticationWebFilter;

	public JWTAuthenticationWebFilter(ReactiveAuthenticationManager authenticationManager,
			ServerAuthenticationConverter serverAuthenticationConverter,
			ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
			ServerAuthenticationFailureHandler authenticationFailureHandler) {
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
		return authenticationWebFilter.filter(exchange, chain);
	}

}

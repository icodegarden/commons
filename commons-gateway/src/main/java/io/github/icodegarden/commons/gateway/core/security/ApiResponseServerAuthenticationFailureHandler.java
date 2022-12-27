package io.github.icodegarden.commons.gateway.core.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;

import io.github.icodegarden.commons.springboot.security.ApiResponseServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ApiResponseServerAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

	private final ServerAuthenticationEntryPoint authenticationEntryPoint;

	public ApiResponseServerAuthenticationFailureHandler() {
		this.authenticationEntryPoint = new ApiResponseServerAuthenticationEntryPoint();
	}

	@Override
	public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
		return authenticationEntryPoint.commence(webFilterExchange.getExchange(), exception);
	}
}
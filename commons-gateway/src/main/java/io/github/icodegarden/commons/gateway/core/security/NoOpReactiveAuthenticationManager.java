package io.github.icodegarden.commons.gateway.core.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NoOpReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		/**
		 * 不校验，能生成就代表通过
		 */
		return Mono.just(authentication);
	}
}
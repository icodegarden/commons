package io.github.icodegarden.commons.gateway.core.security;

import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import io.github.icodegarden.commons.springboot.loadbalancer.FlowTagLoadBalancer;
import io.github.icodegarden.commons.springboot.security.User;
import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JWTAuthenticationWebFilter implements WebFilter {

	private final AuthenticationWebFilter authenticationWebFilter;

	public JWTAuthenticationWebFilter(JWTConfig jwtConfig, ServerAuthenticationEntryPoint authenticationEntryPoint) {
		authenticationWebFilter = new AuthenticationWebFilter(new NoOpReactiveAuthenticationManager());
		authenticationWebFilter
				.setServerAuthenticationConverter(new JWTResolveServerAuthenticationConverter(jwtConfig));
		authenticationWebFilter
				.setAuthenticationSuccessHandler(new GatewayPreAuthenticatedServerAuthenticationSuccessHandler());

		/**
		 * 需要设置，默认使用的是HttpBasicServerAuthenticationEntryPoint
		 */
		authenticationWebFilter.setAuthenticationFailureHandler(
				new ApiResponseServerAuthenticationFailureHandler(authenticationEntryPoint));
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return authenticationWebFilter.filter(exchange, chain);
	}

	private class NoOpReactiveAuthenticationManager implements ReactiveAuthenticationManager {
		@Override
		public Mono<Authentication> authenticate(Authentication authentication) {
			/**
			 * 不用校验，能生成就代表通过
			 */
			return Mono.just(authentication);
		}
	}

	private class JWTResolveServerAuthenticationConverter implements ServerAuthenticationConverter {
		private final JWTConfig jwtConfig;

		public JWTResolveServerAuthenticationConverter(JWTConfig jwtConfig) {
			this.jwtConfig = jwtConfig;
		}

		@Override
		public Mono<Authentication> convert(ServerWebExchange exchange) {
			return Mono.defer(() -> {
				ServerHttpRequest request = exchange.getRequest();

				String jwt = getJWT(request);

				if (StringUtils.hasText(jwt)) {
					try {
						JWTResolver jwtResolver = new JWTResolver(jwtConfig, jwt);
						Authentication authentication = jwtResolver.getAuthentication();
						return Mono.just(authentication);
					} catch (TokenExpiredException e) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE.getSub_code(),
								"Not Authenticated, Token Expired"));
					} catch (JWTDecodeException | SignatureVerificationException e) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE.getSub_code(),
								"Not Authenticated, Token Invalid"));
					} catch (JWTVerificationException e) {
						throw new AuthenticationServiceException("Verification Token Error", e);
					}

				}

				return Mono.empty();
			});
		}
	}

	private class ApiResponseServerAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

		private final ServerAuthenticationEntryPoint authenticationEntryPoint;

		public ApiResponseServerAuthenticationFailureHandler(ServerAuthenticationEntryPoint authenticationEntryPoint) {
			this.authenticationEntryPoint = authenticationEntryPoint;
		}

		@Override
		public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
				AuthenticationException exception) {
			return authenticationEntryPoint.commence(webFilterExchange.getExchange(), exception);
		}
	}

	private class GatewayPreAuthenticatedServerAuthenticationSuccessHandler
			implements ServerAuthenticationSuccessHandler {

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
						String flowTag = (String) details.get("flowTag");
						httpHeaders.add(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG, flowTag);
					}
				}).build();

				return chain.filter(exchange.mutate().request(request).build());
			});
		}
	}

	private String getJWT(ServerHttpRequest request) {
		String bearerToken = request.getHeaders().getFirst(WebUtils.AUTHORIZATION_HEADER);
		if (bearerToken != null) {
			return resolveBearerToken(bearerToken, " ");
		}
		return null;
	}

	private String resolveBearerToken(String bearerToken, @Nullable String concat) {
		if (concat == null) {
			concat = " ";
		}
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer" + concat)) {
			String originToken = bearerToken.substring(7, bearerToken.length());
			return originToken;
		}
		return null;
	}
}

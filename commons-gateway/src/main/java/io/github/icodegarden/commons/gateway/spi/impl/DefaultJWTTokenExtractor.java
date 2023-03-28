package io.github.icodegarden.commons.gateway.spi.impl;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.gateway.spi.JWTTokenExtractor;
import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultJWTTokenExtractor implements JWTTokenExtractor {

	@Override
	public String extract(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();

		String jwt = getJWT(request);
		return jwt;
	}

	private String getJWT(ServerHttpRequest request) {
		String bearerToken = request.getHeaders().getFirst(WebUtils.HEADER_AUTHORIZATION);
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

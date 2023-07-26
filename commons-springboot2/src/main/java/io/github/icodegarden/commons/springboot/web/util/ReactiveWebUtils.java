package io.github.icodegarden.commons.springboot.web.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ReactiveWebUtils extends BaseWebUtils {
	private ReactiveWebUtils() {
	}

	private static final ThreadLocal<ServerWebExchange> SERVERWEBEXCHANGE_HOLDER = new ThreadLocal<>();

	public static ServerWebExchange getExchange() {
		return SERVERWEBEXCHANGE_HOLDER.get();
	}

	public static void setExchange(ServerWebExchange exchange) {
		SERVERWEBEXCHANGE_HOLDER.set(exchange);
	}

	public static String getJWT() {
		String bearerToken = getAuthorizationToken();
		if (bearerToken != null) {
			return resolveBearerToken(bearerToken, " ");
		}
		return null;
	}

	public static void setJWT(String jwt) {
		ServerWebExchange request = getExchange();
		if (request != null) {
			String bearerToken = createBearerToken(jwt, " ");
			request.getAttributes().put(HEADER_AUTHORIZATION, bearerToken);// use for rpc;
		}
	}

	public static String getBasicAuthorizationToken() {
		String basicToken = getAuthorizationToken();
		if (basicToken != null) {
			return resolveBasicToken(basicToken, " ");
		}
		return null;
	}

	public static String getAuthorizationToken() {
		ServerWebExchange request = getExchange();
		if (request == null) {
			return null;
		}
		String authorizationToken = (String) request.getAttribute(HEADER_AUTHORIZATION);
		return authorizationToken != null ? authorizationToken
				: request.getRequest().getHeaders().getFirst(HEADER_AUTHORIZATION);
	}

	public static boolean isInternalRpc() {
		ServerWebExchange request = getExchange();
		if (request == null) {
			return false;
		}

		String header = request.getRequest().getHeaders().getFirst(HEADER_INTERNAL_RPC);
		return header != null && Boolean.valueOf(header);
	}

	public static String getRequestId() {
		ServerWebExchange request = getExchange();
		if (request == null) {
			return null;
		}

		return request.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
	}

	public static void responseJWT(String jwt, ServerWebExchange exchange) {
		String bearerToken = createBearerToken(jwt, " ");
		exchange.getResponse().getHeaders().set(HEADER_AUTHORIZATION, bearerToken);
	}

	public static void responseWrite(int status, String body, ServerWebExchange exchange) throws IOException {
		Assert.hasText(body, "body must not empty");
		responseWrite(status, null, body, exchange);
	}

	public static void responseWrite(int status, List<Tuple2<String, List<String>>> headers, ServerWebExchange exchange)
			throws IOException {
		Assert.notEmpty(headers, "headers must not empty");
		responseWrite(status, headers, null, exchange);
	}

	public static void responseWrite(int status, @Nullable List<Tuple2<String, List<String>>> headers,
			@Nullable String body, ServerWebExchange exchange) throws IOException {
		exchange.getResponse().setRawStatusCode(status);
		if (headers != null && !headers.isEmpty()) {
			for (Tuple2<String, List<String>> header : headers) {
				for (String value : header.getT2()) {
					exchange.getResponse().getHeaders().add(header.getT1(), value);
				}
			}
		}

		if (body == null) {
			body = "";
		}

		exchange.getResponse().getHeaders().set("Content-Type", "application/json;charset=utf-8");
		DefaultDataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance
				.wrap(body.getBytes(StandardCharsets.UTF_8));
		exchange.getResponse().writeWith(Mono.just(dataBuffer));
	}
}

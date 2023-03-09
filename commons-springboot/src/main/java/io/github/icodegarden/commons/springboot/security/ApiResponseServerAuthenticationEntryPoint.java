package io.github.icodegarden.commons.springboot.security;

import java.nio.charset.Charset;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.lang.spec.response.ApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * InternalApi/OpenApi认证失败时的统一响应
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class ApiResponseServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

	private static final Charset CHARSET = Charset.forName("utf-8");

	private final ApiResponseBuilder builder;

	public ApiResponseServerAuthenticationEntryPoint() {
		this.builder = new DefaultApiResponseBuilder();
	}

	public ApiResponseServerAuthenticationEntryPoint(ApiResponseBuilder builder) {
		this.builder = builder;
	}

	public static interface ApiResponseBuilder {
		ApiResponse build(ServerWebExchange exchange, ErrorCodeException ece);
	}

	private class DefaultApiResponseBuilder implements ApiResponseBuilder {
		@Override
		public ApiResponse build(ServerWebExchange exchange, ErrorCodeException ece) {
			return InternalApiResponse.fail(ece);
		}
	}

	/**
	 * 认证失败时
	 */
	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
		if (exchange.getResponse().isCommitted()) {
			return Mono.empty();
		}

		return Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap((response) -> {
			response.setStatusCode(HttpStatus.OK);
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			DataBufferFactory dataBufferFactory = response.bufferFactory();

			String message = (e.getMessage() != null ? e.getMessage() : "Not Authenticated.");

			if (log.isInfoEnabled()) {
				String path = null;
				try {
					path = exchange.getRequest().getPath().toString();
				} catch (Exception ex) {
					log.error("ex on get request path", ex);
				}

				log.info("path of {} request Authentication failed:{}", path, message);
			}

			ErrorCodeException ece;
			if (e instanceof ErrorCodeAuthenticationException) {
				ece = ((ErrorCodeAuthenticationException) e).getErrorCodeException();
			} else if (e instanceof AuthenticationServiceException) {
				/**
				 * 500类型
				 */
				ece = new ServerErrorCodeException("Authentication", message, e);
			} else {
				ece = new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE.getSub_code(), message);
			}

			ApiResponse apiResponse = builder.build(exchange, ece);

			DataBuffer buffer = dataBufferFactory.wrap(JsonUtils.serialize(apiResponse).getBytes(CHARSET));
			return response.writeWith(Mono.just(buffer)).doOnError((error) -> DataBufferUtils.release(buffer));
		});
	}
}

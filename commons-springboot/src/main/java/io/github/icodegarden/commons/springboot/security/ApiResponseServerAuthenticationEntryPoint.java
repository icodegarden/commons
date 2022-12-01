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

import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class ApiResponseServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

	private static final Charset CHARSET = Charset.forName("utf-8");

	/**
	 * 认证失败时
	 */
	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
		return Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap((response) -> {
			response.setStatusCode(HttpStatus.OK);
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			DataBufferFactory dataBufferFactory = response.bufferFactory();

			String message = (e.getMessage() != null ? e.getMessage() : "Not Authenticated.");

			if (log.isInfoEnabled()) {
				log.info("request Authentication failed:{}", message);
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

			InternalApiResponse<Object> apiResponse = InternalApiResponse.fail(ece);

			DataBuffer buffer = dataBufferFactory.wrap(JsonUtils.serialize(apiResponse).getBytes(CHARSET));
			return response.writeWith(Mono.just(buffer)).doOnError((error) -> DataBufferUtils.release(buffer));
		});
	}
}

package io.github.icodegarden.commons.springboot.security;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.lang.spec.response.ClientPermissionErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ApiResponseServerAccessDeniedHandler implements ServerAccessDeniedHandler {
	private static final Logger log = LoggerFactory.getLogger(ApiResponseServerAuthenticationEntryPoint.class);

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
		return Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap((response) -> {
			response.setStatusCode(HttpStatus.OK);
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

			String message = "Access Denied, Not Authorized.";

			if (log.isInfoEnabled()) {
				log.info("request {}", message);
			}

			ClientPermissionErrorCodeException ece = new ClientPermissionErrorCodeException(
					ClientPermissionErrorCodeException.SubPair.INSUFFICIENT_PERMISSIONS.getSub_code(), message);
			InternalApiResponse<Object> apiResponse = InternalApiResponse.fail(ece);

			DataBufferFactory dataBufferFactory = response.bufferFactory();
			DataBuffer buffer = dataBufferFactory
					.wrap(JsonUtils.serialize(apiResponse).getBytes(Charset.forName("utf-8")));
			return response.writeWith(Mono.just(buffer)).doOnError((error) -> DataBufferUtils.release(buffer));
		});
	}

}

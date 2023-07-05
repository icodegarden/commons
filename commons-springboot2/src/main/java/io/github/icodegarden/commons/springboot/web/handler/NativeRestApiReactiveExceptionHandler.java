package io.github.icodegarden.commons.springboot.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;

/**
 * 使用 @Bean
 * 
 * @author Fangfang.Xu
 *
 */
public class NativeRestApiReactiveExceptionHandler extends AbstractReactiveExceptionHandler<String> {

	private static final Logger log = LoggerFactory.getLogger(NativeRestApiReactiveExceptionHandler.class);

	@Override
	public ResponseEntity<String> onServerWebInputException(ServerWebExchange exchange, ServerWebInputException cause)
			throws Exception {
		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400).body("Missing Path Variable:" + cause.getReason());
	}

	@Override
	public ResponseEntity<String> onException(ServerWebExchange exchange, Exception cause) {
		ErrorCodeException ece = convertErrorCodeException(cause);

		return ResponseEntity.status(ece.httpStatus()).body(ece.getMessage());
	}
}
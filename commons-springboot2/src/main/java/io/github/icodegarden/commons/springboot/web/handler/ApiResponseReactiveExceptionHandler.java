package io.github.icodegarden.commons.springboot.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import io.github.icodegarden.commons.lang.spec.response.ApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterMissingErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;

/**
 * 使用 @Bean
 * 
 * @author Fangfang.Xu
 *
 */
public class ApiResponseReactiveExceptionHandler extends AbstractReactiveExceptionHandler<ApiResponse> {

	private static final Logger log = LoggerFactory.getLogger(ApiResponseReactiveExceptionHandler.class);

	@Override
	public ResponseEntity<ApiResponse> onServerWebInputException(ServerWebExchange exchange,
			ServerWebInputException cause) throws Exception {

		/**
		 * spring是这样抛出异常的，例如在
		 * RequestParamMethodArgumentResolver.handleMissingValue(String,
		 * MethodParameter, ServerWebExchange)
		 */
//		String type = parameter.getNestedParameterType().getSimpleName();
//		String reason = "Required " + type + " parameter '" + name + "' is not present";
//		throw new ServerWebInputException(reason, parameter);

		ErrorCodeException ece = null;

		String reason = cause.getReason();
		if (reason != null) {
			if (reason.startsWith("Required") || reason.startsWith("Missing")) {
				ece = new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_PARAMETER.getSub_code(), reason);
			}
		}

		if (ece == null) {
			ece = new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(), reason);
		}

		if (log.isWarnEnabled()) {
			log.warn("{} {}", PARAMETER_INVALID_LOG_MODULE, ece.getMessage(), cause);
		}

		/**
		 * 一律使用OpenApiResponse来构造即可<br>
		 * biz_code会被gateway补充，那里有BodyCache<br>
		 */
		return ResponseEntity.ok(OpenApiResponse.fail(null, ece));
	}

	@Override
	public ResponseEntity<ApiResponse> onException(ServerWebExchange exchange, Exception cause) {
//		OpenApiRequestBody body = extractOpenApiRequestBody(request);

		ErrorCodeException ece = convertErrorCodeException(cause);

//		if (body != null) {
//			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
//		}
//
//		return ResponseEntity.ok(InternalApiResponse.fail(ece));

		/**
		 * 一律使用OpenApiResponse来构造即可<br>
		 * biz_code会被gateway补充，那里有BodyCache<br>
		 */
		return ResponseEntity.ok(OpenApiResponse.fail(null, ece));
	}

}
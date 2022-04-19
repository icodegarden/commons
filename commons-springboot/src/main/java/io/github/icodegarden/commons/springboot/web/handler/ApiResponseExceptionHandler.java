package io.github.icodegarden.commons.springboot.web.handler;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import io.github.icodegarden.commons.lang.spec.response.ApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterMissingErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 使用 @Bean
 * 
 * @author Fangfang.Xu
 *
 */
public class ApiResponseExceptionHandler extends AbstractExceptionHandler<ApiResponse> {

	private static final Logger log = LoggerFactory.getLogger(ApiResponseExceptionHandler.class);

	@Override
	public ResponseEntity<ApiResponse> onParameterMissing(HttpServletRequest request,
			MissingServletRequestParameterException cause) throws Exception {
		ErrorCodeException ece = new ClientParameterMissingErrorCodeException(
				ClientParameterMissingErrorCodeException.SubPair.MISSING_PARAMETER.getSub_code(),
				"parameter:" + cause.getParameterName());
		if (log.isWarnEnabled()) {
			log.warn("{} {}", PARAMETER_INVALID_LOG_MODULE, ece.toString(), cause);
		}
		OpenApiRequestBody body = extractOpenApiRequestBody(request);
		if (body != null) {
			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
		}

		return ResponseEntity.ok(InternalApiResponse.fail(ece));
	}

	@Override
	public ResponseEntity<ApiResponse> onParameterTypeInvalid(HttpServletRequest request,
			MethodArgumentTypeMismatchException cause) {
		ErrorCodeException ece = new ClientParameterInvalidErrorCodeException(
				ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
				"parameter:" + cause.getName()/* 对应的字段名 */);
		if (log.isWarnEnabled()) {
			log.warn("{} {}", PARAMETER_INVALID_LOG_MODULE, ece.toString(), cause);
		}
		OpenApiRequestBody body = extractOpenApiRequestBody(request);
		if (body != null) {
			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
		}

		return ResponseEntity.ok(InternalApiResponse.fail(ece));
	}

	@Override
	public ResponseEntity<ApiResponse> onBodyParameterMissing(HttpServletRequest request,
			MethodArgumentNotValidException cause) {
		ErrorCodeException ece;
		if (cause.getBindingResult().hasErrors()) {
			List<ObjectError> allErrors = cause.getBindingResult().getAllErrors();
			String subMsg = allErrors.stream().findFirst().map(error -> {
				if (error instanceof FieldError) {
					String field = ((FieldError) error).getField();
					return "parameter:" + field;
				}
				return cause.getMessage();
			}).get();

			ece = new ClientParameterMissingErrorCodeException(
					ClientParameterMissingErrorCodeException.SubPair.MISSING_PARAMETER.getSub_code(), subMsg);
		} else {
			ece = new ClientParameterMissingErrorCodeException(
					ClientParameterMissingErrorCodeException.SubPair.MISSING_PARAMETER.getSub_code(),
					cause.getMessage());
		}
		if (log.isWarnEnabled()) {
			log.warn("{} {}", PARAMETER_INVALID_LOG_MODULE, ece.toString(), cause);
		}

		OpenApiRequestBody body = extractOpenApiRequestBody(request);
		if (body != null) {
			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
		}

		return ResponseEntity.ok(InternalApiResponse.fail(ece));
	}

	@Override
	public ResponseEntity<ApiResponse> onBodyParameterTypeInvalid(HttpServletRequest request,
			HttpMessageNotReadableException cause) {
		/**
		 * 由于类型不匹配，这种情况实际上是 JSON parse error
		 */

		ErrorCodeException ece = new ClientParameterInvalidErrorCodeException(
				ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
				"Invalid:json-field-type");
		if (log.isWarnEnabled()) {
			log.warn("{} {}", PARAMETER_INVALID_LOG_MODULE, ece.toString(), cause);
		}
		OpenApiRequestBody body = extractOpenApiRequestBody(request);
		if (body != null) {
			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
		}

		return ResponseEntity.ok(InternalApiResponse.fail(ece));
	}

	@Override
	public ResponseEntity<ApiResponse> onException(HttpServletRequest request, Exception cause) {
		ServerErrorCodeException ece = new ServerErrorCodeException(cause);
		if (log.isWarnEnabled()) {
			log.warn("{} {}", EXCEPTION_LOG_MODULE, ece.toString(), cause);
		}
		OpenApiRequestBody body = extractOpenApiRequestBody(request);
		if (body != null) {
			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
		}

		return ResponseEntity.ok(InternalApiResponse.fail(ece));
	}

	/**
	 * 
	 * @return Nullable
	 */
	private OpenApiRequestBody extractOpenApiRequestBody(HttpServletRequest request) {
		if (request != null && request instanceof ContentCachingRequestWrapper) {
			ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
			String content;
			try {
				content = new String(wrapper.getContentAsByteArray(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
			OpenApiRequestBody body = JsonUtils.deserialize(content, OpenApiRequestBody.class);
			if (StringUtils.hasText(body.getSign())) {
				return body;
			}
		}
		return null;
	}
}
package io.github.icodegarden.commons.springboot.web.handler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;

/**
 * 使用 @Bean
 * 
 * @author Fangfang.Xu
 *
 */
public class NativeRestApiExceptionHandler extends AbstractExceptionHandler<String> {

	private static final Logger log = LoggerFactory.getLogger(NativeRestApiExceptionHandler.class);

	@Override
	public ResponseEntity<String> onPathVariableMissing(HttpServletRequest request, MissingPathVariableException cause)
			throws Exception {
		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400).body("Missing Path Variable:" + cause.getVariableName());
	}

	@Override
	public ResponseEntity<String> onRequestHeaderMissing(HttpServletRequest request,
			MissingRequestHeaderException cause) throws Exception {
		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400).body("Missing Header:" + cause.getHeaderName());
	}

	@Override
	public ResponseEntity<String> onRequestCookieMissing(HttpServletRequest request,
			MissingRequestCookieException cause) throws Exception {
		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400).body("Missing Cookie:" + cause.getCookieName());
	}

	@Override
	public ResponseEntity<String> onParameterMissing(HttpServletRequest request,
			MissingServletRequestParameterException cause) throws Exception {
		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400).body("Missing:" + cause.getParameterName());
	}

	@Override
	public ResponseEntity<String> onParameterTypeInvalid(HttpServletRequest request,
			MethodArgumentTypeMismatchException cause) {
		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400).body("Invalid:" + cause.getName());
	}

	@Override
	public ResponseEntity<String> onBodyParameterInvalid(HttpServletRequest request,
			MethodArgumentNotValidException cause) {
		String subMsg;
		if (cause.getBindingResult().hasErrors()) {
			List<ObjectError> allErrors = cause.getBindingResult().getAllErrors();
			subMsg = allErrors.stream().findFirst().map(error -> {
				if (error instanceof FieldError) {
					String field = ((FieldError) error).getField();
					return "parameter:" + field;
				}
				return cause.getMessage();
			}).get();

		} else {
			subMsg = cause.getMessage();
		}
		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400)
				.body(ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER + ":" + subMsg);
	}

	@Override
	public ResponseEntity<String> onBodyParameterTypeInvalid(HttpServletRequest request,
			HttpMessageNotReadableException cause) {
		/**
		 * 由于类型不匹配，这种情况实际上是 JSON parse error
		 */

		if (log.isWarnEnabled()) {
			log.warn("{}", PARAMETER_INVALID_LOG_MODULE, cause);
		}
		return ResponseEntity.status(400).body("Invalid:json-field-type");
	}

	@Override
	public ResponseEntity<String> onException(HttpServletRequest request, Exception cause) {
		ErrorCodeException ece = convertErrorCodeException(cause);

		return ResponseEntity.status(ece.httpStatus()).body(ece.getMessage());
	}
}
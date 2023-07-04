package io.github.icodegarden.commons.springboot.web.handler;

import java.lang.reflect.UndeclaredThrowableException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterMissingErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;

/**
 * 使用 @Bean <br>
 * 
 * @author Fangfang.Xu
 *
 */
@ControllerAdvice
public abstract class AbstractExceptionHandler<T> {
	private static final Logger log = LoggerFactory.getLogger(AbstractExceptionHandler.class);

	protected static final String PARAMETER_INVALID_LOG_MODULE = "Parameter-Invalid";
	protected static final String EXCEPTION_LOG_MODULE = "Service Currently Unavailable";

	protected boolean printErrorStackOnWarn = true;

	public void setPrintErrorStackOnWarn(boolean printErrorStackOnWarn) {
		this.printErrorStackOnWarn = printErrorStackOnWarn;
	}

	/**
	 * spring参数缺失，不会进aop
	 * 
	 * @param cause
	 * @return
	 * @throws Exception
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public abstract ResponseEntity<T> onParameterMissing(HttpServletRequest request,
			MissingServletRequestParameterException cause) throws Exception;

	/**
	 * spring参数类型错误，不会进aop
	 * 
	 * @param cause
	 * @return
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public abstract ResponseEntity<T> onParameterTypeInvalid(HttpServletRequest request,
			MethodArgumentTypeMismatchException cause);

	/**
	 * body spring参数缺失，不会进aop
	 * 
	 * @param cause
	 * @return
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public abstract ResponseEntity<T> onBodyParameterInvalid(HttpServletRequest request,
			MethodArgumentNotValidException cause);

	/**
	 * body spring参数类型错误，不会进aop
	 * 
	 * @param cause
	 * @return
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public abstract ResponseEntity<T> onBodyParameterTypeInvalid(HttpServletRequest request,
			HttpMessageNotReadableException cause);

	/**
	 * method不匹配，这种情况属于客户端用错了method，不做处理直接按原生响应
	 * 
	 * @param cause
	 * @return
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<T> onMethodNotSupported(HttpServletRequest request,
			HttpRequestMethodNotSupportedException cause) throws Exception {
		throw cause;
	}

	/**
	 * 其他错误，包含业务异常
	 * 
	 * @param cause
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public abstract ResponseEntity<T> onException(HttpServletRequest request, Exception cause);

	protected ErrorCodeException convertErrorCodeException(Exception e) {
		ErrorCodeException ece = null;
		if (e instanceof ErrorCodeException) {
			ece = (ErrorCodeException) e;
		} else {
			/**
			 * 可能的Client ex
			 */
			if (e instanceof IllegalArgumentException) {
				String eMessage = e.getMessage();

				if (StringUtils.hasText(eMessage)) {
					if (ClientParameterInvalidErrorCodeException.KEYWORDS.stream()
							.anyMatch(keyword -> eMessage.startsWith(keyword))) {
						ece = new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
								e.getMessage());
					} else if (ClientParameterMissingErrorCodeException.KEYWORDS.stream()
							.anyMatch(keyword -> eMessage.startsWith(keyword))) {
						ece = new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_PARAMETER.getSub_code(),
								e.getMessage());
					}
				}
			}

			/**
			 * 其他的一律视为 服务异常
			 */
			if (ece == null) {
				ece = causeErrorCodeException(e);
				if (ece == null) {
					ece = new ServerErrorCodeException(e);
				}
			}
		}

		if (ece instanceof ServerErrorCodeException) {
			log.error("{} ex on handle request", EXCEPTION_LOG_MODULE, ece);
		} else {
			if (log.isWarnEnabled()) {
				if (printErrorStackOnWarn) {
					log.warn("request has a Client Exception:{}", ece.getMessage(), ece);
				} else {
					log.warn("request has a Client Exception:{}", ece.getMessage());
				}
			}
		}

		return ece;
	}

	private ErrorCodeException causeErrorCodeException(Throwable e) {
		int counter = 0;
		while (e != null && counter++ < 10 && !(e instanceof ErrorCodeException)) {
			if (e instanceof UndeclaredThrowableException) {
				e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
			} else if (e instanceof org.springframework.cglib.proxy.UndeclaredThrowableException) {
				e = ((org.springframework.cglib.proxy.UndeclaredThrowableException) e).getUndeclaredThrowable();
			} else {
				e = e.getCause();
			}
		}
		if (e != null && e instanceof ErrorCodeException) {
			return (ErrorCodeException) e;
		}
		return null;
	}

	/**
	 * @return Nullable
	 */
//	@Deprecated //使用该方法判断有性能损失
//	protected OpenApiRequestBody extractOpenApiRequestBody(HttpServletRequest request) {
//		if (request != null && request instanceof ContentCachingRequestWrapper) {
//			ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
//
//			byte[] bs = wrapper.getContentAsByteArray();
//			if (bs == null || bs.length == 0) {
//				return null;
//			}
//
//			String content;
//			try {
//				content = new String(bs, "utf-8");
//			} catch (UnsupportedEncodingException e) {
//				log.error("WARN ex on extractOpenApiRequestBody convert String", e);
//				return null;
//			}
//
//			if (!StringUtils.hasText(content) || !content.startsWith("{") || !content.endsWith("}")) {
//				return null;
//			}
//
//			try {
//				OpenApiRequestBody body = JsonUtils.deserialize(content, OpenApiRequestBody.class);
//				if (StringUtils.hasText(body.getSign())) {
//					return body;
//				}
//			} catch (Exception e) {
//				log.error("WARN ex on extractOpenApiRequestBody deserialize", e);
//				return null;
//			}
//		}
//		return null;
//	}
}
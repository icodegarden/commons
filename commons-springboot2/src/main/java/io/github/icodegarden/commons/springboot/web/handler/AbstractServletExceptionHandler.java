package io.github.icodegarden.commons.springboot.web.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 使用 @Bean <br>
 * 
 * @author Fangfang.Xu
 *
 */
@ControllerAdvice
public abstract class AbstractServletExceptionHandler<T> extends BaseExceptionHandler {

	@ExceptionHandler(MissingPathVariableException.class)
	public abstract ResponseEntity<T> onPathVariableMissing(HttpServletRequest request,
			MissingPathVariableException cause) throws Exception;

	@ExceptionHandler(MissingRequestHeaderException.class)
	public abstract ResponseEntity<T> onRequestHeaderMissing(HttpServletRequest request,
			MissingRequestHeaderException cause) throws Exception;

	@ExceptionHandler(MissingRequestCookieException.class)
	public abstract ResponseEntity<T> onRequestCookieMissing(HttpServletRequest request,
			MissingRequestCookieException cause) throws Exception;

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

}
package io.github.icodegarden.commons.springboot.web.handler;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * 使用 @Bean
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration//为了对@Bean contentCachingFilter起作用
@ControllerAdvice
public abstract class AbstractExceptionHandler<T> {

	protected static final String PARAMETER_INVALID_LOG_MODULE = "Parameter-Invalid";
	protected static final String EXCEPTION_LOG_MODULE = "Service Currently Unavailable";

	@Bean
	public FilterRegistrationBean<Filter> contentCachingFilter() {
		OncePerRequestFilter filter = new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				filterChain.doFilter(new ContentCachingRequestWrapper(request), response);
			}
		};

		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
		bean.setFilter(filter);
		bean.setName("contentCaching");
		bean.addUrlPatterns("/*");

		return bean;
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
	public abstract ResponseEntity<T> onBodyParameterMissing(HttpServletRequest request,
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
	 * 其他错误，包含业务异常
	 * 
	 * @param cause
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public abstract ResponseEntity<T> onException(HttpServletRequest request, Exception cause);

}
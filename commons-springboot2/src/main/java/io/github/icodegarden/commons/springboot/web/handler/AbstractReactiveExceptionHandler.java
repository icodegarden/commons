package io.github.icodegarden.commons.springboot.web.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

/**
 * 使用 @Bean <br>
 * 
 * 之所以需要单独的这个抽象类，是因为webflux的异常类型与webmvc不兼容，webmvc的那套异常全部需要依赖servlet库
 * 
 * @author Fangfang.Xu
 *
 */
@ControllerAdvice
public abstract class AbstractReactiveExceptionHandler<T> extends BaseExceptionHandler {

	@ExceptionHandler(ServerWebInputException.class)
	public abstract ResponseEntity<T> onServerWebInputException(ServerWebExchange exchange,
			ServerWebInputException cause) throws Exception;

	/**
	 * 其他错误，包含业务异常
	 * 
	 * @param cause
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public abstract ResponseEntity<T> onException(ServerWebExchange exchange, Exception cause);

}
package io.github.icodegarden.commons.springboot.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.ResponseEntity;

import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;

/**
 * 使用@Bean
 * 
 * @author Fangfang.Xu
 *
 */
@Deprecated
@Aspect
@EnableAspectJAutoProxy
public class NativeRestApiTransferAspect extends AbstractTransferAspect {

	private static final Logger log = LoggerFactory.getLogger(NativeRestApiTransferAspect.class);

	@Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
	public void pointcut() {
	}

	@Around("pointcut()")
	public Object doInvoke(ProceedingJoinPoint pjp) throws Throwable {
		Signature signature = pjp.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		Method targetMethod = methodSignature.getMethod();
		Class<?> returnType = targetMethod.getReturnType();
		/**
		 * 约定返回类型必须是ResponseEntity<T>
		 */
		if (ResponseEntity.class.equals(returnType)) {
			ErrorCodeException ece;
			try {
				return pjp.proceed();
			} catch (ServerErrorCodeException e) {
				log.error("ex of ServerErrorCodeException on handle request", e);
				ece = e;
			} catch (IllegalArgumentException e) {
				if (log.isWarnEnabled()) {
					if (printErrorStackOnWarn) {
						log.warn("request has a Client Exception:{}", e.getMessage(), e);
					} else {
						log.warn("request has a Client Exception:{}", e.getMessage());
					}
				}
				ece = new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
						e.getMessage());
			} catch (ErrorCodeException e) {
				if (log.isWarnEnabled()) {
					if (printErrorStackOnWarn) {
						log.warn("request has a Client Exception:{}", e.getMessage(), e);
					} else {
						log.warn("request has a Client Exception:{}", e.getMessage());
					}
				}
				ece = e;
			} catch (Throwable e) {
				log.error("ex on handle request", e);
				ece = causeErrorCodeException(e);
				if (ece == null) {
					ece = new ServerErrorCodeException(e);
				}
			}
			return ResponseEntity.status(ece.httpStatus()).body(ece.getSub_msg());
		}

		throw new IllegalArgumentException(String.format(
				"rest api of method:%s return type must be ResponseEntity, current:%s", targetMethod, returnType));
	}

}
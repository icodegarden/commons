package io.github.icodegarden.commons.springboot.aop;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;

/**
 * 使用@Bean
 * 
 * @author Fangfang.Xu
 *
 */
@Deprecated
@Aspect
@EnableAspectJAutoProxy
@SuppressWarnings("rawtypes")
public class ApiResponseTransferAspect extends AbstractTransferAspect {

	private static final Logger log = LoggerFactory.getLogger(ApiResponseTransferAspect.class);

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
			Type t1 = targetMethod.getGenericReturnType();
			if (t1 instanceof ParameterizedType) {// 当返回是ResponseEntity没有泛型时，t1==Class不是ParameterizedType
				// org.springframework.http.ResponseEntity<T>
				ParameterizedType pt = (ParameterizedType) t1;
				// T
				Type t2 = pt.getActualTypeArguments()[0];

				if (isTypeOf(t2, InternalApiResponse.class)) {
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
					return ResponseEntity.ok(InternalApiResponse.fail(ece));
				} else if (isTypeOf(t2, OpenApiResponse.class)) {
					OpenApiRequestBody body = null;

					Object[] args = pjp.getArgs();
					for (Object arg : args) {
						Class<?> parameterType = arg.getClass();
						if (OpenApiRequestBody.class.isAssignableFrom(parameterType)) {
							body = (OpenApiRequestBody) arg;
							break;
						}
					}

					if (body == null) {
						throw new IllegalArgumentException(String.format(
								"openapi of method:%s must has request body of OpenApiRequestBody", targetMethod));
					}

					ErrorCodeException ece;
					try {
						return pjp.proceed();
					} catch (ServerErrorCodeException e) {
						log.error("ex of ServerErrorCodeException on handle request, request body:{}", body, e);
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
								log.warn("request has a Client Exception:{}, request body:{}", e.getMessage(), body, e);
							} else {
								log.warn("request has a Client Exception:{}, request body:{}", e.getMessage(), body);
							}
						}
						ece = e;
					} catch (Throwable e) {
						log.error("ex on handle request, request body:{}", body, e);
						ece = causeErrorCodeException(e);
						if (ece == null) {
							ece = new ServerErrorCodeException(e);
						}
					}
					return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
				} else {
					throw new IllegalArgumentException(
							String.format("rest api of method:%s ParameterizedType must be ApiResponse, current:%s",
									targetMethod, t2));
				}
			}
		}

		throw new IllegalArgumentException(String.format(
				"rest api of method:%s return type must be ResponseEntity, current:%s", targetMethod, returnType));
	}

	/**
	 * 
	 * @param type   需要比较的对象
	 * @param typeOf 目标类型
	 * @return
	 */
	private boolean isTypeOf(Type type, Class<?> typeOf) {
		if (type instanceof Class && typeOf.isAssignableFrom((Class) type)) {
			return true;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType pt2 = (ParameterizedType) type;
			Class rawType = (Class) pt2.getRawType();
			if (typeOf.isAssignableFrom(rawType)) {
				return true;
			}
		}
		return false;
	}

}
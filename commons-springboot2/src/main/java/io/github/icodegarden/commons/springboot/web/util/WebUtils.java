package io.github.icodegarden.commons.springboot.web.util;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.util.ClassUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class WebUtils extends BaseWebUtils {

	private static final Method GET_HEADERNAMES_METHOD;
	private static final Method GET_HEADER_METHOD;
	private static final Method GET_JWT_METHOD;
	private static final Method SET_JWT_METHOD;
	private static final Method GET_BASIC_AUTHORIZATIONTOKEN_METHOD;
	private static final Method GET_AUTHORIZATIONTOKEN_METHOD;
	private static final Method IS_APIRPC_METHOD;
	private static final Method IS_OPENAPIRPC_METHOD;
	private static final Method IS_INTERNALRPC_METHOD;
	private static final Method GET_REQUESTID_METHOD;

	static {
//		/**
//		 * 有webflux，且没有webmvc <br>
//		 * 
//		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
//		 */
//		@ConditionalOnClass({ DispatcherHandler.class })
//		@ConditionalOnMissingClass({ "org.springframework.web.servlet.DispatcherServlet",
//				"org.glassfish.jersey.servlet.ServletContainer" })

		Class<?> utilClazz;
		if (ClassUtils.isPresent("org.springframework.web.reactive.DispatcherHandler", null)
				&& !ClassUtils.isPresent("org.springframework.web.servlet.DispatcherServlet", null)
				&& !ClassUtils.isPresent("org.glassfish.jersey.servlet.ServletContainer", null)) {
			utilClazz = ReactiveWebUtils.class;
		} else {
			utilClazz = WebUtils.class;
		}
		try {
			GET_HEADERNAMES_METHOD = utilClazz.getDeclaredMethod("getHeaderNames");
			GET_HEADER_METHOD = utilClazz.getDeclaredMethod("getHeader", String.class);
			GET_JWT_METHOD = utilClazz.getDeclaredMethod("getJWT");
			SET_JWT_METHOD = utilClazz.getDeclaredMethod("setJWT", String.class);
			GET_BASIC_AUTHORIZATIONTOKEN_METHOD = utilClazz.getDeclaredMethod("getBasicAuthorizationToken");
			GET_AUTHORIZATIONTOKEN_METHOD = utilClazz.getDeclaredMethod("getAuthorizationToken");
			IS_APIRPC_METHOD = utilClazz.getDeclaredMethod("isApiRpc");
			IS_OPENAPIRPC_METHOD = utilClazz.getDeclaredMethod("isOpenapiRpc");
			IS_INTERNALRPC_METHOD = utilClazz.getDeclaredMethod("isInternalRpc");
			GET_REQUESTID_METHOD = utilClazz.getDeclaredMethod("getRequestId");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private WebUtils() {
	}

	public static List<String> getHeaderNames() {
		try {
			return (List) GET_HEADERNAMES_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static String getHeader(String name) {
		try {
			return (String) GET_HEADER_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static String getJWT() {
		try {
			return (String) GET_JWT_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static void setJWT(String jwt) {
		try {
			SET_JWT_METHOD.invoke(null, jwt);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static String getBasicAuthorizationToken() {
		try {
			return (String) GET_BASIC_AUTHORIZATIONTOKEN_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static String getAuthorizationToken() {
		try {
			return (String) GET_AUTHORIZATIONTOKEN_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean isApiRpc() {
		try {
			return (boolean) IS_APIRPC_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean isOpenapiRpc() {
		try {
			return (boolean) IS_OPENAPIRPC_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean isInternalRpc() {
		try {
			return (boolean) IS_INTERNALRPC_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static String getRequestId() {
		try {
			return (String) GET_REQUESTID_METHOD.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}

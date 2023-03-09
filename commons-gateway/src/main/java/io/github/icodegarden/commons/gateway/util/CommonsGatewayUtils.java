package io.github.icodegarden.commons.gateway.util;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.gateway.core.security.signature.App;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.spec.sign.AppKeySignUtils;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.spec.sign.RSASignUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CommonsGatewayUtils {

	private static final String CACHED_APP_ATTR = "cachedApp";

	/**
	 * @return 当被全局filter设置为缓存时有
	 */
	public static @Nullable OpenApiRequestBody getOpenApiRequestBody(ServerWebExchange exchange) {
		return exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
	}

	/**
	 * @return previousCachedBody
	 */
	public static Object setOpenApiRequestBody(ServerWebExchange exchange, OpenApiRequestBody requestBody) {
		return exchange.getAttributes().put(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR, requestBody);
	}

	/**
	 * @return 当被认证时能识别到对应的app设置为缓存时有
	 */
	public static @Nullable App getApp(ServerWebExchange exchange) {
		return exchange.getAttribute(CACHED_APP_ATTR);
	}

	/**
	 * @return previousCachedBody
	 */
	public static Object setApp(ServerWebExchange exchange, App app) {
		return exchange.getAttributes().put(CACHED_APP_ATTR, app);
	}

	public static boolean supportsSignType(String sign_type) {
		return AppKeySignUtils.supports(sign_type) || RSASignUtils.supports(sign_type);
	}

	/**
	 * {@link #supportsSignType()}
	 * 
	 * @param requestBody
	 * @param key
	 * @return
	 */
	public static boolean validateSign(OpenApiRequestBody requestBody, String key) {
		boolean b = false;
		if (AppKeySignUtils.supports(requestBody.getSign_type())) {
			b = AppKeySignUtils.validateRequestSign(requestBody, key);
		} else if (RSASignUtils.supports(requestBody.getSign_type())) {
			b = RSASignUtils.validateRequestSign(requestBody, key);
		} else {
			/**
			 * 不会进来，上面已校验 {@link #supportsSignType()}
			 */
		}
		return b;
	}

	public static String responseSign(OpenApiResponse body, String sign_type, App app) {
		if (AppKeySignUtils.supports(sign_type)) {
			return AppKeySignUtils.responseSign(body, sign_type, app.getAppKey());
		} else if (RSASignUtils.supports(sign_type)) {
			return RSASignUtils.responseSign(body, sign_type, app.getPrivateKey());
		}
		return null;
	}
}

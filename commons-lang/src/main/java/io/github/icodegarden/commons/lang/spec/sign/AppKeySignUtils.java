package io.github.icodegarden.commons.lang.spec.sign;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AppKeySignUtils extends BaseSignUtils {

	private static final Logger log = LoggerFactory.getLogger(AppKeySignUtils.class);

	public static String requestSign(OpenApiRequestBody body, String appKey) {
		String buildSignParams = buildRequestSignParams(body, appKey);
		if (log.isDebugEnabled()) {
			log.debug("request params to sign:{}", buildSignParams);
		}

		String sign_type = body.getSign_type();
		return doSign(buildSignParams, sign_type);
	}

	public static boolean validateRequestSign(OpenApiRequestBody body, String appKey) {
		String requestSign = requestSign(body, appKey);
		return requestSign.equals(body.getSign());
	}

	public static String responseSign(OpenApiResponseBody body, String sign_type, String appKey) {
		String buildSignParams = buildResponseSignParams(body, appKey);
		if (log.isDebugEnabled()) {
			log.debug("response params to sign:{}", buildSignParams);
		}

		return doSign(buildSignParams, sign_type);
	}

	private static String doSign(String buildSignParams, String sign_type) {
		switch (sign_type) {
		case "MD5":
			return DigestUtils.md5Hex(buildSignParams).toUpperCase();
		case "SHA1":
			return DigestUtils.sha1Hex(buildSignParams).toUpperCase();
		case "SHA256":
			return DigestUtils.sha256Hex(buildSignParams).toUpperCase();
		default:
			throw new IllegalArgumentException("NOT SUPPORT sign_type:" + sign_type);
		}
	}

	public static boolean validateResponseSign(OpenApiResponseBody body, String sign_type, String appKey) {
		return responseSign(body, sign_type, appKey).equals(body.getSign());
	}

}

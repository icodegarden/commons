package io.github.icodegarden.commons.lang.spec.sign;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AppKeySignUtils extends BaseSignUtils {

	private static final Logger log = LoggerFactory.getLogger(AppKeySignUtils.class);

	private static enum SignType {
		SHA256, SHA1, MD5
	}

	public static boolean supports(String signType) {
		try {
			SignType.valueOf(signType);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

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

	public static String responseSign(OpenApiResponse body, String sign_type, String appKey) {
		String buildSignParams = buildResponseSignParams(body, appKey);
		if (log.isDebugEnabled()) {
			log.debug("response params to sign:{}", buildSignParams);
		}

		return doSign(buildSignParams, sign_type);
	}

	private static String doSign(String buildSignParams, String sign_type) {
		if (SignType.MD5.name().equals(sign_type)) {
			return DigestUtils.md5Hex(buildSignParams).toUpperCase();
		} else if (SignType.SHA1.name().equals(sign_type)) {
			return DigestUtils.sha1Hex(buildSignParams).toUpperCase();
		} else if (SignType.SHA256.name().equals(sign_type)) {
			return DigestUtils.sha256Hex(buildSignParams).toUpperCase();
		} else {
			throw new IllegalArgumentException("NOT SUPPORT sign_type:" + sign_type);
		}
	}

	public static boolean validateResponseSign(OpenApiResponse body, String sign_type, String appKey) {
		return responseSign(body, sign_type, appKey).equals(body.getSign());
	}

}

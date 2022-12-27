package io.github.icodegarden.commons.gateway.core.security.jwt;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * 
 * @author Fangfang.Xu
 *
 */
abstract class JWTDecoder {

	private static final Map<String, JWTVerifier> JWT_VERIFIER_MAP = new HashMap<String, JWTVerifier>();

	static DecodedJWT decode(String secretKey, String jwt) throws JWTVerificationException {
		JWTVerifier v = JWT_VERIFIER_MAP.get(secretKey);
		if (v == null) {
			v = JWT.require(Algorithm.HMAC256(secretKey)).build();
			JWT_VERIFIER_MAP.put(secretKey, v);
		}

		return v.verify(jwt);
	}
}

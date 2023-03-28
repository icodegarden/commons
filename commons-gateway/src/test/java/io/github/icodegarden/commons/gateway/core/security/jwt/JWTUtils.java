package io.github.icodegarden.commons.gateway.core.security.jwt;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JWTUtils {

	public static void main(String[] args) {
		long tokenExpireSeconds = 10 * 365 * 24 * 3600;
		String secretKey = "icodegarden@jwt0123456789";

		long now = System.currentTimeMillis();
		Date expiresAt = new Date(now + tokenExpireSeconds * 1000);
		String sign = JWT.create()//
				.withSubject("auth")//
				.withIssuer("icodegarden")//
				.withClaim("id", 1L)//
				.withClaim("username", "xff")//
				.withClaim("platformRole", "admin")//
				.withExpiresAt(expiresAt)//
				.sign(Algorithm.HMAC256(secretKey));

		System.out.println("Authorization Bearer " + sign);
	}

}
package io.github.icodegarden.commons.gateway.core.security.jwt;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class JWTConfig {

	private String issuer;
	private String secretKey;
	private int tokenExpireSeconds;

	public JWTConfig(String issuer, String secretKey, int tokenExpireSeconds) {
		Assert.hasText(issuer, "issuer must not empty");
		Assert.hasText(secretKey, "secretKey must not empty");
		this.issuer = issuer;
		this.secretKey = secretKey;
		this.tokenExpireSeconds = tokenExpireSeconds;
	}

}
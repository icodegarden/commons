package io.github.icodegarden.commons.apigateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "commons.gateway.security")
public class CommonsGatewaySecurityProperties {

	private Jwt jwt = new Jwt();

	@Data
	public static class Jwt {
		private String issuer;
		private String secretKey;
		private int tokenExpireSeconds = 3600;
	}
}
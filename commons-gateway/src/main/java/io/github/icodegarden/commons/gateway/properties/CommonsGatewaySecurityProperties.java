package io.github.icodegarden.commons.gateway.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.commons.gateway.core.security.App;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */

@ConfigurationProperties(prefix = "commons.gateway.security")
@Getter
@Setter
@ToString
public class CommonsGatewaySecurityProperties {

	private Jwt jwt;
	private AppKey appKey;

	@Getter
	@Setter
	@ToString
	public static class Jwt {
		private String issuer;
		private String secretKey;
		private int tokenExpireSeconds = 3600;
	}

	@Getter
	@Setter
	@ToString
	public static class AppKey {

		/**
		 * 是否在认证后设置appKey的header
		 */
		private Boolean headerAppKey = false;
		private List<App> apps;
	}
}
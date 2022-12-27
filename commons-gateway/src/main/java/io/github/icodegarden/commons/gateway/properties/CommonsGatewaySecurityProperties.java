package io.github.icodegarden.commons.gateway.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

import io.github.icodegarden.commons.gateway.core.security.signature.App;
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
	private Signature signature;

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
	public static class Signature {

		/**
		 * 是否在认证后设置appKey的header
		 */
		private Boolean headerAppKey = false;
		
		@Nullable
		private List<App> apps;
	}
}
package io.github.icodegarden.commons.gateway.properties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		 * 能够被网关接受的请求，不在此范围的将被直接拒绝；这有利于短路代码提升性能
		 */
//		private Set<String> acceptPathPatterns = new HashSet<String>(Arrays.asList("/openapi/v1/biz/methods"));// 默认只需对此path
		/**
		 * 需要被网关认证的请求，不在此范围的直接跳过认证，能不能pass则看springsecurity的配置规则
		 */
		private Set<String> authPathPatterns = new HashSet<String>(Arrays.asList("/openapi/v1/biz/methods"));// 默认只需对此path进行认证

		/**
		 * 是否在认证后设置appKey到header
		 */
		private Boolean headerAppKey = false;

		@Nullable
		private List<App> apps;
	}
}
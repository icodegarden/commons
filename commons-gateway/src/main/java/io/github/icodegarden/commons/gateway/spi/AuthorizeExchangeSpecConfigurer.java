package io.github.icodegarden.commons.gateway.spi;

import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;

import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties.Jwt;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface AuthorizeExchangeSpecConfigurer {

	void config(CommonsGatewaySecurityProperties securityProperties, AuthorizeExchangeSpec authorizeExchangeSpec);

	public static void configDefault(CommonsGatewaySecurityProperties securityProperties,
			AuthorizeExchangeSpec authorizeExchangeSpec) {
		CommonsGatewaySecurityProperties.Signature signature = securityProperties.getSignature();
		Jwt jwt = securityProperties.getJwt();
		if (signature != null) {
			signature.getAuthPathPatterns().forEach(pathPattern -> {
				/**
				 * openapi系列
				 */
				authorizeExchangeSpec.pathMatchers(pathPattern).authenticated();
			});
		} else if (jwt != null) {
			jwt.getAuthPathPatterns().forEach(pathPattern -> {
				/**
				 * api系列
				 */
				authorizeExchangeSpec.pathMatchers(pathPattern).authenticated();
			});
		}

		authorizeExchangeSpec
				/**
				 * 登录认证
				 */
				.pathMatchers("/*/login/**").permitAll()//
				.pathMatchers("/*/authenticate/**").permitAll()//
				/**
				 * 匿名
				 */
				.pathMatchers("/anonymous/**").permitAll()//
				.pathMatchers("/*/anonymous/**").permitAll()//
				/**
				 * swagger
				 */
				.pathMatchers("/swagger*/**").permitAll()//
				.pathMatchers("/*/swagger*/**").permitAll()//
				.pathMatchers("/*/v3/api-docs/**").permitAll()//
				/**
				 * spring actuator endpoint<br>
				 * 包括自定义的/actuator/readiness
				 */
				.pathMatchers("/actuator/**").permitAll()//
				/**
				 * 其他
				 */
				.anyExchange().authenticated();
	}

}
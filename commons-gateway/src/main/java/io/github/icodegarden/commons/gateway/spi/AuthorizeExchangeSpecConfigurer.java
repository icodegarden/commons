package io.github.icodegarden.commons.gateway.spi;

import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface AuthorizeExchangeSpecConfigurer {

	void config(AuthorizeExchangeSpec authorizeExchangeSpec);

	public static void configDefault(AuthorizeExchangeSpec authorizeExchangeSpec) {
		authorizeExchangeSpec
				/**
				 * api系列
				 */
				.pathMatchers("/openapi/**").authenticated()//
				.pathMatchers("/*/api/**").authenticated()//
				.pathMatchers("/*/internalapi/**").authenticated()//
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
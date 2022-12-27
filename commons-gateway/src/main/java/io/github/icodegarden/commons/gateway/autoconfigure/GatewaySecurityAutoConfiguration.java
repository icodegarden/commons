package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.gateway.core.security.ApiResponseServerAuthenticationFailureHandler;
import io.github.icodegarden.commons.gateway.core.security.AppServerAuthenticationSuccessHandler;
import io.github.icodegarden.commons.gateway.core.security.NoOpReactiveAuthenticationManager;
import io.github.icodegarden.commons.gateway.core.security.UserServerAuthenticationSuccessHandler;
import io.github.icodegarden.commons.gateway.core.security.jwt.JWTAuthenticationWebFilter;
import io.github.icodegarden.commons.gateway.core.security.jwt.JWTServerAuthenticationConverter;
import io.github.icodegarden.commons.gateway.core.security.signature.SignatureAuthenticationWebFilter;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties.Jwt;
import io.github.icodegarden.commons.gateway.spi.AppProvider;
import io.github.icodegarden.commons.gateway.spi.AuthWebFilter;
import io.github.icodegarden.commons.gateway.spi.JWTAuthenticationConverter;
import io.github.icodegarden.commons.gateway.spi.OpenApiRequestValidator;
import io.github.icodegarden.commons.gateway.spi.impl.DefaultJWTAuthenticationConverter;
import io.github.icodegarden.commons.springboot.security.ApiResponseServerAccessDeniedHandler;
import io.github.icodegarden.commons.springboot.security.ApiResponseServerAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Fangfang.Xu
 */
@ConditionalOnProperty(value = "commons.gateway.security.support.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CommonsGatewaySecurityProperties.class)
@Configuration
@EnableWebFluxSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
public class GatewaySecurityAutoConfiguration {

	@Autowired
	private CommonsGatewaySecurityProperties securityProperties;
	@Autowired(required = false)
	private AuthorizeExchangeSpecConfigurer authorizeExchangeSpecConfigurer;
	@Autowired
	private AppProvider appProvider;
	@Autowired
	private OpenApiRequestValidator openApiRequestValidator;
	@Autowired(required = false)
	private AuthWebFilter authWebFilter;

	@Autowired(required = false)
	private JWTAuthenticationConverter jwtAuthenticationConverter;
	@Autowired(required = false)
	private ReactiveAuthenticationManager authenticationManager;
	@Autowired(required = false)
	private ServerAuthenticationConverter serverAuthenticationConverter;
	@Autowired(required = false)
	private ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler;
	@Autowired(required = false)
	private ServerAuthenticationFailureHandler authenticationFailureHandler;

	/**
	 * 配置方式要换成 WebFlux的方式
	 */
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		ApiResponseServerAuthenticationEntryPoint serverAuthenticationEntryPoint = new ApiResponseServerAuthenticationEntryPoint();

		AuthorizeExchangeSpec authorizeExchangeSpec = http.exceptionHandling()
				.authenticationEntryPoint(serverAuthenticationEntryPoint)
				.accessDeniedHandler(new ApiResponseServerAccessDeniedHandler()).and().csrf().disable().headers()
				.frameOptions().disable().and().authorizeExchange();

		if (authorizeExchangeSpecConfigurer != null) {
			log.info("gateway security AuthorizeExchangeSpecConfigurer is exist, do config custom");
			authorizeExchangeSpecConfigurer.config(authorizeExchangeSpec);
		} else {
			log.info("gateway security AuthorizeExchangeSpecConfigurer not exist, do config default");
			AuthorizeExchangeSpecConfigurer.configDefault(authorizeExchangeSpec);
		}

		WebFilter webFilter;
		if (authWebFilter != null) {
			/**
			 * 自定义的优先
			 */
			webFilter = authWebFilter;
		} else if (securityProperties.getJwt() != null) {
			Jwt jwt = securityProperties.getJwt();
			log.info("gateway security config Authentication WebFilter by jwt:{}", jwt);

			webFilter = new JWTAuthenticationWebFilter(
					authenticationManager != null ? authenticationManager : new NoOpReactiveAuthenticationManager(),
					serverAuthenticationConverter != null ? serverAuthenticationConverter
							: new JWTServerAuthenticationConverter(jwt.getSecretKey(),
									jwtAuthenticationConverter != null ? jwtAuthenticationConverter
											: new DefaultJWTAuthenticationConverter()),
					serverAuthenticationSuccessHandler != null ? serverAuthenticationSuccessHandler
							: new UserServerAuthenticationSuccessHandler(),
					authenticationFailureHandler != null ? authenticationFailureHandler
							: new ApiResponseServerAuthenticationFailureHandler());
		} else if (securityProperties.getSignature() != null) {
			CommonsGatewaySecurityProperties.Signature signature = securityProperties.getSignature();
			log.info("gateway security config Authentication WebFilter by signature:{}", signature);

			webFilter = new SignatureAuthenticationWebFilter(appProvider, openApiRequestValidator,
					authenticationManager != null ? authenticationManager : new NoOpReactiveAuthenticationManager(),
					serverAuthenticationSuccessHandler != null ? serverAuthenticationSuccessHandler
							: new AppServerAuthenticationSuccessHandler(appProvider, signature.getHeaderAppKey()),
					authenticationFailureHandler != null ? authenticationFailureHandler
							: new ApiResponseServerAuthenticationFailureHandler());
		} else {
			log.info("gateway security config Authentication WebFilter by NoOp");
			webFilter = new NoOpWebFilter();
		}

		authorizeExchangeSpec.and().addFilterBefore(webFilter, SecurityWebFiltersOrder.AUTHORIZATION);

		return http.build();
	}

	public static interface AuthorizeExchangeSpecConfigurer {

		public static void configDefault(AuthorizeExchangeSpec authorizeExchangeSpec) {
			authorizeExchangeSpec
					/**
					 * api系列
					 */
					.pathMatchers("/openapi/**").authenticated().pathMatchers("/*/api/**").authenticated()
					.pathMatchers("/*/internalapi/**").authenticated()
					/**
					 * 登录认证
					 */
					.pathMatchers("/*/login/**").permitAll().pathMatchers("/*/authenticate/**").permitAll()
					/**
					 * 匿名
					 */
					.pathMatchers("/anonymous/**").permitAll().pathMatchers("/*/anonymous/**").permitAll()
					/**
					 * swagger
					 */
					.pathMatchers("/swagger*/**").permitAll().pathMatchers("/*/swagger*/**").permitAll()
					.pathMatchers("/*/v3/api-docs/**").permitAll()
					/**
					 * spring actuator endpoint<br>
					 * 包括自定义的/actuator/readiness
					 */
					.pathMatchers("/actuator/**").permitAll()
					/**
					 * 其他
					 */
					.anyExchange().authenticated();
		}

		void config(AuthorizeExchangeSpec authorizeExchangeSpec);
	}

	private class NoOpWebFilter implements AuthWebFilter {
		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			return chain.filter(exchange);
		}

	}
}

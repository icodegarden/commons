package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
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
import io.github.icodegarden.commons.gateway.core.security.AuthMatcher;
import io.github.icodegarden.commons.gateway.core.security.NoOpReactiveAuthenticationManager;
import io.github.icodegarden.commons.gateway.core.security.UserServerAuthenticationSuccessHandler;
import io.github.icodegarden.commons.gateway.core.security.jwt.JWTAuthenticationWebFilter;
import io.github.icodegarden.commons.gateway.core.security.jwt.JWTServerAuthenticationConverter;
import io.github.icodegarden.commons.gateway.core.security.signature.SignatureAuthenticationWebFilter;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties.Jwt;
import io.github.icodegarden.commons.gateway.spi.AppProvider;
import io.github.icodegarden.commons.gateway.spi.AuthWebFilter;
import io.github.icodegarden.commons.gateway.spi.AuthorizeExchangeSpecConfigurer;
import io.github.icodegarden.commons.gateway.spi.JWTAuthenticationConverter;
import io.github.icodegarden.commons.gateway.spi.JWTTokenExtractor;
import io.github.icodegarden.commons.gateway.spi.OpenApiRequestValidator;
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
	@Autowired
	private AuthMatcher authMatcher;
	@Autowired
	private ServerCodecConfigurer codecConfigurer;
	@Autowired(required = false)
	private AuthorizeExchangeSpecConfigurer authorizeExchangeSpecConfigurer;
	@Autowired
	private AppProvider appProvider;
	@Autowired
	private OpenApiRequestValidator openApiRequestValidator;
	@Autowired(required = false)
	private AuthWebFilter authWebFilter;

	@Autowired
	private JWTTokenExtractor jwtTokenExtractor;
	@Autowired
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
			authorizeExchangeSpecConfigurer.config(securityProperties, authorizeExchangeSpec);
		} else {
			log.info("gateway security AuthorizeExchangeSpecConfigurer not exist, do config default");
			AuthorizeExchangeSpecConfigurer.configDefault(securityProperties, authorizeExchangeSpec);
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

			webFilter = new JWTAuthenticationWebFilter(authMatcher,
					authenticationManager != null ? authenticationManager : new NoOpReactiveAuthenticationManager(),
					serverAuthenticationConverter != null ? serverAuthenticationConverter
							: new JWTServerAuthenticationConverter(jwt.getSecretKey(), jwtTokenExtractor,
									jwtAuthenticationConverter),
					serverAuthenticationSuccessHandler != null ? serverAuthenticationSuccessHandler
							: new UserServerAuthenticationSuccessHandler(),
					authenticationFailureHandler != null ? authenticationFailureHandler
							: new ApiResponseServerAuthenticationFailureHandler());
		} else if (securityProperties.getSignature() != null) {
			CommonsGatewaySecurityProperties.Signature signature = securityProperties.getSignature();
			log.info("gateway security config Authentication WebFilter by signature:{}", signature);
			SignatureAuthenticationWebFilter.Config config = new SignatureAuthenticationWebFilter.Config(
					codecConfigurer, appProvider, openApiRequestValidator,
					authenticationManager != null ? authenticationManager : new NoOpReactiveAuthenticationManager(),
					serverAuthenticationSuccessHandler != null ? serverAuthenticationSuccessHandler
							: new AppServerAuthenticationSuccessHandler(appProvider, signature.getHeaderAppKey()),
					authenticationFailureHandler != null ? authenticationFailureHandler
							: new ApiResponseServerAuthenticationFailureHandler());
			webFilter = new SignatureAuthenticationWebFilter(authMatcher, config);
		} else {
			log.info("gateway security config Authentication WebFilter by NoOp");
			webFilter = new NoOpWebFilter();
		}

		authorizeExchangeSpec.and().addFilterBefore(webFilter, SecurityWebFiltersOrder.AUTHORIZATION);

		return http.build();
	}

	private class NoOpWebFilter implements AuthWebFilter {
		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			return chain.filter(exchange);
		}

	}
}

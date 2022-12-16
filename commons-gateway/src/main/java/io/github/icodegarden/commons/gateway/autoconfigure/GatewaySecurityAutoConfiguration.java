package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.gateway.core.security.AppProvider;
import io.github.icodegarden.commons.gateway.core.security.JWTAuthenticationWebFilter;
import io.github.icodegarden.commons.gateway.core.security.JWTConfig;
import io.github.icodegarden.commons.gateway.core.security.OpenApiRequestValidator;
import io.github.icodegarden.commons.gateway.core.security.SignatureAuthenticationWebFilter;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties.Jwt;
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
    
    /**
     * 配置方式要换成 WebFlux的方式
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		ApiResponseServerAuthenticationEntryPoint serverAuthenticationEntryPoint 
			= new ApiResponseServerAuthenticationEntryPoint();
		
        AuthorizeExchangeSpec authorizeExchangeSpec = http
            .exceptionHandling()
            .authenticationEntryPoint(serverAuthenticationEntryPoint)
            .accessDeniedHandler(new ApiResponseServerAccessDeniedHandler())
        .and()
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
        .and()
        	.authorizeExchange();
        
        if(authorizeExchangeSpecConfigurer != null) {
        	log.info("gateway security AuthorizeExchangeSpecConfigurer is exist, do config custom");
        	authorizeExchangeSpecConfigurer.config(authorizeExchangeSpec);
        }else {
        	log.info("gateway security AuthorizeExchangeSpecConfigurer not exist, do config default");
        	AuthorizeExchangeSpecConfigurer.configDefault(authorizeExchangeSpec);
        }
        
		WebFilter webFilter;
		if (securityProperties.getJwt() != null) {
			Jwt jwt = securityProperties.getJwt();
			log.info("gateway security config Authentication WebFilter by jwt:{}", jwt);

			JWTConfig jwtConfig = new JWTConfig(jwt.getIssuer(), jwt.getSecretKey(), jwt.getTokenExpireSeconds());
			webFilter = new JWTAuthenticationWebFilter(jwtConfig, serverAuthenticationEntryPoint);
		} else if (securityProperties.getSignature() != null) {
			CommonsGatewaySecurityProperties.Signature signature = securityProperties.getSignature();
			log.info("gateway security config Authentication WebFilter by signature:{}", signature);
			webFilter = new SignatureAuthenticationWebFilter(appProvider, openApiRequestValidator, serverAuthenticationEntryPoint)
					.setHeaderAppKey(signature.getHeaderAppKey());
		} else {
			log.info("gateway security config Authentication WebFilter by NoOp");
			webFilter = new NoOpWebFilter();
		}
        
        authorizeExchangeSpec
        .and()
        	.addFilterBefore(webFilter, SecurityWebFiltersOrder.AUTHORIZATION);
        
        return http.build();
    }
    
    public static interface AuthorizeExchangeSpecConfigurer {

    	public static void configDefault(AuthorizeExchangeSpec authorizeExchangeSpec) {
    		authorizeExchangeSpec
    		/**
    		 * api系列
    		 */
        	.pathMatchers("/openapi/**").authenticated()
        	.pathMatchers("/*/api/**").authenticated()
            .pathMatchers("/*/internalapi/**").authenticated()
            /**
             * 登录认证
             */
            .pathMatchers("/*/login/**").permitAll()
            .pathMatchers("/*/authenticate/**").permitAll()
            /**
             * 匿名
             */
            .pathMatchers("/anonymous/**").permitAll()
            .pathMatchers("/*/anonymous/**").permitAll()
            /**
             * swagger
             */
            .pathMatchers("/swagger*/**").permitAll()
            .pathMatchers("/*/swagger*/**").permitAll()
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
    
    private class NoOpWebFilter implements WebFilter {
		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			return chain.filter(exchange);
		}
    	
    }
}

package io.github.icodegarden.commons.apigateway.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import io.github.icodegarden.commons.apigateway.core.security.JWTAuthenticationWebFilter;
import io.github.icodegarden.commons.apigateway.core.security.JWTConfig;
import io.github.icodegarden.commons.apigateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.apigateway.properties.CommonsGatewaySecurityProperties.Jwt;
import io.github.icodegarden.commons.springboot.security.ApiResponseServerAccessDeniedHandler;
import io.github.icodegarden.commons.springboot.security.ApiResponseServerAuthenticationEntryPoint;

/**
 * @author Fangfang.Xu
 */
@ConditionalOnProperty(value = "commons.gateway.security.support.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableWebFluxSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class GatewaySecurityConfiguration {
	
    @Autowired
	private CommonsGatewaySecurityProperties securityProperties;
    
    /**
     * 配置方式要换成 WebFlux的方式
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    	Jwt jwt = securityProperties.getJwt();
		JWTConfig jwtConfig = new JWTConfig(jwt.getIssuer(), jwt.getSecretKey(),
				jwt.getTokenExpireSeconds());
    	
		ApiResponseServerAuthenticationEntryPoint serverAuthenticationEntryPoint 
			= new ApiResponseServerAuthenticationEntryPoint();
		
        http
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
        	.authorizeExchange()
            .pathMatchers("/*/api/**").authenticated()
            .pathMatchers("/*/internalapi/**").authenticated()
            .pathMatchers("/*/innerapi/**").authenticated()
            .pathMatchers("/*/login/**").permitAll()
            .pathMatchers("/*/authenticate/**").permitAll()
            .pathMatchers("/anonymous/**").permitAll()
            .pathMatchers("/*/anonymous/**").permitAll()
            .pathMatchers("/swagger*/**").permitAll()
            .pathMatchers("/*/swagger*/**").permitAll()
            .pathMatchers("/*/v3/api-docs/**").permitAll()
            .pathMatchers("/readness/**").permitAll()
            .anyExchange().authenticated()
        .and()
        	.addFilterBefore(new JWTAuthenticationWebFilter(jwtConfig, 
        			serverAuthenticationEntryPoint), SecurityWebFiltersOrder.AUTHORIZATION);
        
        return http.build();
    }
}

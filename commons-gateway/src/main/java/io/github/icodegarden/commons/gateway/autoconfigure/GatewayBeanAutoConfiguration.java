package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.gateway.spi.AppProvider;
import io.github.icodegarden.commons.gateway.spi.JWTAuthenticationConverter;
import io.github.icodegarden.commons.gateway.spi.JWTTokenExtractor;
import io.github.icodegarden.commons.gateway.spi.OpenApiRequestValidator;
import io.github.icodegarden.commons.gateway.spi.impl.ConfiguredAppProvider;
import io.github.icodegarden.commons.gateway.spi.impl.DefaultJWTAuthenticationConverter;
import io.github.icodegarden.commons.gateway.spi.impl.DefaultJWTTokenExtractor;
import io.github.icodegarden.commons.gateway.spi.impl.DefaultOpenApiRequestValidator;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class GatewayBeanAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public JWTTokenExtractor defaultJWTTokenExtractor() {
		return new DefaultJWTTokenExtractor();
	}
	
	@ConditionalOnMissingBean
	@Bean
	public JWTAuthenticationConverter defaultJWTAuthenticationConverter() {
		return new DefaultJWTAuthenticationConverter();
	}

	@ConditionalOnMissingBean
	@Bean
	public AppProvider configuredAppProvider(CommonsGatewaySecurityProperties securityProperties) {
		return new ConfiguredAppProvider(securityProperties);
	}

	@ConditionalOnMissingBean
	@Bean
	public OpenApiRequestValidator defaultOpenApiRequestValidator() {
		return new DefaultOpenApiRequestValidator();
	}
}

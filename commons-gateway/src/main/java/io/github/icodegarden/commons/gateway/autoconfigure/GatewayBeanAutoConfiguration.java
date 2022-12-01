package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.gateway.core.security.AppProvider;
import io.github.icodegarden.commons.gateway.core.security.DefaultAppProvider;
import io.github.icodegarden.commons.gateway.core.security.DefaultOpenApiRequestValidator;
import io.github.icodegarden.commons.gateway.core.security.OpenApiRequestValidator;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class GatewayBeanAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public AppProvider appProvider() {
		return new DefaultAppProvider();
	}

	@ConditionalOnMissingBean
	@Bean
	public OpenApiRequestValidator openApiRequestValidator() {
		return new DefaultOpenApiRequestValidator();
	}
}

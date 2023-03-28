package io.github.icodegarden.commons.gateway.autoconfigure;

import java.util.Set;

import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;

import io.github.icodegarden.commons.gateway.filter.InternalApiModifyResponseBodyGatewayFilterFactory;
import io.github.icodegarden.commons.gateway.filter.OpenApiModifyRequestBodyGatewayFilterFactory;
import io.github.icodegarden.commons.gateway.filter.OpenApiModifyResponseBodyGatewayFilterFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class GatewayFilterFactoryAutoConfiguration {

	@Bean
	@ConditionalOnEnabledFilter
	public OpenApiModifyRequestBodyGatewayFilterFactory openapiModifyRequestBodyGatewayFilterFactory(
			ServerCodecConfigurer codecConfigurer) {
		return new OpenApiModifyRequestBodyGatewayFilterFactory(codecConfigurer.getReaders());
	}

	@Bean
	@ConditionalOnEnabledFilter
	public OpenApiModifyResponseBodyGatewayFilterFactory openapiModifyResponseBodyGatewayFilterFactory(
			ServerCodecConfigurer codecConfigurer, Set<MessageBodyDecoder> bodyDecoders,
			Set<MessageBodyEncoder> bodyEncoders) {
		return new OpenApiModifyResponseBodyGatewayFilterFactory(codecConfigurer.getReaders(), bodyDecoders,
				bodyEncoders);
	}
	
	@Bean
	@ConditionalOnEnabledFilter
	public InternalApiModifyResponseBodyGatewayFilterFactory internalApiModifyResponseBodyGatewayFilterFactory(
			ServerCodecConfigurer codecConfigurer, Set<MessageBodyDecoder> bodyDecoders,
			Set<MessageBodyEncoder> bodyEncoders) {
		return new InternalApiModifyResponseBodyGatewayFilterFactory(codecConfigurer.getReaders(), bodyDecoders,
				bodyEncoders);
	}
}

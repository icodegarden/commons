package io.github.icodegarden.commons.springboot.configuration;

import javax.servlet.Filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.alibaba.csp.sentinel.SphU;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.springboot.ServiceRegistryGracefullyShutdown;
import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import io.github.icodegarden.commons.springboot.web.filter.ProcessingRequestCountFilter;
import io.github.icodegarden.commons.springboot.web.handler.ApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.SentinelAdaptiveApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.util.MappingJackson2HttpMessageConverters;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass({ MappingJackson2HttpMessageConverter.class, Filter.class, ControllerAdvice.class })
@Configuration
public class CommonsWebAutoConfiguration {

	@ConditionalOnProperty(value = "commons.web.converter.mappingJackson.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		return MappingJackson2HttpMessageConverters.simple();
	}

	@ConditionalOnProperty(value = "commons.web.filter.gatewayPreAuthenticatedAuthentication.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public FilterRegistrationBean<Filter> gatewayPreAuthenticatedAuthenticationFilter() {
		GatewayPreAuthenticatedAuthenticationFilter filter = new GatewayPreAuthenticatedAuthenticationFilter();

		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
		bean.setFilter(filter);
		bean.setName("gatewayPreAuthenticatedAuthenticationFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

		return bean;
	}

	@ConditionalOnProperty(value = "commons.web.filter.processingRequestCount.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public FilterRegistrationBean<Filter> processingRequestCountFilter(ServiceRegistry serviceRegistry,
			Registration registration) {
		/**
		 * 顺序最后，30秒实例刷新间隔+10秒冗余
		 */
		ProcessingRequestCountFilter processingRequestCountFilter = new ProcessingRequestCountFilter(Integer.MAX_VALUE,
				30 * 1000 + 10 * 1000);

		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
		bean.setFilter(processingRequestCountFilter);
		bean.setName("processingRequestCountFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

		GracefullyShutdown.Registry.singleton()
				.register(new ServiceRegistryGracefullyShutdown(serviceRegistry, registration));
		GracefullyShutdown.Registry.singleton().register(processingRequestCountFilter);

		return bean;
	}

	@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnClass(SphU.class)
	@Configuration
	protected static class SentinelAdaptiveApiResponseExceptionHandlerAutoConfiguration {
		@Bean
		public SentinelAdaptiveApiResponseExceptionHandler sentinelAdaptiveApiResponseExceptionHandler() {
			return new SentinelAdaptiveApiResponseExceptionHandler();
		}
	}

	@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnMissingBean(SentinelAdaptiveApiResponseExceptionHandler.class)
	@Configuration
	protected static class ApiResponseExceptionHandlerAutoConfiguration {
		@Bean
		public ApiResponseExceptionHandler apiResponseExceptionHandler() {
			return new ApiResponseExceptionHandler();
		}
	}

}

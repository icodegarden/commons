package io.github.icodegarden.commons.springboot.autoconfigure;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.WebFilter;
import org.springframework.web.servlet.DispatcherServlet;

import com.alibaba.csp.sentinel.SphU;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.springboot.ServiceRegistryGracefullyShutdown;
import io.github.icodegarden.commons.springboot.web.filter.CacheRequestBodyFilter;
import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import io.github.icodegarden.commons.springboot.web.filter.ProcessingRequestCountFilter;
import io.github.icodegarden.commons.springboot.web.filter.ProcessingRequestCountWebFilter;
import io.github.icodegarden.commons.springboot.web.handler.ApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.NativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.SentinelAdaptiveApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.SentinelAdaptiveNativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.util.MappingJackson2HttpMessageConverters;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@Slf4j
public class CommonsWebAutoConfiguration {

	private static final int FILTER_ORDER_PROCESSING_REQUEST_COUNT = Ordered.HIGHEST_PRECEDENCE;// 最高优先级
	private static final int FILTER_ORDER_GATEWAY_PRE_AUTHENTICATED_AUTHENTICATION = FILTER_ORDER_PROCESSING_REQUEST_COUNT
			+ 1;
	
	/**
	 * 公共的
	 * 可能不是springcloud项目
	 */
	@ConditionalOnClass({ ServiceRegistry.class })
	@Configuration
	protected static class ServiceRegistryGracefullyShutdownAutoConfiguration {
		@Autowired(required = false)
		private ServiceRegistry serviceRegistry;
		@Autowired(required = false)
		private Registration registration;

		@PostConstruct
		private void init() {
			log.info("commons init bean of ServiceRegistryGracefullyShutdownAutoConfiguration");
			if (serviceRegistry != null && registration != null) {
				GracefullyShutdown.Registry.singleton()
						.register(new ServiceRegistryGracefullyShutdown(serviceRegistry, registration));// 默认下线优先级最高
			}
		}
	}
	
	/**
	 * 公共的
	 */
	@ConditionalOnProperty(value = "commons.web.converter.mappingJackson.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		log.info("commons init bean of MappingJackson2HttpMessageConverter");
		return MappingJackson2HttpMessageConverters.simple();
	}

	// ----------------------------------------------------------------------------------------

	/**
	 * 有webmvc <br>
	 * 
	 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
	 */
	@ConditionalOnClass({ DispatcherServlet.class })
	@Configuration
	protected static class WebMvcAutoConfiguration {

		@ConditionalOnProperty(value = "commons.web.filter.cacheRequestBody.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public FilterRegistrationBean<Filter> cacheRequestBodyFilter() {
			log.info("commons init bean of CacheRequestBodyFilter");

			CacheRequestBodyFilter filter = new CacheRequestBodyFilter();

			FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
			bean.setFilter(filter);
			bean.setName("cacheRequestBodyFilter");
			bean.addUrlPatterns("/*");
			bean.setOrder(Ordered.LOWEST_PRECEDENCE);

			return bean;
		}

		@ConditionalOnProperty(value = "commons.web.filter.processingRequestCount.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public FilterRegistrationBean<Filter> processingRequestCountFilter() {
			log.info("commons init bean of ProcessingRequestCountFilter");

			/**
			 * 下线优先级最低，30秒实例刷新间隔+10秒冗余
			 */
			ProcessingRequestCountFilter processingRequestCountFilter = new ProcessingRequestCountFilter(
					Integer.MAX_VALUE, 30 * 1000 + 10 * 1000/* 写死固定值，基本不需要配置化 */);

			FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
			bean.setFilter(processingRequestCountFilter);
			bean.setName("processingRequestCountFilter");
			bean.addUrlPatterns("/*");
			bean.setOrder(FILTER_ORDER_PROCESSING_REQUEST_COUNT);

			GracefullyShutdown.Registry.singleton().register(processingRequestCountFilter);

			return bean;
		}

		/**
		 * 网关是webflux不会有这个
		 */
		/**
		 * 只对没有spring-security依赖的起作用，有依赖的认为自己认证 
		 */
		@ConditionalOnMissingClass({"org.springframework.security.core.Authentication"})
		@ConditionalOnProperty(value = "commons.web.filter.gatewayPreAuthenticatedAuthentication.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public FilterRegistrationBean<Filter> gatewayPreAuthenticatedAuthenticationFilter() {
			log.info("commons init bean of GatewayPreAuthenticatedAuthenticationFilter");

			GatewayPreAuthenticatedAuthenticationFilter filter = new GatewayPreAuthenticatedAuthenticationFilter();

			FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
			bean.setFilter(filter);
			bean.setName("gatewayPreAuthenticatedAuthenticationFilter");
			bean.addUrlPatterns("/*");
			bean.setOrder(FILTER_ORDER_GATEWAY_PRE_AUTHENTICATED_AUTHENTICATION);

			return bean;
		}

		/**
		 * 内部依赖javax.servlet.<br>
		 * 有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherServlet.class, SphU.class })
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled", havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class SentinelAdaptiveApiResponseExceptionHandlerAutoConfiguration {
			@Bean
			public SentinelAdaptiveApiResponseExceptionHandler sentinelAdaptiveApiResponseExceptionHandler() {
				log.info("commons init bean of SentinelAdaptiveApiResponseExceptionHandler");
				return new SentinelAdaptiveApiResponseExceptionHandler();
			}
		}

		/**
		 * 内部依赖javax.servlet.<br>
		 * 有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherServlet.class, SphU.class })
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.nativeRestApi.enabled", havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class SentinelAdaptiveNativeRestApiExceptionHandlerAutoConfiguration {
			@Bean
			public SentinelAdaptiveNativeRestApiExceptionHandler sentinelAdaptiveNativeRestApiExceptionHandler() {
				log.info("commons init bean of SentinelAdaptiveNativeRestApiExceptionHandler");
				return new SentinelAdaptiveNativeRestApiExceptionHandler();
			}
		}

		/**
		 * 内部依赖javax.servlet.<br>
		 * 有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherServlet.class })
		@ConditionalOnMissingClass("com.alibaba.csp.sentinel.SphU")
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled", havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class ApiResponseExceptionHandlerAutoConfiguration {
			@Bean
			public ApiResponseExceptionHandler apiResponseExceptionHandler() {
				log.info("commons init bean of ApiResponseExceptionHandler");
				return new ApiResponseExceptionHandler();
			}
		}

		/**
		 * 内部依赖javax.servlet.<br>
		 * 有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherServlet.class })
		@ConditionalOnMissingClass("com.alibaba.csp.sentinel.SphU")
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.nativeRestApi.enabled", havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class NativeRestApiExceptionHandlerAutoConfiguration {
			@Bean
			public NativeRestApiExceptionHandler nativeRestApiExceptionHandler() {
				log.info("commons init bean of NativeRestApiExceptionHandler");
				return new NativeRestApiExceptionHandler();
			}
		}
	}

	// ----------------------------------------------------------------------------------------

	/**
	 * 有webflux，且没有webmvc <br>
	 * 
	 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
	 */
	@ConditionalOnClass({ DispatcherHandler.class })
	@ConditionalOnMissingClass({ "org.springframework.web.servlet.DispatcherServlet",
			"org.glassfish.jersey.servlet.ServletContainer" })
	@Configuration
	protected static class WebFluxAutoConfiguration {

		/**
		 * 暂无Flux的CacheRequestBody
		 */

		@ConditionalOnProperty(value = "commons.web.webfilter.processingRequestCount.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public WebFilter processingRequestCountFilter() {
			log.info("commons init bean of ProcessingRequestCountFilter");

			/**
			 * 下线优先级最低，30秒实例刷新间隔+10秒冗余
			 */
			ProcessingRequestCountWebFilter processingRequestCountWebFilter = new ProcessingRequestCountWebFilter(
					Integer.MAX_VALUE, 30 * 1000 + 10 * 1000/* 写死固定值，基本不需要配置化 */);
			processingRequestCountWebFilter.setOrder(FILTER_ORDER_PROCESSING_REQUEST_COUNT);

			GracefullyShutdown.Registry.singleton().register(processingRequestCountWebFilter);

			return processingRequestCountWebFilter;
		}

		/**
		 * 暂不实现 GatewayPreAuthenticatedAuthenticationWebFilter，因为webflux是异步的，身份信息跨线程不适合
		 */

		/**
		 * 暂无Flux的ExceptionHandler
		 */
	}
}

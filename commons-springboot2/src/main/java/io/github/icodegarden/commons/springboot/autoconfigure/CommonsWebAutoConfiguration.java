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
import io.github.icodegarden.commons.springboot.web.filter.ReactiveGatewayPreAuthenticatedAuthenticationFilter;
import io.github.icodegarden.commons.springboot.web.filter.ReactiveProcessingRequestCountFilter;
import io.github.icodegarden.commons.springboot.web.handler.ApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ApiResponseReactiveExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.NativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.NativeRestApiReactiveExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.SentinelAdaptiveApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.SentinelAdaptiveApiResponseReactiveExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.SentinelAdaptiveNativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.SentinelAdaptiveNativeRestApiReactiveExceptionHandler;
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

	public static final int FILTER_ORDER_PROCESSING_REQUEST_COUNT = Ordered.HIGHEST_PRECEDENCE;// 最高优先级
	public static final int FILTER_ORDER_GATEWAY_PRE_AUTHENTICATED_AUTHENTICATION = FILTER_ORDER_PROCESSING_REQUEST_COUNT
			+ 100;

	/**
	 * 公共的 可能不是springcloud项目
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
	 * 公共的 可能不是web项目
	 */
	@ConditionalOnClass({ MappingJackson2HttpMessageConverter.class })
	@ConditionalOnProperty(value = "commons.web.converter.mappingJackson.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	protected static class MappingJackson2HttpMessageConverterAutoConfiguration {

		@Bean
		public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
			log.info("commons init bean of MappingJackson2HttpMessageConverter");
			return MappingJackson2HttpMessageConverters.simple();
		}
	}

	// ----------------------------------------------------------------------------------------

	/**
	 * 有webmvc，所以不会对gateway起作用 <br>
	 * spring对webmvc比webflux优先
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
		 * gateway是webflux因此不会有这个<br>
		 * 只对没有spring-security依赖的起作用，有依赖的认为自己认证
		 */
		@ConditionalOnMissingClass({ "org.springframework.security.core.Authentication" })
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

		/**
		 * gateway不需要这个<br>
		 */
		@ConditionalOnMissingClass({ "org.springframework.cloud.gateway.filter.GatewayFilter"/* gateway不需要这个 */ })
		@ConditionalOnProperty(value = "commons.reactiveWeb.filter.processingRequestCount.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public WebFilter reactiveProcessingRequestCountFilter() {
			log.info("commons init bean of ReactiveProcessingRequestCountFilter");

			/**
			 * 下线优先级最低，30秒实例刷新间隔+10秒冗余
			 */
			ReactiveProcessingRequestCountFilter filter = new ReactiveProcessingRequestCountFilter(Integer.MAX_VALUE,
					30 * 1000 + 10 * 1000/* 写死固定值，基本不需要配置化 */);
			filter.setOrder(FILTER_ORDER_PROCESSING_REQUEST_COUNT);

			/*
			 * 由CommonsBeanAutoConfiguration无损下线注册
			 */
//			GracefullyShutdown.Registry.singleton().register(filter);

			return filter;
		}

		/**
		 * gateway不需要这个<br>
		 * 只对没有spring-security依赖的起作用，有依赖的认为自己认证
		 */
		@ConditionalOnMissingClass({ "org.springframework.security.core.Authentication",
				"org.springframework.cloud.gateway.filter.GatewayFilter"/* gateway不需要这个 */ })
		@ConditionalOnProperty(value = "commons.reactiveWeb.filter.gatewayPreAuthenticatedAuthentication.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public WebFilter reactiveGatewayPreAuthenticatedAuthenticationFilter() {
			log.info("commons init bean of ReactiveGatewayPreAuthenticatedAuthenticationFilter");

			ReactiveGatewayPreAuthenticatedAuthenticationFilter filter = new ReactiveGatewayPreAuthenticatedAuthenticationFilter();
			filter.setOrder(FILTER_ORDER_GATEWAY_PRE_AUTHENTICATED_AUTHENTICATION);

			return filter;
		}

		/**
		 * 
		 * 有webflux且没有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherHandler.class, SphU.class })
		@ConditionalOnMissingClass({ "org.springframework.web.servlet.DispatcherServlet",
				"org.glassfish.jersey.servlet.ServletContainer" })
		@ConditionalOnProperty(value = "commons.reactiveWeb.exceptionHandler.apiResponse.enabled", havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class SentinelAdaptiveApiResponseReactiveExceptionHandlerAutoConfiguration {
			@Bean
			public SentinelAdaptiveApiResponseReactiveExceptionHandler sentinelAdaptiveApiResponseReactiveExceptionHandler() {
				log.info("commons init bean of SentinelAdaptiveApiResponseReactiveExceptionHandler");
				return new SentinelAdaptiveApiResponseReactiveExceptionHandler();
			}
		}

		/**
		 * 
		 * 有webflux且没有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherHandler.class, SphU.class })
		@ConditionalOnMissingClass({ "org.springframework.web.servlet.DispatcherServlet",
				"org.glassfish.jersey.servlet.ServletContainer" })
		@ConditionalOnProperty(value = "commons.reactiveWeb.exceptionHandler.nativeRestApi.enabled", havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class SentinelAdaptiveNativeRestApiReactiveExceptionHandlerAutoConfiguration {
			@Bean
			public SentinelAdaptiveNativeRestApiReactiveExceptionHandler sentinelAdaptiveNativeRestApiReactiveExceptionHandler() {
				log.info("commons init bean of SentinelAdaptiveNativeRestReactiveApiExceptionHandler");
				return new SentinelAdaptiveNativeRestApiReactiveExceptionHandler();
			}
		}

		/**
		 * 
		 * 有webflux且没有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherHandler.class })
		@ConditionalOnMissingClass({ "org.springframework.web.servlet.DispatcherServlet",
				"org.glassfish.jersey.servlet.ServletContainer", "com.alibaba.csp.sentinel.SphU" })
		@ConditionalOnProperty(value = "commons.reactiveWeb.exceptionHandler.apiResponse.enabled", havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class ApiResponseReactiveExceptionHandlerAutoConfiguration {
			@Bean
			public ApiResponseReactiveExceptionHandler apiResponseReactiveExceptionHandler() {
				log.info("commons init bean of ApiResponseReactiveExceptionHandler");
				return new ApiResponseReactiveExceptionHandler();
			}
		}

		/**
		 * 
		 * 有webflux且没有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherHandler.class })
		@ConditionalOnMissingClass({ "org.springframework.web.servlet.DispatcherServlet",
				"org.glassfish.jersey.servlet.ServletContainer", "com.alibaba.csp.sentinel.SphU" })
		@ConditionalOnProperty(value = "commons.reactiveWeb.exceptionHandler.nativeRestApi.enabled", havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class NativeRestApiReactiveExceptionHandlerAutoConfiguration {
			@Bean
			public NativeRestApiReactiveExceptionHandler nativeRestApiReactiveExceptionHandler() {
				log.info("commons init bean of NativeRestApiReactiveExceptionHandler");
				return new NativeRestApiReactiveExceptionHandler();
			}
		}
	}
}

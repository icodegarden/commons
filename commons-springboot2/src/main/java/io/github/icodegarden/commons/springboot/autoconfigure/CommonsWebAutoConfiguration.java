package io.github.icodegarden.commons.springboot.autoconfigure;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import io.github.icodegarden.commons.springboot.RegistryGracefullyShutdown;
import io.github.icodegarden.commons.springboot.ServiceRegistryGracefullyShutdown;
import io.github.icodegarden.commons.springboot.properties.CommonsWebProperties;
import io.github.icodegarden.commons.springboot.web.filter.ServletCacheRequestBodyFilter;
import io.github.icodegarden.commons.springboot.web.filter.ServletGatewayPreAuthenticatedAuthenticationFilter;
import io.github.icodegarden.commons.springboot.web.filter.ServletProcessingRequestCountFilter;
import io.github.icodegarden.commons.springboot.web.filter.ReactiveGatewayPreAuthenticatedAuthenticationFilter;
import io.github.icodegarden.commons.springboot.web.filter.ReactiveProcessingRequestCountFilter;
import io.github.icodegarden.commons.springboot.web.handler.ServletApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ReactiveApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ServletNativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ReactiveNativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ServletSentinelAdaptiveApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ReactiveSentinelAdaptiveApiResponseExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ServletSentinelAdaptiveNativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.handler.ReactiveSentinelAdaptiveNativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.util.MappingJackson2HttpMessageConverters;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EnableConfigurationProperties({ CommonsWebProperties.class })
@Configuration
@Slf4j
public class CommonsWebAutoConfiguration {

	public static final int FILTER_ORDER_PROCESSING_REQUEST_COUNT = Ordered.HIGHEST_PRECEDENCE;// 最高优先级
	public static final int FILTER_ORDER_GATEWAY_PRE_AUTHENTICATED_AUTHENTICATION = FILTER_ORDER_PROCESSING_REQUEST_COUNT
			+ 100;

	/**
	 * 如果用户有作为bean
	 */
	@Autowired(required = false)
	private io.github.icodegarden.commons.lang.concurrent.registry.Registry registry;

	@PostConstruct
	private void init() {
		/**
		 * 与springcloud的ServiceRegistry互不影响
		 */
		if (registry != null) {
			GracefullyShutdown.Registry.singleton().register(new RegistryGracefullyShutdown(registry));// 默认下线优先级最高
		}
	}

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
		public FilterRegistrationBean<Filter> servletCacheRequestBodyFilter() {
			log.info("commons init bean of CacheRequestBodyFilter");

			ServletCacheRequestBodyFilter filter = new ServletCacheRequestBodyFilter();

			FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
			bean.setFilter(filter);
			bean.setName("cacheRequestBodyFilter");
			bean.addUrlPatterns("/*");
			bean.setOrder(Ordered.LOWEST_PRECEDENCE);

			return bean;
		}

		@ConditionalOnProperty(value = "commons.web.filter.processingRequestCount.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public FilterRegistrationBean<Filter> servletProcessingRequestCountFilter() {
			log.info("commons init bean of ProcessingRequestCountFilter");

			/**
			 * 下线优先级最低，30秒实例刷新间隔+10秒冗余
			 */
			ServletProcessingRequestCountFilter processingRequestCountFilter = new ServletProcessingRequestCountFilter(
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
		public FilterRegistrationBean<Filter> servletGatewayPreAuthenticatedAuthenticationFilter() {
			log.info("commons init bean of GatewayPreAuthenticatedAuthenticationFilter");

			ServletGatewayPreAuthenticatedAuthenticationFilter filter = new ServletGatewayPreAuthenticatedAuthenticationFilter();

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
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class SentinelAdaptiveApiResponseExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ServletSentinelAdaptiveApiResponseExceptionHandler servletSentinelAdaptiveApiResponseExceptionHandler(
					CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of SentinelAdaptiveApiResponseExceptionHandler");
				ServletSentinelAdaptiveApiResponseExceptionHandler exceptionHandler = new ServletSentinelAdaptiveApiResponseExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
			}
		}

		/**
		 * 内部依赖javax.servlet.<br>
		 * 有webmvc <br>
		 * 
		 * @see org.springframework.boot.WebApplicationType.deduceFromClasspath()
		 */
		@ConditionalOnClass({ DispatcherServlet.class, SphU.class })
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.nativeRestApi.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class SentinelAdaptiveNativeRestApiExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ServletSentinelAdaptiveNativeRestApiExceptionHandler servletSentinelAdaptiveNativeRestApiExceptionHandler(
					CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of SentinelAdaptiveNativeRestApiExceptionHandler");
				ServletSentinelAdaptiveNativeRestApiExceptionHandler exceptionHandler = new ServletSentinelAdaptiveNativeRestApiExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
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
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class ApiResponseExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ServletApiResponseExceptionHandler servletApiResponseExceptionHandler(CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of ApiResponseExceptionHandler");
				ServletApiResponseExceptionHandler exceptionHandler = new ServletApiResponseExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
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
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.nativeRestApi.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class NativeRestApiExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ServletNativeRestApiExceptionHandler servletNativeRestApiExceptionHandler(
					CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of NativeRestApiExceptionHandler");
				ServletNativeRestApiExceptionHandler exceptionHandler = new ServletNativeRestApiExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
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
		@ConditionalOnProperty(value = "commons.web.filter.processingRequestCount.enabled", havingValue = "true", matchIfMissing = true)
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
		@ConditionalOnProperty(value = "commons.web.filter.gatewayPreAuthenticatedAuthentication.enabled", havingValue = "true", matchIfMissing = true)
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
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class SentinelAdaptiveApiResponseReactiveExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ReactiveSentinelAdaptiveApiResponseExceptionHandler reactiveSentinelAdaptiveApiResponseExceptionHandler(
					CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of SentinelAdaptiveApiResponseReactiveExceptionHandler");
				ReactiveSentinelAdaptiveApiResponseExceptionHandler exceptionHandler = new ReactiveSentinelAdaptiveApiResponseExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
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
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.nativeRestApi.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class SentinelAdaptiveNativeRestApiReactiveExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ReactiveSentinelAdaptiveNativeRestApiExceptionHandler reactiveSentinelAdaptiveNativeRestApiExceptionHandler(
					CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of SentinelAdaptiveNativeRestReactiveApiExceptionHandler");
				ReactiveSentinelAdaptiveNativeRestApiExceptionHandler exceptionHandler = new ReactiveSentinelAdaptiveNativeRestApiExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
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
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.apiResponse.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = true)
		@Configuration
		protected static class ApiResponseReactiveExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ReactiveApiResponseExceptionHandler reactiveApiResponseExceptionHandler(
					CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of ApiResponseReactiveExceptionHandler");
				ReactiveApiResponseExceptionHandler exceptionHandler = new ReactiveApiResponseExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
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
		@ConditionalOnProperty(value = "commons.web.exceptionHandler.nativeRestApi.enabled"/* mvc和flux使用相同的配置名 */, havingValue = "true", matchIfMissing = false)
		@Configuration
		protected static class NativeRestApiReactiveExceptionHandlerAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public ReactiveNativeRestApiExceptionHandler reactiveNativeRestApiExceptionHandler(
					CommonsWebProperties commonsWebProperties) {
				log.info("commons init bean of NativeRestApiReactiveExceptionHandler");
				ReactiveNativeRestApiExceptionHandler exceptionHandler = new ReactiveNativeRestApiExceptionHandler();
				exceptionHandler.setPrintErrorStackOnWarn(
						commonsWebProperties.getExceptionHandler().getPrintErrorStackOnWarn());
				return exceptionHandler;
			}
		}
	}
}

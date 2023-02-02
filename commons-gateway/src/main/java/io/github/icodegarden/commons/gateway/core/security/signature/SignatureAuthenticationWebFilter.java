package io.github.icodegarden.commons.gateway.core.security.signature;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.gateway.spi.AppProvider;
import io.github.icodegarden.commons.gateway.spi.AuthWebFilter;
import io.github.icodegarden.commons.gateway.spi.OpenApiRequestValidator;
import io.github.icodegarden.commons.gateway.util.CommonsGatewayUtils;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterMissingErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.LogUtils;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import io.github.icodegarden.commons.springboot.security.SpringUser;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class SignatureAuthenticationWebFilter implements AuthWebFilter {

	private static final Charset CHARSET = Charset.forName("utf-8");

	private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
	private final AuthenticationWebFilter authenticationWebFilter;
	private final AppProvider appProvider;
	private final OpenApiRequestValidator openApiRequestValidator;
//	private final ServerWebExchangeMatcher acceptMatcher;
	private final ServerWebExchangeMatcher authMatcher;

	public SignatureAuthenticationWebFilter(Config config) {
//		List<ServerWebExchangeMatcher> matchers = config.getAcceptPathPatterns().stream().map(path -> {
//			return new PathPatternParserServerWebExchangeMatcher(path/* ,HttpMethod.resolve("POST")不区分 */);
//		}).collect(Collectors.toList());
//		acceptMatcher = new OrServerWebExchangeMatcher(matchers);

		List<ServerWebExchangeMatcher> matchers = config.getAuthPathPatterns().stream().map(path -> {
			return new PathPatternParserServerWebExchangeMatcher(path);
		}).collect(Collectors.toList());
		authMatcher = new OrServerWebExchangeMatcher(matchers);

		this.appProvider = config.getAppProvider();
		this.openApiRequestValidator = config.getOpenApiRequestValidator();

		authenticationWebFilter = new AuthenticationWebFilter(config.getAuthenticationManager());

		authenticationWebFilter.setServerAuthenticationConverter(new AppServerAuthenticationConverter());

		authenticationWebFilter.setAuthenticationSuccessHandler(config.getServerAuthenticationSuccessHandler());

		/**
		 * 需要设置，默认使用的是HttpBasicServerAuthenticationEntryPoint
		 */
		authenticationWebFilter.setAuthenticationFailureHandler(config.getServerAuthenticationFailureHandler());
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String requestPath = exchange.getRequest().getURI().getPath();

//		if (!isAcceptPath(exchange)) {
//			LogUtils.infoIfEnabled(log, () -> log.info("request path:{} Not Accept", requestPath));
//
//			ServerHttpResponse response = exchange.getResponse();
//			response.setStatusCode(HttpStatus.FORBIDDEN);
//			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//			DataBufferFactory dataBufferFactory = response.bufferFactory();
//			DataBuffer buffer = dataBufferFactory.wrap("Path Not Accept".getBytes(CHARSET));
//			return response.writeWith(Mono.just(buffer));
//		}

		if (!isAuthPath(exchange)) {
			LogUtils.infoIfEnabled(log,
					() -> log.info("request path:{} not a AuthPath, ignore authentication", requestPath));
			return chain.filter(exchange);
		}

		/**
		 * 协议校验，在入口即检查，且响应按http status即可，这是约定的对接方式
		 */
		if (exchange.getRequest().getMethod() != HttpMethod.POST) {
			/**
			 * 要求POST
			 */
			ServerHttpResponse response = exchange.getResponse();

			response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);// 405
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return response.writeWith(Mono.empty());
		}
		MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
		if (!MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
				&& !MediaType.APPLICATION_JSON_UTF8.isCompatibleWith(mediaType)) {
			/**
			 * 要求application/json
			 */
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE);// 415
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return response.writeWith(Mono.empty());
		}

		/**
		 * 无法使用Cache Request Body，WebFilter的执行在所有GatewayFilter之前
		 */
		ServerHttpRequest request = exchange.getRequest();
		URI requestUri = request.getURI();
		String scheme = requestUri.getScheme();

		// Record only http requests (including https)
		if ((!"http".equals(scheme) && !"https".equals(scheme))) {
			return chain.filter(exchange);
		}

		Object cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
		if (cachedBody != null) {
			return chain.filter(exchange);
		}

		return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, (serverHttpRequest) -> {
			final ServerRequest serverRequest = ServerRequest
					.create(exchange.mutate().request(serverHttpRequest).build(), messageReaders);
			/**
			 * 转换检查json格式错误，缓存类型为OpenApiRequestBody提升性能
			 */
			return serverRequest.bodyToMono((OpenApiRequestBody.class)).doOnError(e -> {
				try {
					serverRequest.bodyToMono(String.class).doOnNext(objectValue -> {
						LogUtils.debugIfEnabled(log, () -> log.debug("cache body failed, request path:{} body:{}",
								requestPath, objectValue));
					});
				} catch (Exception e2) {
					log.error("ex on log request body after cache body error", e2);
				}

				/**
				 * json格式有问题，则响应错误码
				 */
				ServerHttpResponse response = exchange.getResponse();

				response.setStatusCode(HttpStatus.OK);
				response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
				DataBufferFactory dataBufferFactory = response.bufferFactory();

				ClientParameterInvalidErrorCodeException ece = new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(),
						"Invalid:Request Body");
				InternalApiResponse<Object> apiResponse = InternalApiResponse.fail(ece);

				DataBuffer buffer = dataBufferFactory.wrap(JsonUtils.serialize(apiResponse).getBytes(CHARSET));
				response.writeWith(Mono.just(buffer)).doOnError((error) -> DataBufferUtils.release(buffer)).subscribe();
			}).doOnNext(objectValue -> {
				LogUtils.debugIfEnabled(log, () -> log.debug("request path:{} body:{}", requestPath, objectValue));
				exchange.getAttributes().put(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR, objectValue);
			});
//					.then(Mono.defer(() -> {
//				ServerHttpRequest cachedRequest = exchange
//						.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
//				Assert.notNull(cachedRequest, "cache request shouldn't be null");
//				exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
//				return chain.filter(exchange.mutate().request(cachedRequest).build());
//			}));
		}).then(authenticationWebFilter.filter(exchange, chain));
	}

	/**
	 * 拒绝不支持的path
	 */
//	private boolean isAcceptPath(ServerWebExchange exchange) {
//		AtomicReference<Boolean> match = new AtomicReference<Boolean>();
//		Mono<MatchResult> mono = acceptMatcher.matches(exchange);
//		mono.subscribeOn(Schedulers.immediate());
//		mono.subscribe(next -> {
//			match.set(next.isMatch());
//		});
//		return match.get();
//	}

	/**
	 * 该认证只针对符合规范的openapi，其他的一律不做处理交给spring security识别是否需要认证<br>
	 * 因为历史开放接口可能不是按规范来的，则交给下游服务自行处理
	 */
	private boolean isAuthPath(ServerWebExchange exchange) {
		AtomicReference<Boolean> match = new AtomicReference<Boolean>();
		Mono<MatchResult> mono = authMatcher.matches(exchange);
		mono.subscribeOn(Schedulers.immediate());
		mono.subscribe(next -> {
			match.set(next.isMatch());
		});
		return match.get();
	}

	@Getter
	@ToString
	public static class Config {
//		private Set<String> acceptPathPatterns;
		private Set<String> authPathPatterns;
		private AppProvider appProvider;
		private OpenApiRequestValidator openApiRequestValidator;
		private ReactiveAuthenticationManager authenticationManager;
		private ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler;
		private ServerAuthenticationFailureHandler serverAuthenticationFailureHandler;

		public Config(Set<String> authPathPatterns, AppProvider appProvider,
				OpenApiRequestValidator openApiRequestValidator, ReactiveAuthenticationManager authenticationManager,
				ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
				ServerAuthenticationFailureHandler serverAuthenticationFailureHandler) {
			this.authPathPatterns = authPathPatterns;
			this.appProvider = appProvider;
			this.openApiRequestValidator = openApiRequestValidator;
			this.authenticationManager = authenticationManager;
			this.serverAuthenticationSuccessHandler = serverAuthenticationSuccessHandler;
			this.serverAuthenticationFailureHandler = serverAuthenticationFailureHandler;
		}
	}

	private class AppServerAuthenticationConverter implements ServerAuthenticationConverter {

		@Override
		public Mono<Authentication> convert(ServerWebExchange exchange) {
			return Mono.defer(() -> {
				String requestPath = exchange.getRequest().getURI().getPath();

				OpenApiRequestBody requestBody = CommonsGatewayUtils.getOpenApiRequestBody(exchange);

				if (requestBody != null) {

					if (!StringUtils.hasText(requestBody.getApp_id())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_APP_ID));
					}

					App app = appProvider.getApp(requestBody.getApp_id());
					if (app == null) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_APP_ID));
					}

					openApiRequestValidator.validate(requestPath, requestBody, app);

					/**
					 * 认证通过后
					 */
					SpringUser user = new SpringUser(requestBody.getApp_id(), app.getAppName(), "",
							Collections.emptyList());
					PreAuthenticatedAuthenticationToken authenticationToken = new PreAuthenticatedAuthenticationToken(
							user, "", Collections.emptyList());

					String flowTagRequired = app.getFlowTagRequired();
					String flowTagFirst = app.getFlowTagFirst();
					if (StringUtils.hasText(flowTagRequired) || StringUtils.hasText(flowTagFirst)) {
						Map<String, Object> details = new HashMap<String, Object>(1, 1);
						details.put("flowTagRequired", flowTagRequired);
						details.put("flowTagFirst", flowTagFirst);
						authenticationToken.setDetails(details);
					}

					return Mono.just(authenticationToken);
				}

				if (log.isWarnEnabled()) {
					log.warn("request body cache not exist");
				}
				return Mono.empty();
			});
		}
	}

}

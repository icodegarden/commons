package io.github.icodegarden.commons.gateway.core.security;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterMissingErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientPermissionErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.sign.AppKeySignUtils;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.spec.sign.RSASignUtils;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import io.github.icodegarden.commons.springboot.loadbalancer.FlowTagLoadBalancer;
import io.github.icodegarden.commons.springboot.security.SpringUser;
import io.github.icodegarden.commons.springboot.security.User;
import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class AppKeyAuthenticationWebFilter implements WebFilter {
	
	private static final Charset CHARSET = Charset.forName("utf-8");
	
	private static final Pattern DATETIME_PATTERN = Pattern
			.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$");

	private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
	private final AuthenticationWebFilter authenticationWebFilter;
	private final Map<String, App> appMap;
	private boolean headerAppKey;

	public AppKeyAuthenticationWebFilter(List<App> apps, ServerAuthenticationEntryPoint authenticationEntryPoint) {
		authenticationWebFilter = new AuthenticationWebFilter(new NoOpReactiveAuthenticationManager());
		appMap = apps.stream().collect(Collectors.toMap(App::getAppId, app -> app));
		
		authenticationWebFilter.setServerAuthenticationConverter(new SignResolveServerAuthenticationConverter());
		authenticationWebFilter
				.setAuthenticationSuccessHandler(new GatewayPreAuthenticatedServerAuthenticationSuccessHandler());

		/**
		 * 需要设置，默认使用的是HttpBasicServerAuthenticationEntryPoint
		 */
		authenticationWebFilter.setAuthenticationFailureHandler(
				new ApiResponseServerAuthenticationFailureHandler(authenticationEntryPoint));
	}
	
	public AppKeyAuthenticationWebFilter setHeaderAppKey(boolean headerAppKey) {
		this.headerAppKey = headerAppKey;
		return this;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		/**
		 * 该认证器只针对符合规范的openapi，其他的一律不做处理交给spring security识别是否需要认证<br>
		 * 因为像早期对接TC的/openapi/v1/bss/sync,/openapi/v1/bss/state,/openapi/v1/softwareParts/sync，不是按规范来的，则交给下游服务自行处理
		 */
		if (!"/openapi/v1/biz/methods".equals(exchange.getRequest().getURI().getPath())) {
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

	private class NoOpReactiveAuthenticationManager implements ReactiveAuthenticationManager {
		@Override
		public Mono<Authentication> authenticate(Authentication authentication) {
			/**
			 * 不用校验，能生成就代表通过
			 */
			return Mono.just(authentication);
		}
	}

	private class SignResolveServerAuthenticationConverter implements ServerAuthenticationConverter {

		@Override
		public Mono<Authentication> convert(ServerWebExchange exchange) {
			return Mono.defer(() -> {
				OpenApiRequestBody requestBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);

				if (requestBody != null) {

					if (!StringUtils.hasText(requestBody.getApp_id())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_APP_ID));
					}

					App app = appMap.get(requestBody.getApp_id());

					if (requestBody.getApp_id().length() > 32 || app == null) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_APP_ID));
					}

					// --------------------------------------------------

					if (!StringUtils.hasText(requestBody.getMethod())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_METHOD));
					}
					if (!StringUtils.hasText(requestBody.getSign())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_SIGNATURE));
					}
					if (!StringUtils.hasText(requestBody.getSign_type())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_SIGNATURE_TYPE));
					}
					if (!StringUtils.hasText(requestBody.getApp_id())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_APP_ID));
					}
					if (!StringUtils.hasText(requestBody.getTimestamp())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_TIMESTAMP));
					}
					if (!StringUtils.hasText(requestBody.getVersion())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_VERSION));
					}
					if (!StringUtils.hasText(requestBody.getRequest_id())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
								ClientParameterMissingErrorCodeException.SubPair.MISSING_REQUEST_ID));
					}

					// --------------------------------------

					if (!"JSON".equalsIgnoreCase(requestBody.getFormat())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_FORMAT));
					}
					if (!"SHA256".equals(requestBody.getSign_type()) && !"RSA2".equals(requestBody.getSign_type())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE_TYPE));
					}
					if (requestBody.getTimestamp().length() != 19
							|| !DATETIME_PATTERN.matcher(requestBody.getTimestamp()).matches()) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_TIMESTAMP));
					}
					if (!StringUtils.hasText(requestBody.getCharset())
							|| !"UTF-8".equalsIgnoreCase(requestBody.getCharset())) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_CHARSET));
					}
					if (!StringUtils.hasText(requestBody.getRequest_id())
							|| requestBody.getRequest_id().length() > 32) {
						// FIXME request_id 重复性检查
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_REQUEST_ID));
					}

					boolean b;
					if ("SHA256".equals(requestBody.getSign_type())) {
						b = AppKeySignUtils.validateRequestSign(requestBody, app.getAppKey());
					} else if ("RSA2".equals(requestBody.getSign_type())) {
						b = RSASignUtils.validateRequestSign(requestBody, app.getAppKey());
					} else {
						// 不会进来，上面已校验
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE_TYPE));
					}
					if (!b) {
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE));
					}

					/**
					 * 接口权限，如果没有配置则表示拥有所有接口权限
					 */
					if (!app.getMethods().isEmpty() && !app.getMethods().contains(requestBody.getMethod())) {
						throw new ErrorCodeAuthenticationException(new ClientPermissionErrorCodeException(
								ClientPermissionErrorCodeException.SubPair.INSUFFICIENT_PERMISSIONS));
					}

					SpringUser user = new SpringUser(requestBody.getApp_id(), app.getAppName(), "",
							Collections.emptyList());
					PreAuthenticatedAuthenticationToken authenticationToken = new PreAuthenticatedAuthenticationToken(user, "",
							Collections.emptyList());
					
					String flowTag = app.getFlowTag();
					if (StringUtils.hasText(flowTag)) {
						Map<String, Object> details = new HashMap<String, Object>(1, 1);
						details.put("flowTag", flowTag);
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

	private class ApiResponseServerAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

		private final ServerAuthenticationEntryPoint authenticationEntryPoint;

		public ApiResponseServerAuthenticationFailureHandler(ServerAuthenticationEntryPoint authenticationEntryPoint) {
			this.authenticationEntryPoint = authenticationEntryPoint;
		}

		@Override
		public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
				AuthenticationException exception) {
			return authenticationEntryPoint.commence(webFilterExchange.getExchange(), exception);
		}
	}

	private class GatewayPreAuthenticatedServerAuthenticationSuccessHandler
			implements ServerAuthenticationSuccessHandler {

		@Override
		public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
			return Mono.defer(() -> {
				WebFilterChain chain = webFilterExchange.getChain();
				ServerWebExchange exchange = webFilterExchange.getExchange();

				User principal = (User) authentication.getPrincipal();
				Map<String, Object> details = (Map) authentication.getDetails();

				ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
					httpHeaders.add(GatewayPreAuthenticatedAuthenticationFilter.HEADER_APPID, principal.getUserId());
					httpHeaders.add(GatewayPreAuthenticatedAuthenticationFilter.HEADER_APPNAME, principal.getUsername());
					if (details != null) {
						String flowTag = (String) details.get("flowTag");
						httpHeaders.add(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG, flowTag);
					}
					
					if(headerAppKey) {
						OpenApiRequestBody requestBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
						App app = appMap.get(requestBody.getApp_id());
						httpHeaders.add("X-Auth-AppKey", app.getAppKey());
					}
				}).build();

				return chain.filter(exchange.mutate().request(request).build());
			});
		}
	}
}
package io.github.icodegarden.commons.gateway.core.security.signature;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import io.github.icodegarden.commons.lang.spec.response.ClientPermissionErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.LogUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import io.github.icodegarden.commons.springboot.security.SpringUser;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class SignatureAuthenticationWebFilter implements AuthWebFilter {

	/**
	 * 可配
	 */
	public static int REJECT_SECONDS_BEFORE = 5 * 60;
	public static int REJECT_SECONDS_AFTER = 10;

	private static final Charset CHARSET = Charset.forName("utf-8");

	private static final Pattern DATETIME_PATTERN = Pattern
			.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$");

	private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
	private final Set<String> requiredAuthPaths;
	private final AuthenticationWebFilter authenticationWebFilter;
	private final AppProvider appProvider;
	private final OpenApiRequestValidator openApiRequestValidator;

	public SignatureAuthenticationWebFilter(Collection<String> requiredAuthPaths, AppProvider appProvider,
			OpenApiRequestValidator openApiRequestValidator, ReactiveAuthenticationManager authenticationManager,
			ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
			ServerAuthenticationFailureHandler serverAuthenticationFailureHandler) {
		this.requiredAuthPaths = new HashSet<String>(requiredAuthPaths);
		this.appProvider = appProvider;
		this.openApiRequestValidator = openApiRequestValidator;

		authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);

		authenticationWebFilter.setServerAuthenticationConverter(new AppServerAuthenticationConverter());

		authenticationWebFilter.setAuthenticationSuccessHandler(serverAuthenticationSuccessHandler);

		/**
		 * 需要设置，默认使用的是HttpBasicServerAuthenticationEntryPoint
		 */
		authenticationWebFilter.setAuthenticationFailureHandler(serverAuthenticationFailureHandler);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String requestPath = exchange.getRequest().getURI().getPath();

		/**
		 * 不需要，path如果不正确将无法匹配到访问的资源
		 */
//		if (!acceptPaths.contains(requestPath)) {
//			ServerHttpResponse response = exchange.getResponse();
//			response.setStatusCode(HttpStatus.FORBIDDEN);
//			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//			DataBufferFactory dataBufferFactory = response.bufferFactory();
//			DataBuffer buffer = dataBufferFactory.wrap("Invalid Path".getBytes(CHARSET));
//			return response.writeWith(Mono.just(buffer));
//		}

		/**
		 * 该认证器只针对符合规范的openapi，其他的一律不做处理交给spring security识别是否需要认证<br>
		 * 因为历史开放接口可能不是按规范来的，则交给下游服务自行处理
		 */
		if (!requiredAuthPaths.contains(requestPath)) {
			LogUtils.infoIfEnabled(log,
					() -> log.info("request path:{} not a RequiredAuthPath, ignore authentication", requestPath));
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
						LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_FORMAT:{}",
								app.getAppName(), app.getAppId(), requestPath, requestBody.getFormat()));
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_FORMAT));
					}
					if (!CommonsGatewayUtils.supportsSignType(requestBody.getSign_type())) {
						LogUtils.infoIfEnabled(log,
								() -> log.info("app:{}.{} of rquest path:{} INVALID_SIGNATURE_TYPE:{}",
										app.getAppName(), app.getAppId(), requestPath, requestBody.getSign_type()));
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE_TYPE));
					}
					if (requestBody.getTimestamp().length() != 19
							|| !DATETIME_PATTERN.matcher(requestBody.getTimestamp()).matches()) {
						LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_TIMESTAMP:{}",
								app.getAppName(), app.getAppId(), requestPath, requestBody.getTimestamp()));
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_TIMESTAMP));
					}
					/**
					 * n秒（例如5分钟）之前的视为重放;比现在晚n秒（例如10秒）以上视为不符合
					 */
					LocalDateTime ts = LocalDateTime.parse(requestBody.getTimestamp(),
							SystemUtils.STANDARD_DATETIME_FORMATTER);
					if (ts.plusSeconds(REJECT_SECONDS_BEFORE).isBefore(SystemUtils.now())
							|| ts.minusSeconds(REJECT_SECONDS_AFTER).isAfter(SystemUtils.now())) {
						LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_TIMESTAMP:{}",
								app.getAppName(), app.getAppId(), requestPath, requestBody.getTimestamp()));
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_TIMESTAMP));
					}
					if (!StringUtils.hasText(requestBody.getCharset())
							|| !"UTF-8".equalsIgnoreCase(requestBody.getCharset())) {
						LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_CHARSET:{}",
								app.getAppName(), app.getAppId(), requestPath, requestBody.getCharset()));
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_CHARSET));
					}

					boolean b = CommonsGatewayUtils.validateSign(requestBody, app.getAppKey());
					/**
					 * 验签不通过
					 */
					if (!b) {
						LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_SIGNATURE:{}",
								app.getAppName(), app.getAppId(), requestPath, requestBody.getSign()));
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE));
					}

					/**
					 * 接口权限，如果没有配置则表示拥有所有接口权限
					 */
					if (!app.getMethods().isEmpty() && !app.getMethods().contains(requestBody.getMethod())) {
						LogUtils.infoIfEnabled(log,
								() -> log.info("app:{}.{} of rquest path:{} INSUFFICIENT_PERMISSIONS:{}",
										app.getAppName(), app.getAppId(), requestPath, requestBody.getMethod()));
						throw new ErrorCodeAuthenticationException(new ClientPermissionErrorCodeException(
								ClientPermissionErrorCodeException.SubPair.INSUFFICIENT_PERMISSIONS));
					}

					/**
					 * 把request_id的校验放在签名校验之后，是因为校验防重放可能使用网络IO更耗时，以便前面的验证不通过直接拒绝
					 */
					if (!StringUtils.hasText(requestBody.getRequest_id()) || requestBody.getRequest_id().length() > 32
							|| !openApiRequestValidator.validate(requestBody)) {
						LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_REQUEST_ID:{}",
								app.getAppName(), app.getAppId(), requestPath, requestBody.getRequest_id()));
						throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
								ClientParameterInvalidErrorCodeException.SubPair.INVALID_REQUEST_ID));
					}

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

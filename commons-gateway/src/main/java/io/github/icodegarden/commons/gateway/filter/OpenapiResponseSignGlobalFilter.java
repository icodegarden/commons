package io.github.icodegarden.commons.gateway.filter;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory.Config;
import org.springframework.core.Ordered;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.gateway.core.security.App;
import io.github.icodegarden.commons.gateway.core.security.AppProvider;
import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.gateway.util.CommonsGatewayUtils;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import reactor.core.publisher.Mono;

/**
 * <h1>对OpenApiResponse设置签名</h1>
 * 
 * 这个filter的顺序是最前<br>
 * 这个类的生效条件是openapi模式<br>
 * 
 * 所有GlobalFilter的顺序在认证的WebFilter（AppKeyAuthenticationWebFilter、JWTAuthenticationWebFilter）之后，因此可以拿到已认证的身份信息（如果认证通过）<br>
 * 
 * @author Fangfang.Xu
 *
 */
@Component
public class OpenapiResponseSignGlobalFilter implements GlobalFilter, Ordered {

	public static final int ORDER = HIGHEST_PRECEDENCE;

	@Autowired
	private ServerCodecConfigurer codecConfigurer;
	@Autowired
	private Set<MessageBodyDecoder> bodyDecoders;
	@Autowired
	private Set<MessageBodyEncoder> bodyEncoders;

	@Autowired
	private CommonsGatewaySecurityProperties securityProperties;
	@Autowired
	private AppProvider appProvider;

	private GatewayFilter delegatorFilter;

	@PostConstruct
	private void init() {
		CommonsGatewaySecurityProperties.Signature signature = securityProperties.getSignature();
		if (signature != null) {
			Config config = new ModifyResponseBodyGatewayFilterFactory.Config();
			/**
			 * 指定了String后，setRewriteFunction就只会进String类型<br>
			 * 下游服务的返回体是json String，ServerErrorGlobalFilter的返回体也是json String，因此这里String正合适
			 */
			config.setInClass(String.class);
			config.setOutClass(Object.class);

			config.setRewriteFunction((exc, obj) -> {
				ServerWebExchange exchange = (ServerWebExchange) exc;

				OpenApiResponse openApiResponse;
				if (obj instanceof String) {
					/**
					 * 实际只会进这里
					 */
					openApiResponse = JsonUtils.deserialize(obj.toString(), OpenApiResponse.class);
				} else if (obj instanceof OpenApiResponse) {
					openApiResponse = (OpenApiResponse) obj;
				} else {
					/**
					 * 不可能到这里<br>
					 * 原样出去
					 */
					return Mono.just(obj);
				}

				if (!StringUtils.hasText(openApiResponse.getSign())) {
					/**
					 * response签名
					 */
					String appId = exchange.getRequest().getHeaders()
							.getFirst(GatewayPreAuthenticatedAuthenticationFilter.HEADER_APPID);
					if (appId != null) {
						OpenApiRequestBody requestBody = CommonsGatewayUtils.getOpenApiRequestBody(exchange);
						App app = appProvider.getApp(appId);
						String sign = CommonsGatewayUtils.responseSign(openApiResponse, requestBody.getSign_type(),
								app);
						openApiResponse.setSign(sign);
					}
				}

				return Mono.just(openApiResponse);
			});

			/**
			 * copy from
			 * org.springframework.cloud.gateway.config.GatewayAutoConfiguration.modifyResponseBodyGatewayFilterFactory(ServerCodecConfigurer,
			 * Set<MessageBodyDecoder>, Set<MessageBodyEncoder>)
			 */
			ModifyResponseBodyGatewayFilterFactory factory = new ModifyResponseBodyGatewayFilterFactory(
					codecConfigurer.getReaders(), bodyDecoders, bodyEncoders);
			/**
			 * 这是 ModifyResponseGatewayFilter
			 */
			this.delegatorFilter = factory.apply(config);
		}
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (delegatorFilter == null) {
			return chain.filter(exchange);
		}

		return delegatorFilter.filter(exchange, chain);
	}

	@Override
	public int getOrder() {
		return ORDER;
	}
}
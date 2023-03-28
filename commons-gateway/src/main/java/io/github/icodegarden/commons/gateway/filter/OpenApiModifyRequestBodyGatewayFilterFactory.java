package io.github.icodegarden.commons.gateway.filter;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 适用于只把biz_content交给下游
 * 
 * @author Fangfang.Xu
 * 
 */
public class OpenApiModifyRequestBodyGatewayFilterFactory
		extends AbstractGatewayFilterFactory<OpenApiModifyRequestBodyGatewayFilterFactory.Config> {

	private final List<HttpMessageReader<?>> messageReaders;

	public OpenApiModifyRequestBodyGatewayFilterFactory(List<HttpMessageReader<?>> messageReaders) {
		super(Config.class);
		this.messageReaders = messageReaders;
	}

	@Override
	@SuppressWarnings("unchecked")
	public GatewayFilter apply(Config config) {
		return new GatewayFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
				Class inClass = config.getInClass();
				ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);

				// TODO: flux or mono
				Mono<?> modifiedBody = serverRequest.bodyToMono(inClass)
						.flatMap(originalBody -> config.getRewriteFunction().apply(exchange, originalBody))
						.switchIfEmpty(Mono.defer(() -> (Mono) config.getRewriteFunction().apply(exchange, null)));

				BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, config.getOutClass());
				HttpHeaders headers = new HttpHeaders();
				headers.putAll(exchange.getRequest().getHeaders());

				// the new content type will be computed by bodyInserter
				// and then set in the request decorator
				headers.remove(HttpHeaders.CONTENT_LENGTH);

				// if the body is changing content types, set it here, to the bodyInserter
				// will know about it
				if (config.getContentType() != null) {
					headers.set(HttpHeaders.CONTENT_TYPE, config.getContentType());
				}
				CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
				return bodyInserter.insert(outputMessage, new BodyInserterContext())
						// .log("modify_request", Level.INFO)
						.then(Mono.defer(() -> {
							ServerHttpRequest decorator = decorate(exchange, headers, outputMessage);
							return chain.filter(exchange.mutate().request(decorator).build());
						})).onErrorResume((Function<Throwable, Mono<Void>>) throwable -> release(exchange,
								outputMessage, throwable));
			}

			@Override
			public String toString() {
				return filterToStringCreator(OpenApiModifyRequestBodyGatewayFilterFactory.this)
						.append("Content type", config.getContentType()).append("In class", config.getInClass())
						.append("Out class", config.getOutClass()).toString();
			}
		};
	}

	protected Mono<Void> release(ServerWebExchange exchange, CachedBodyOutputMessage outputMessage,
			Throwable throwable) {
		boolean b;
		try {
			Method isCachedMethod = outputMessage.getClass().getDeclaredMethod("isCached");
			isCachedMethod.setAccessible(true);
			b = (boolean) isCachedMethod.invoke(outputMessage);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		if (b) {
			return outputMessage.getBody().map(DataBufferUtils::release).then(Mono.error(throwable));
		}
		return Mono.error(throwable);
	}

	ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers,
			CachedBodyOutputMessage outputMessage) {
		return new ServerHttpRequestDecorator(exchange.getRequest()) {
			@Override
			public HttpHeaders getHeaders() {
				long contentLength = headers.getContentLength();
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.putAll(headers);
				if (contentLength > 0) {
					httpHeaders.setContentLength(contentLength);
				} else {
					// TODO: this causes a 'HTTP/1.1 411 Length Required' // on
					// httpbin.org
					httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
				}
				return httpHeaders;
			}

			@Override
			public Flux<DataBuffer> getBody() {
				return outputMessage.getBody();
			}
		};
	}

	public static class Config {

		private Class inClass = OpenApiRequestBody.class;

		private Class outClass = String.class;

		private String contentType = null;

		private RewriteFunction rewriteFunction = (exchange, value) -> {
//			Map message = JsonUtils.deserialize((String) value, Map.class);
//			Object object = message.get("biz_content");// 是字符串
//			return Mono.just(object.toString());
			
			/**
			 * 被缓存了的
			 */
			OpenApiRequestBody body = (OpenApiRequestBody)value;
			return Mono.just(body.getBiz_content());
		};

		public Class getInClass() {
			return inClass;
		}

		public Config setInClass(Class inClass) {
			this.inClass = inClass;
			return this;
		}

		public Class getOutClass() {
			return outClass;
		}

		public Config setOutClass(Class outClass) {
			this.outClass = outClass;
			return this;
		}

		public RewriteFunction getRewriteFunction() {
			return rewriteFunction;
		}

		public Config setRewriteFunction(RewriteFunction rewriteFunction) {
			this.rewriteFunction = rewriteFunction;
			return this;
		}

		public <T, R> Config setRewriteFunction(Class<T> inClass, Class<R> outClass,
				RewriteFunction<T, R> rewriteFunction) {
			setInClass(inClass);
			setOutClass(outClass);
			setRewriteFunction(rewriteFunction);
			return this;
		}

		public String getContentType() {
			return contentType;
		}

		public Config setContentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

	}

}

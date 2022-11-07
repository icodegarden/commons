package io.github.icodegarden.commons.gateway.filter;

import java.nio.charset.Charset;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

import io.github.icodegarden.commons.lang.spec.response.ClientLimitedErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientPermissionErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Component
public class ServerErrorGlobalFilter implements GlobalFilter, Ordered {
	private static final Charset CHARSET = Charset.forName("utf-8");
	
	private static final String CLIENT_LIMITED_LOG_MODULE = "Client-Limited Sentinel";
	
	//TODO common
	private @Nullable BlockException causeBlockException(Throwable t) {
		int counter = 0;
		Throwable cause = t;
		while (cause != null && counter++ < 10) {
			if (cause instanceof BlockException) {
				return (BlockException) cause;
			}
			cause = cause.getCause();
		}
		return null;
	}
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return chain.filter(exchange).onErrorResume(e -> {
			log.error("ex on do filter chain", e);
			
			/**
			 * 无需SentinelGatewayBlockExceptionHandler
			 */
			ErrorCodeException ece = toErrorCodeExceptionIfBlockException(e);			
			if(ece == null) {
				ece = new ServerErrorCodeException("global.error", e.getMessage(), e);
			}
			
			ServerHttpResponse response = exchange.getResponse();

			response.setStatusCode(HttpStatus.OK);
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			DataBufferFactory dataBufferFactory = response.bufferFactory();

			InternalApiResponse<Object> apiResponse = InternalApiResponse.fail(ece);

			DataBuffer buffer = dataBufferFactory.wrap(JsonUtils.serialize(apiResponse).getBytes(CHARSET));
			return response.writeWith(Mono.just(buffer)).doOnError((error) -> DataBufferUtils.release(buffer));
		});
	}
	
	//TODO common
	private ErrorCodeException toErrorCodeExceptionIfBlockException(Throwable t) {
		BlockException e = causeBlockException(t);
		if(e != null) {
			ErrorCodeException ece = null;
			/**
			 * 以下一律是触发了但没有降级
			 */
			if (e instanceof SystemBlockException) {
				ece = new ServerErrorCodeException("sentinel-system-limited", e.getRule().getResource(), e);
			} else if (e instanceof AuthorityException) {
				ece = new ClientPermissionErrorCodeException("client.sentinel-authority-limited",
						e.getRule().getResource());
			} else if (e instanceof FlowException) {
				ece = new ClientLimitedErrorCodeException("client.sentinel-flow-limited", e.getRule().getResource());
			} else if (e instanceof ParamFlowException) {
				if (SentinelIdentityParamFlowGlobalFilter.RESOURCE_NAME.equals(e.getRule().getResource())) {
					ece = new ClientLimitedErrorCodeException(ClientLimitedErrorCodeException.SubPair.APP_CALL_LIMITED);
				} else {
					ece = new ClientLimitedErrorCodeException("client.sentinel-paramflow-limited",
							e.getRule().getResource());
				}
			} else if (e instanceof DegradeException) {
				ece = new ServerErrorCodeException("sentinel-degrade-limited", e.getRule().getResource(), e);
			} else {
				ece = new ClientLimitedErrorCodeException(
						"client.sentinel-" + e.getClass().getSimpleName() + "-limited", e.getRule().getResource());
			}

			if (log.isWarnEnabled()) {
				log.warn("{} {}", CLIENT_LIMITED_LOG_MODULE, ece.getMessage(), e);
			}
			
			return ece;
		}
		
		return null;
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}
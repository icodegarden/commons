package io.github.icodegarden.commons.springboot.web.handler;

import java.lang.reflect.UndeclaredThrowableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

import io.github.icodegarden.commons.lang.spec.response.ApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ClientLimitedErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientPermissionErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.springboot.sentinel.SentinelEventObserverRegistry;

/**
 * 使用 @Bean <br>
 * 支持对Sentinel异常的适配
 * 
 * @author Fangfang.Xu
 *
 */
public class SentinelAdaptiveApiResponseReactiveExceptionHandler extends ApiResponseReactiveExceptionHandler {

	private static final Logger log = LoggerFactory
			.getLogger(SentinelAdaptiveApiResponseReactiveExceptionHandler.class);

	private static final String CLIENT_LIMITED_LOG_MODULE = "Client-Limited Sentinel";

	/**
	 * 为BlockException单独设立一个
	 */
	@ExceptionHandler(BlockException.class)
	public ResponseEntity<ApiResponse> onBlockException(ServerWebExchange exchange, BlockException e) throws Exception {
		SentinelEventObserverRegistry.getInstance().notifyBlockException(e);

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
			ece = new ClientLimitedErrorCodeException("client.sentinel-paramflow-limited", e.getRule().getResource());
		} else if (e instanceof DegradeException) {
			ece = new ServerErrorCodeException("sentinel-degrade-limited", e.getRule().getResource(), e);
		} else {
			ece = new ClientLimitedErrorCodeException("client.sentinel-" + e.getClass().getSimpleName() + "-limited",
					e.getRule().getResource());
		}

		if (log.isWarnEnabled()) {
			log.warn("{} {}", CLIENT_LIMITED_LOG_MODULE, ece.getMessage(), e);
		}
//		OpenApiRequestBody body = extractOpenApiRequestBody(request);
//		if (body != null) {
//			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
//		}
//
//		return ResponseEntity.ok(InternalApiResponse.fail(ece));

		/**
		 * 一律使用OpenApiResponse来构造即可<br>
		 * biz_code会被gateway补充，那里有BodyCache<br>
		 */
		return ResponseEntity.ok(OpenApiResponse.fail(null, ece));
	}

	/**
	 * 为UndeclaredThrowableException单独设立一个
	 */
	@ExceptionHandler(UndeclaredThrowableException.class)
	public ResponseEntity<ApiResponse> onUndeclaredThrowableException(ServerWebExchange exchange,
			UndeclaredThrowableException ex) throws Exception {
		BlockException blockException = causeBlockException(ex);
		if (blockException != null) {
			return onBlockException(exchange, blockException);
		}

		return onException(exchange, ex);
	}

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
}
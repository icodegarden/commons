package io.github.icodegarden.commons.springboot.web.handler;

import java.lang.reflect.UndeclaredThrowableException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.springboot.sentinel.SentinelEventObserverRegistry;

/**
 * 使用 @Bean <br>
 * 支持对Sentinel异常的适配
 * 
 * @author Fangfang.Xu
 *
 */
public class SentinelAdaptiveApiResponseExceptionHandler extends ApiResponseExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(SentinelAdaptiveApiResponseExceptionHandler.class);

	private static final String CLIENT_LIMITED_LOG_MODULE = "Client-Limited Sentinel";

	/**
	 * 为BlockException单独设立一个
	 */
	@ExceptionHandler(BlockException.class)
	public ResponseEntity<ApiResponse> onBlockException(HttpServletRequest request, BlockException e) throws Exception {
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
		OpenApiRequestBody body = extractOpenApiRequestBody(request);
		if (body != null) {
			return ResponseEntity.ok(OpenApiResponse.fail(body.getMethod(), ece));
		}

		return ResponseEntity.ok(InternalApiResponse.fail(ece));
	}

	/**
	 * 为UndeclaredThrowableException单独设立一个
	 */
	@ExceptionHandler(UndeclaredThrowableException.class)
	public ResponseEntity<ApiResponse> onUndeclaredThrowableException(HttpServletRequest request,
			UndeclaredThrowableException ex) throws Exception {
		BlockException blockException = causeBlockException(ex);
		if (blockException != null) {
			return onBlockException(request, blockException);
		}

		return onException(request, ex);
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
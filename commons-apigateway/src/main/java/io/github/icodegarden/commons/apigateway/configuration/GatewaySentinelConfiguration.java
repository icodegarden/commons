package io.github.icodegarden.commons.apigateway.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;

import io.github.icodegarden.commons.apigateway.filter.SentinelUserParamFlowGlobalFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(SphU.class)
@ConditionalOnProperty(value = "commons.gateway.sentinel.support.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@Slf4j
public class GatewaySentinelConfiguration {

	/**
	 * 只需这个，无需SentinelGatewayBlockExceptionHandler，由 ServerErrorGlobalFilter
	 * 取代SentinelGatewayBlockExceptionHandler<br>
	 * 顺序在ServerErrorGlobalFilter之后，因为需要ServerErrorGlobalFilter处理异常
	 */
	@Bean
	@Order(-1)
	public GlobalFilter sentinelGatewayFilter() {
		log.info("gateway init bean of SentinelGatewayFilter");
		return new SentinelGatewayFilter();
	}

	/**
	 * 顺序在SentinelGatewayFilter之后，先让SentinelGatewayFilter做第一层作用
	 */
	@Bean
	public GlobalFilter sentinelUserParamFlowGlobalFilter() {
		log.info("gateway init bean of SentinelUserParamFlowGlobalFilter");
		return new SentinelUserParamFlowGlobalFilter(-2);
	}
}
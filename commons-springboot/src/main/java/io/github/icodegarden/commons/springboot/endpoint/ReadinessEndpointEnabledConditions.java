package io.github.icodegarden.commons.springboot.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * 满足任一即可
 * 
 * @author Fangfang.Xu
 *
 */
public class ReadinessEndpointEnabledConditions extends AnyNestedCondition {

	public ReadinessEndpointEnabledConditions() {
		super(ConfigurationPhase.PARSE_CONFIGURATION);
	}

	/**
	 * 如果是网关
	 */
	@ConditionalOnClass(value = Endpoint.class, name = "org.springframework.cloud.gateway.filter.GlobalFilter")
	static class IsGateway {

	}

	/**
	 * 如果主动配置<br>
	 * 这里的 commons.endpoint.readiness.enabled 跟ReadinessEndpointAutoConfiguration上的区别是 这里要求配置指定，他们会分别识别
	 */
	@ConditionalOnProperty(value = "commons.endpoint.readiness.enabled", havingValue = "true", matchIfMissing = false)
	static class PropertyEnabled {

	}

}
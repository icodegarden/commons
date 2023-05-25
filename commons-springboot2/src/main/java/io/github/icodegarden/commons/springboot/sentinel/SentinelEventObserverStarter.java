package io.github.icodegarden.commons.springboot.sentinel;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class SentinelEventObserverStarter {

	public static void addDefaultLoggingObserver() {
		SentinelEventObserverRegistry.getInstance().addBlockExceptionObserver("logging", e -> {
			if (log.isWarnEnabled()) {
				AbstractRule rule = e.getRule();

				log.warn("Sentinel {} -> {}, identityId:{}, identityName:{}, requestId:{}, rule:{}",
						e.getClass().getSimpleName(), //
						rule.getResource(), //
						SecurityUtils.getUserId(), //
						SecurityUtils.getUsername(), //
						WebUtils.getRequestId(), //
						rule);
			}
		});

		SentinelEventObserverRegistry.getInstance().addCircuitBreakerObserver("logging",
				(prevState, newState, rule, snapshotValue) -> {
					if (newState == State.OPEN) {
						if (log.isWarnEnabled()) {
							log.warn(String.format(
									"Sentinel CircuitBreaker %s -> OPEN at %s, snapshotValue=%.2f, DegradeRule=%s",
									prevState.name(), SystemUtils.now(), snapshotValue, rule));
						}
					} else {
						if (log.isWarnEnabled()) {
							log.warn(String.format(
									"Sentinel CircuitBreaker %s -> %s at %s, snapshotValue=%.2f, DegradeRule=%s",
									prevState.name(), newState.name(), SystemUtils.now(), snapshotValue, rule));
						}
					}
				});
	}
}
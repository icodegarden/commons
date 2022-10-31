package io.github.icodegarden.commons.springboot.sentinel;

import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class SentinelEventStarter {

	public static void addDefaultLoggingObserver() {
		EventObserverRegistry.getInstance().addStateChangeObserver("logging",
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
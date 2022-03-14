package io.github.icodegarden.commons.exchange.exception;

import java.util.Collection;

import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;

/**
 * 实例交互失败且没有切换实例的必要
 * 
 * @author Fangfang.Xu
 *
 */
public class NoSwitchableExchangeException extends ExchangeException {
	private static final long serialVersionUID = 1L;

	public static final String MESSAGE = "No Switchable";

	public NoSwitchableExchangeException(Collection<MetricsInstance> candidates,
			Collection<ExchangeFailedInstance> exchangedInstances) {
		super(MESSAGE, candidates, exchangedInstances);
	}

}
package io.github.icodegarden.commons.exchange.exception;

import java.util.Collection;

import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;

/**
 * 所有实例交互失败
 * @author Fangfang.Xu
 *
 */
public class AllInstanceFailedExchangeException extends ExchangeException {
	private static final long serialVersionUID = 1L;

	public static final String MESSAGE = "All Instance Failed";

	public AllInstanceFailedExchangeException(Collection<MetricsInstance> candidates,
			Collection<ExchangeFailedInstance> exchangedInstances) {
		super(MESSAGE, candidates, exchangedInstances);
	}

}
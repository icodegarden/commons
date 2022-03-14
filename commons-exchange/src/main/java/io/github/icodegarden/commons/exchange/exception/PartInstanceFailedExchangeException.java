package io.github.icodegarden.commons.exchange.exception;

import java.util.Collection;

import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;

/**
 * 部分实例交互失败
 * 
 * @author Fangfang.Xu
 *
 */
public class PartInstanceFailedExchangeException extends ExchangeException {
	private static final long serialVersionUID = 1L;

	public static final String MESSAGE = "Part Instance Failed";

	private Collection<MetricsInstance> successInstances;

	public PartInstanceFailedExchangeException(Collection<MetricsInstance> candidates,
			Collection<ExchangeFailedInstance> exchangeFailedInstances, Collection<MetricsInstance> successInstances) {
		super(MESSAGE, candidates, exchangeFailedInstances);
		this.successInstances = successInstances;
	}

	public Collection<MetricsInstance> getSuccessInstances() {
		return successInstances;
	}
}
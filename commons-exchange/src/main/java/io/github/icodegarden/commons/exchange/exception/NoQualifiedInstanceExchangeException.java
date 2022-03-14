package io.github.icodegarden.commons.exchange.exception;

import java.util.Collection;

import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;

/**
 * 没有合格的候选实例
 * 
 * @author Fangfang.Xu
 *
 */
public class NoQualifiedInstanceExchangeException extends ExchangeException {
	private static final long serialVersionUID = 1L;

	public static final String MESSAGE = "No Qualified Instance";

	public NoQualifiedInstanceExchangeException(Collection<MetricsInstance> candidates) {
		super(MESSAGE, candidates, null);
	}

}
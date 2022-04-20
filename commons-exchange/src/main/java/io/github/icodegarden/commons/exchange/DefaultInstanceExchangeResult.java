package io.github.icodegarden.commons.exchange;

import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultInstanceExchangeResult implements InstanceExchangeResult {
	private static final long serialVersionUID = -5302046943952479128L;

	private boolean success;
	private MetricsInstance instance;
	private int exchangedTimes;
	private Object response;
	private ExchangeFailedReason failedReason;

	public DefaultInstanceExchangeResult() {

	}

	public DefaultInstanceExchangeResult(boolean success, MetricsInstance instance, int exchangedTimes, Object response,
			ExchangeFailedReason failedReason) {
		this.success = success;
		this.instance = instance;
		this.exchangedTimes = exchangedTimes;
		this.response = response;
		this.failedReason = failedReason;
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

	@Override
	public MetricsInstance instance() {
		return instance;
	}

	@Override
	public int exchangedTimes() {
		return exchangedTimes;
	}

	@Override
	public ExchangeFailedReason failedReason() {
		return failedReason;
	}

	@Override
	public Object response() {
		return response;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setInstance(MetricsInstance instance) {
		this.instance = instance;
	}

	public void setExchangedTimes(int exchangedTimes) {
		this.exchangedTimes = exchangedTimes;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public void setFailedReason(ExchangeFailedReason failedReason) {
		this.failedReason = failedReason;
	}

	@Override
	public String toString() {
		return "[success=" + success + ", instance=" + instance + ", exchangedTimes=" + exchangedTimes + ", response="
				+ response + ", failedReason=" + failedReason + "]";
	}

}
package io.github.icodegarden.commons.exchange;

import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ToString
public class ReasonExchangeResult implements ExchangeResult {
	private static final long serialVersionUID = 1L;

	private final boolean success;

	private Object result;
	
	private ExchangeFailedReason exchangeFailedReason;

	public ReasonExchangeResult(boolean success, Object result, ExchangeFailedReason exchangeFailedReason) {
		this.success = success;
		this.result = result;
		this.exchangeFailedReason = exchangeFailedReason;
	}

	@Override
	public Object response() {
		return result;
	}

	public ExchangeFailedReason getExchangeFailedReason() {
		return exchangeFailedReason;
	}

	public boolean isSuccess() {
		return success;
	}
}
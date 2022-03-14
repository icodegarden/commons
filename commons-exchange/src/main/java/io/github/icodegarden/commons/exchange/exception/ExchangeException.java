package io.github.icodegarden.commons.exchange.exception;

import java.util.Collection;
import java.util.Collections;

import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ExchangeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_MAX_MESSAGE_LENGTH = 65535;
	
	private Collection<MetricsInstance> candidates;
	private Collection<ExchangeFailedInstance> exchangedInstances;
	private int maxMessageLength = DEFAULT_MAX_MESSAGE_LENGTH;

	protected ExchangeException(String message, Collection<MetricsInstance> candidates,
			Collection<ExchangeFailedInstance> exchangedInstances) {
		super(message);
		this.candidates = candidates;
		this.exchangedInstances = exchangedInstances;
	}

	public Collection<MetricsInstance> getCandidates() {
		return candidates;
	}

	public Collection<ExchangeFailedInstance> getExchangedInstances() {
		return exchangedInstances;
	}

	public void setMaxMessageLength(int maxMessageLength) {
		this.maxMessageLength = maxMessageLength;
	}
	
	@Override
	public String getMessage() {
		String json = new Json(super.getMessage(), candidates, exchangedInstances).toJson();
		/**
		 * 预估长度是不会超出的，保险起见
		 */
		if (json.length() >= maxMessageLength) {
			json = new Json(super.getMessage(), Collections.emptyList(), exchangedInstances).toJson();
		}
		return json;
	}
	
	@SuppressWarnings("all")
	private class Json {
		private String message;
		private Collection<MetricsInstance> candidates;
		private Collection<ExchangeFailedInstance> exchangedInstances;
		public Json(String message, Collection<MetricsInstance> candidates,
				Collection<ExchangeFailedInstance> exchangedInstances) {
			this.message = message;
			this.candidates = candidates;
			this.exchangedInstances = exchangedInstances;
		} 
		
		public String getMessage() {
			return message;
		}

		public Collection<MetricsInstance> getCandidates() {
			return candidates;
		}

		public Collection<ExchangeFailedInstance> getExchangedInstances() {
			return exchangedInstances;
		}

		private String toJson() {
			return JsonUtils.serialize(this);
		}
	}

	public static class ExchangeFailedInstance {
		private MetricsInstance instance;
		private int exchangedTimes;// 交互次数
		private ExchangeFailedReason reason;// 失败原因

		public ExchangeFailedInstance(MetricsInstance instance, int exchangedTimes, ExchangeFailedReason reason) {
			this.instance = instance;
			this.exchangedTimes = exchangedTimes;
			this.reason = reason;
		}

		public MetricsInstance getInstance() {
			return instance;
		}

		public int getExchangedTimes() {
			return exchangedTimes;
		}

		public ExchangeFailedReason getReason() {
			return reason;
		}

		@Override
		public String toString() {
			return "[instance=" + instance + ", exchangedTimes=" + exchangedTimes + ", reason=" + reason + "]";
		}
	}

}

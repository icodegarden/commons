package io.github.icodegarden.commons.exchange;

import java.io.Serializable;

import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface InstanceExchangeResult extends Serializable {

	/**
	 * 
	 * @return 交互是否成功，而不在乎处理逻辑是否成功，只要服务端代码没有异常视为成功
	 */
	boolean isSuccess();

	/**
	 * 
	 * @return 发生交互的实例
	 */
	@Nullable
	MetricsInstance instance();

	/**
	 * @return 交互次数
	 */
	int exchangedTimes();

	/**
	 * 
	 * @return 返回结果
	 */
	@Nullable
	Object response();

	/**
	 * 
	 * @return {@link #isSuccess}是false时一定有
	 */
	@Nullable
	ExchangeFailedReason failedReason();

	/**
	 * 从server构造
	 * 
	 * @param success
	 * @param response
	 * @param failedReason
	 * @return
	 */
	public static InstanceExchangeResult server(boolean success, Object response, ExchangeFailedReason failedReason) {
		return new DefaultInstanceExchangeResult(success, null, 1, response, failedReason);
	}

	/**
	 * 从client构造，适合client端就已经决定结果，例如连接失败，也可以是成功的
	 * 
	 * @param success
	 * @param instance
	 * @param failedReason
	 * @return
	 */
	public static InstanceExchangeResult clientWithoutExchange(boolean success, MetricsInstance instance,
			ExchangeFailedReason failedReason) {
		return new DefaultInstanceExchangeResult(success, instance, 0, null, failedReason);
	}
	
	public static InstanceExchangeResult clientWithoutExchange(boolean success, Object response, MetricsInstance instance,
			ExchangeFailedReason failedReason) {
		return new DefaultInstanceExchangeResult(success, instance, 0, response, failedReason);
	}

	public static InstanceExchangeResult setInstance(MetricsInstance instance, InstanceExchangeResult result) {
		return new DefaultInstanceExchangeResult(result.isSuccess(), instance, result.exchangedTimes(), result.response(),
				result.failedReason());
	}

}

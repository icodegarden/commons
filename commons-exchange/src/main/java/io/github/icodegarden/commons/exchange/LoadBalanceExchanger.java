package io.github.icodegarden.commons.exchange;

import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;

/**
 * 使用负载均衡选择实例<br>
 * 
 * @author Fangfang.Xu
 */
public interface LoadBalanceExchanger<ER extends ExchangeResult> extends Exchanger<ER> {

	/**
	 * 使用指定的InstanceLoadBalance
	 * @param body
	 * @throws ExchangeException 所有的失败（包括处理结果的失败）都以该异常抛出
	 * @return 成功时才返回
	 */
	ER exchange(Object body, int timeout, InstanceLoadBalance instanceLoadBalance) throws ExchangeException;
}

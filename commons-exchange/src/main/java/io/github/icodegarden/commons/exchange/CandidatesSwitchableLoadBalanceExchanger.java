package io.github.icodegarden.commons.exchange;

import java.util.Queue;

import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;

/**
 * 
 * @author Fangfang.Xu
 */
public class CandidatesSwitchableLoadBalanceExchanger implements LoadBalanceExchanger<ShardExchangeResult> {

//	private static final Logger log = LoggerFactory.getLogger(CandidatesSwitchableLoadBalanceExchanger.class);

	private Protocol protocol;
	private final InstanceLoadBalance instanceLoadBalance;
	private final String serviceName;
	private final int maxCandidate;

	/**
	 * 
	 * @param instanceLoadBalance 默认InstanceLoadBalance
	 * @param serviceName
	 * @param maxCandidate
	 */
	public CandidatesSwitchableLoadBalanceExchanger(Protocol protocol, InstanceLoadBalance instanceLoadBalance,
			String serviceName, int maxCandidate) {
		this.protocol = protocol;
		this.instanceLoadBalance = instanceLoadBalance;
		this.serviceName = serviceName;
		this.maxCandidate = maxCandidate;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * 使用默认负载均衡选择实例，最多选择maxCandidate个实例</br>
	 * <h1>使用{@link CandidatesSwitchableExchanger}交互</h1>
	 */
	@Override
	public ShardExchangeResult exchange(Object body, int timeout) throws ExchangeException {
		return exchange(body, timeout, this.instanceLoadBalance);
	}

	/**
	 * 最多选择maxCandidate个实例</br>
	 * <h1>使用{@link CandidatesSwitchableExchanger}交互</h1>
	 */
	@Override
	public ShardExchangeResult exchange(Object body, int timeout, InstanceLoadBalance instanceLoadBalance)
			throws ExchangeException {
		Queue<MetricsInstance> candidates = instanceLoadBalance.selectCandidates(serviceName, maxCandidate);

		CandidatesSwitchableExchanger exchanger = new CandidatesSwitchableExchanger(protocol, candidates, true);
		return exchanger.exchange(body, timeout);
	}
}

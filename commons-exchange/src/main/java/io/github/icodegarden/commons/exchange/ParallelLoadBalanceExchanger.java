package io.github.icodegarden.commons.exchange;

import java.util.Objects;
import java.util.Queue;

import io.github.icodegarden.commons.exchange.ParallelExchanger.Config;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.annotation.NotNull;

/**
 * 使用负载均衡选择实例，并行的对选择的所有实例进行交互<br>
 * 
 * @author Fangfang.Xu
 */
public class ParallelLoadBalanceExchanger implements LoadBalanceExchanger<ParallelExchangeResult> {

//	private static final Logger log = LoggerFactory.getLogger(ParallelLoadBalanceExchanger.class);

	/**
	 * 默认的InstanceLoadBalance
	 */
	private final InstanceLoadBalance defaultInstanceLoadBalance;
	private final Config defaultConfig;
	private final String serviceName;
	private final ParallelExchanger parallelExchanger;

	/**
	 * 使用无界线程池
	 * 
	 * @param instanceLoadBalance
	 * @param serviceName
	 * @param maxCandidate
	 * @param maxShards
	 */
	public ParallelLoadBalanceExchanger(Protocol protocol, @NotNull InstanceLoadBalance defaultInstanceLoadBalance,
			String serviceName) {
		this(protocol, defaultInstanceLoadBalance, serviceName, new Config());
	}

	/**
	 * 
	 * @param instanceLoadBalance
	 * @param serviceName
	 * @param maxCandidate
	 * @param maxShards
	 * @param parallelThreadsTotal
	 */
	public ParallelLoadBalanceExchanger(Protocol protocol, @NotNull InstanceLoadBalance defaultInstanceLoadBalance,
			String serviceName, Config defaultConfig) {
		Objects.requireNonNull(defaultInstanceLoadBalance, "defaultInstanceLoadBalance must not null");
		
		this.defaultInstanceLoadBalance = defaultInstanceLoadBalance;
		this.serviceName = serviceName;
		this.defaultConfig = defaultConfig;

		parallelExchanger = new ParallelExchanger(protocol, null, defaultConfig);
	}

	public void setProtocol(Protocol protocol) {
		parallelExchanger.setProtocol(protocol);
	}
	
	/**
	 * 使用默认负载均衡
	 */
	@Override
	public ParallelExchangeResult exchange(Object body, int timeout) throws ExchangeException {
		return exchange(body, timeout, this.defaultInstanceLoadBalance);
	}

	/**
	 * 使用指定的负载均衡
	 */
	@Override
	public ParallelExchangeResult exchange(Object body, int timeout, InstanceLoadBalance instanceLoadBalance)
			throws ExchangeException {
		return exchange(body, timeout, instanceLoadBalance, defaultConfig);
	}

	/**
	 * 使用指定的负载均衡、指定的配置
	 */
	public ParallelExchangeResult exchange(Object body, int timeout, InstanceLoadBalance instanceLoadBalance,
			Config config) throws ExchangeException {
		/**
		 * 先选取所有实例
		 */
		Queue<MetricsInstance> candidates = instanceLoadBalance.selectCandidates(serviceName, Integer.MAX_VALUE);
		return parallelExchanger.exchange(body, timeout, candidates, config);
	}
}

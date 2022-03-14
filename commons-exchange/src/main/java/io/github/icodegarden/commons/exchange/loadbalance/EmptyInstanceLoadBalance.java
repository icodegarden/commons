package io.github.icodegarden.commons.exchange.loadbalance;

import java.util.Queue;

/**
 * 始终空实例<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class EmptyInstanceLoadBalance implements InstanceLoadBalance {

	@Override
	public Queue<MetricsInstance> selectCandidates(String serviceName, int maxCandidate) {
		return Constants.EMPTY_METRICS_INSTANCE;
	}

}

package io.github.icodegarden.commons.exchange.loadbalance;

import java.util.Queue;

/**
 * 负载均衡，即基于负载度量数据的均衡器
 * @author Fangfang.Xu
 *
 */
public interface InstanceLoadBalance {
	
	/**
	 * 
	 * @param serviceName
	 * @return Nullable 可以是达到负载上限的实例
	 */
	default MetricsInstance selectInstance(String serviceName) {
		Queue<MetricsInstance> candidates = selectCandidates(serviceName, 1);
		return candidates.poll();
	}
	/**
	 * 
	 * @param serviceName
	 * @param maxCandidate
	 * @return NotNull 可以包含达到负载上限的实例
	 */
	Queue<MetricsInstance> selectCandidates(String serviceName, int maxCandidate);
}

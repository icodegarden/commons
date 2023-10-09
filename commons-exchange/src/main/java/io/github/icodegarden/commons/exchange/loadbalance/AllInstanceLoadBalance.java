package io.github.icodegarden.commons.exchange.loadbalance;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.metricsregistry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AllInstanceLoadBalance implements InstanceLoadBalance {

	private final InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery;

	public AllInstanceLoadBalance(InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery) {
		this.instanceDiscovery = instanceDiscovery;
	}

	@Override
	public Queue<MetricsInstance> selectCandidates(String serviceName, int maxCandidate) {
		List<? extends RegisteredInstance> candidates = instanceDiscovery.listInstances(serviceName);
		if (candidates == null || candidates.isEmpty()) {
			return Constants.EMPTY_METRICS_INSTANCE;
		}
		
		/**
		 * 总是把发现的实例全部选中
		 */
		List<MetricsInstance> list = candidates.stream().map(candidate -> {
			return new DefaultMetricsInstance(candidate, Constants.IGNORE_METRICS);
		}).collect(Collectors.toList());

		return new LinkedList<MetricsInstance>(list);
	}

}
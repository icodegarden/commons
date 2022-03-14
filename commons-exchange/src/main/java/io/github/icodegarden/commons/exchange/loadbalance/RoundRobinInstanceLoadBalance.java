package io.github.icodegarden.commons.exchange.loadbalance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.util.CollectionUtils;

/**
 * 轮询，与度量无关<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class RoundRobinInstanceLoadBalance implements InstanceLoadBalance {

	private final InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery;
	
	private Map<String,Integer> serviceName_fromIndex = new HashMap<String, Integer>();

	public RoundRobinInstanceLoadBalance(InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery) {
		this.instanceDiscovery = instanceDiscovery;
	}

	@Override
	public Queue<MetricsInstance> selectCandidates(String serviceName, int maxCandidate) {
		if (maxCandidate <= 0) {
			throw new IllegalArgumentException(
					String.format("maxCandidate must gt 0 on selectCandidates, current is [%d]", maxCandidate));
		}
		List<? extends RegisteredInstance> candidates = instanceDiscovery.listInstances(serviceName);
		if (candidates == null || candidates.isEmpty()) {
			return Constants.EMPTY_METRICS_INSTANCE;
		}
		
		/**
		 * 目前在并发时可能出现不严格的轮询
		 */
		
		/**
		 * 有则直接取，没有则从0开始
		 */
		Integer fromIndex = serviceName_fromIndex.compute(serviceName, (k, v) -> {
			return v != null ? v : 0;
		});

		candidates = CollectionUtils.nextElements(candidates, fromIndex, maxCandidate);
		List<MetricsInstance> list = candidates.stream().map(candidate -> {
			return new MetricsInstance.Default(candidate, Constants.IGNORE_METRICS);
		}).collect(Collectors.toList());

		/**
		 * 增加轮询的索引位置
		 */
		fromIndex += maxCandidate;
		/**
		 * 
		 */
		if (fromIndex > candidates.size()) {
			fromIndex = 0;
		}
		
		serviceName_fromIndex.put(serviceName, fromIndex);

		return new LinkedList<MetricsInstance>(list);
	}

}

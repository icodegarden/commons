package io.github.icodegarden.commons.exchange.loadbalance;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.metricsregistry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceMetrics;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;

/**
 * 负载低的实例优先<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class MinimumLoadFirstInstanceLoadBalance implements InstanceLoadBalance {

	private final InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery;
	private final InstanceMetrics<? extends Metrics> instanceMetrics;
	private final MinimumLoadFirst algorithm = new MinimumLoadFirst();
	/**
	 * true：没有metrics的实例可以选择<br>
	 * 默认true，方便用户使用，此时实际上已不计MinimumLoadFirst
	 */
	private boolean allowNullMetrics = true;

	public MinimumLoadFirstInstanceLoadBalance(InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery,
			InstanceMetrics<? extends Metrics> instanceMetrics) {
		this.instanceDiscovery = instanceDiscovery;
		this.instanceMetrics = instanceMetrics;
	}

	public void setAllowNullMetrics(boolean allowNullMetrics) {
		this.allowNullMetrics = allowNullMetrics;
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

		List<? extends Metrics> metrics = instanceMetrics.listMetrics(serviceName);
		if (!allowNullMetrics) {
			if (metrics == null || metrics.isEmpty()) {
				return Constants.EMPTY_METRICS_INSTANCE;
			}
		}

		Queue<MetricsInstance> selectCandidates = algorithm.selectCandidates(candidates, metrics, maxCandidate);
		return selectCandidates;
	}

	/**
	 * 
	 * @author Fangfang.Xu
	 *
	 */
	static class MinimumLoadFirst {

		public Queue<MetricsInstance> selectCandidates(List<? extends RegisteredInstance> candidates,
				List<? extends Metrics> metrics, int maxCandidate) {
			Map<String, Metrics> map = metrics.stream().collect(Collectors.toMap(Metrics::getInstanceName, m -> m));

			List<MetricsInstance> sorted = candidates.stream().sorted((o1, o2/* pre */) -> {
				Metrics m1 = map.get(o1.getInstanceName());
				if (m1 == null) {
					return 1;
				}
				Metrics m2 = map.get(o2.getInstanceName());
				if (m2 == null) {
					return -1;
				}
				return m1.compareTo(m2);
			}).limit(maxCandidate).map(candidate -> {
				Metrics m = map.get(candidate.getInstanceName());
				return new DefaultMetricsInstance(candidate, m);
			}).collect(Collectors.toList());

			return new LoadBalancedInstanceQueue(sorted);
		}

		private class LoadBalancedInstanceQueue extends AbstractQueue<MetricsInstance> {
			private List<MetricsInstance> sorted;
			private int i;

			public LoadBalancedInstanceQueue(List<MetricsInstance> sorted) {
				this.sorted = sorted;
			}

			@Override
			public boolean offer(MetricsInstance e) {
				return sorted.add(e);
			}

			@Override
			public MetricsInstance poll() {
				if (sorted.isEmpty()) {
					return null;
				}
				if (i >= sorted.size()) {
					return null;
				}
				return sorted.get(i++);
			}

			@Override
			public MetricsInstance peek() {
				if (sorted.isEmpty()) {
					return null;
				}
				if (i >= sorted.size()) {
					return null;
				}
				return sorted.get(i);
			}

			@Override
			public Iterator<MetricsInstance> iterator() {
				return sorted.iterator();
			}

			@Override
			public int size() {
				return sorted.size();
			}
		}
	}
}

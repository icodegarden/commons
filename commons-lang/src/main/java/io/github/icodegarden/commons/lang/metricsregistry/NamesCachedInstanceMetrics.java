package io.github.icodegarden.commons.lang.metricsregistry;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import io.github.icodegarden.commons.lang.NamesCachedObjectReader;

/**
 *
 * 只关注给定的serviceNames<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class NamesCachedInstanceMetrics implements InstanceMetrics<Metrics> {

	private final InstanceMetrics<? extends Metrics> delegator;
	private final NamesCachedObjectReader namesCachedObjectReader;

	public NamesCachedInstanceMetrics(List<String> serviceNames, InstanceMetrics<? extends Metrics> delegator,
			long cacheRefreshIntervalMillis) throws IllegalArgumentException {
		if (serviceNames == null || serviceNames.isEmpty()) {
			throw new IllegalArgumentException("serviceNames must not empty");
		}
		if (delegator == null) {
			throw new IllegalArgumentException("param delegator must not null");
		}
		this.delegator = delegator;

		namesCachedObjectReader = new NamesCachedObjectReader(serviceNames, delegator, cacheRefreshIntervalMillis);
	}

	@Override
	public List<Metrics> listNamedObjects(String serviceName) {
		return (List) namesCachedObjectReader.listNamedObjects(serviceName);
	}

	@Override
	public <T extends RegisteredInstance> void setMetrics(T instance, Metrics metrics) {
		delegator.setMetrics(instance, metrics);
	}

	@Override
	public <T extends RegisteredInstance> Metrics getMetrics(T instance) {
		return delegator.getMetrics(instance);
	}

	@Override
	public <T extends RegisteredInstance> void removeMetrics(T instance) {
		delegator.removeMetrics(instance);
	}

	/**
	 * 停止调度
	 */
	@Override
	public void close() throws IOException {
		namesCachedObjectReader.close();
	}
}

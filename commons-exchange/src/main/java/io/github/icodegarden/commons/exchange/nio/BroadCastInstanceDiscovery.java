package io.github.icodegarden.commons.exchange.nio;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.Matcher;
import io.github.icodegarden.commons.lang.concurrent.registry.Instance;
import io.github.icodegarden.commons.lang.metricsregistry.DefaultRegisteredInstance;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T>
 */
class BroadCastInstanceDiscovery implements InstanceDiscovery<RegisteredInstance> {

	private final Supplier<List<Instance>> instancesSupplier;
	private final Matcher<Instance> instanceMatcher;

	public BroadCastInstanceDiscovery(Supplier<List<Instance>> instancesSupplier, Matcher<Instance> instanceMatcher) {
		this.instancesSupplier = instancesSupplier;
		this.instanceMatcher = instanceMatcher;
	}

	@Override
	public List<RegisteredInstance> listNamedObjects(String name) {
		List<Instance> instances = instancesSupplier.get();

		if (instances == null || instances.isEmpty()) {
			return Collections.emptyList();
		}

		List<RegisteredInstance> list = instances.stream()//
				.filter(instance -> instanceMatcher.matches(instance))//
				.map(instance -> {
					return new DefaultRegisteredInstance(instance.getName(), instance.getInstanceId(),
							instance.getHost(), instance.getPort());
				}).collect(Collectors.toList());
		return list;
	}

	@Override
	public RegisteredInstance parseInstance(Object data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
	}
}
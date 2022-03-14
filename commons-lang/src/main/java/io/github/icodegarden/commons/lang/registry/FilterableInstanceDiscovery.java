package io.github.icodegarden.commons.lang.registry;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class FilterableInstanceDiscovery<T extends RegisteredInstance> implements InstanceDiscovery<T> {

	private final Predicate<T> predicate;
	private final InstanceDiscovery<T> delegator;

	public FilterableInstanceDiscovery(Predicate<T> predicate, InstanceDiscovery<T> delegator) {
		if (predicate == null) {
			throw new IllegalArgumentException("predicate must not null");
		}
		if (delegator == null) {
			throw new IllegalArgumentException("delegator must not null");
		}
		this.predicate = predicate;
		this.delegator = delegator;
	}

	@Override
	public List<T> listNamedObjects(String name) {
		List<T> instances = delegator.listInstances(name);
		return instances.stream().filter(predicate).collect(Collectors.toList());
	}
	
	@Override
	public void close() throws IOException {
		delegator.close();		
	}
}

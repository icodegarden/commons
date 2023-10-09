package io.github.icodegarden.commons.lang.metricsregistry;

import java.io.Closeable;
import java.util.List;

import io.github.icodegarden.commons.lang.NamedObjectReader;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface InstanceMetrics<M extends Metrics> extends NamedObjectReader<M>, Closeable {

	<T extends RegisteredInstance> void setMetrics(T instance, Metrics metrics);

	<T extends RegisteredInstance> Metrics getMetrics(T instance);

	default List<M> listMetrics(String serviceName) {
		return listNamedObjects(serviceName);
	}

	<T extends RegisteredInstance> void removeMetrics(T instance);
}

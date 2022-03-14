package io.github.icodegarden.commons.lang.registry;

import java.io.Closeable;
import java.util.List;

import io.github.icodegarden.commons.lang.NamedObjectReader;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface InstanceDiscovery<T extends RegisteredInstance> extends NamedObjectReader<T>, Closeable {

	default List<T> listInstances(String serviceName) {
		return listNamedObjects(serviceName);
	}

}

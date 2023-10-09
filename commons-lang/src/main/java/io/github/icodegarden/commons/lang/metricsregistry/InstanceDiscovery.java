package io.github.icodegarden.commons.lang.metricsregistry;

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
	
	/**
	 * 
	 * @param data
	 * @return Nullable 若参数不符合
	 */
	T parseInstance(Object data);

}

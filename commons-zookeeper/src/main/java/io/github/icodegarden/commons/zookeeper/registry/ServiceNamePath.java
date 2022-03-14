package io.github.icodegarden.commons.zookeeper.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ServiceNamePath {

	private static Map<String, String> map = new ConcurrentHashMap<String, String>();

	static String ensureServiceNamePath(ZooKeeperHolder zooKeeperHolder, String root, String serviceName) {
		String path = map.get(serviceName);
		if (path == null) {
			path = buildServiceNamePath(root, serviceName);
			zooKeeperHolder.ensureRootNode(path);
			map.put(serviceName, path);
		}
		return path;
	}
	
	static String buildServiceNamePath(String root, String serviceName) {
		return root + "/" + serviceName + "/instances";
	}
}

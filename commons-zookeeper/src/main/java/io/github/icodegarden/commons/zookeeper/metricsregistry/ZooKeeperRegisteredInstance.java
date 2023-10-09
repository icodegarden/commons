package io.github.icodegarden.commons.zookeeper.metricsregistry;

import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ZooKeeperRegisteredInstance extends RegisteredInstance {

	String getZnode();
}

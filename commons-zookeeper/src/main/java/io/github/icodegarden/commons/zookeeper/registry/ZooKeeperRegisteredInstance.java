package io.github.icodegarden.commons.zookeeper.registry;

import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ZooKeeperRegisteredInstance extends RegisteredInstance {

	String getZnode();
}

package io.github.icodegarden.commons.zookeeper.registry;

import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ZooKeeperRegisteredInstance extends RegisteredInstance {

	String getZnode();

	public static class Default extends RegisteredInstance.Default implements ZooKeeperRegisteredInstance {

		private String znode;

		public Default(String znode, String serviceName, String instanceName, String ip, int port) {
			super(serviceName, instanceName, ip, port);
			this.znode = znode;
		}

		public String getZnode() {
			return znode;
		}

		@Override
		public String toString() {
			return "[znode=" + znode + "," + super.toString() + "]";
		}

	}
}

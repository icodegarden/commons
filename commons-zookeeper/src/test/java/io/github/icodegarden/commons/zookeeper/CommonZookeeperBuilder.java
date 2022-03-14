package io.github.icodegarden.commons.zookeeper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder.Config;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CommonZookeeperBuilder extends PropertiesConfig {

	protected ZooKeeperHolder zkh;

	@BeforeEach
	void initZK() throws Exception {
		Config config = new ZooKeeperHolder.Config(zkConnectString, 30000, 10000);
		config.setAclAuth("xff:xff");
		zkh = new ZooKeeperHolder(config);
	}

	@AfterEach
	void closeZK() throws Exception {
		zkh.close();
	}

}

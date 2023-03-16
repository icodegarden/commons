package io.github.icodegarden.commons.zookeeper;

import io.github.icodegarden.commons.lang.sequence.SequenceManager;
import io.github.icodegarden.commons.lang.sequence.SequenceManagerTests;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder.Config;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZooKeeperSequenceManagerTests extends SequenceManagerTests {

	@Override
	protected SequenceManager getForOneProcess() {
		return newSequenceManager();
	}

	@Override
	protected SequenceManager newForMultiProcess() {
		return newSequenceManager();
	}

	private SequenceManager newSequenceManager() {
		Config config = new ZooKeeperHolder.Config(PropertiesConfig.zkConnectString, 30000, 10000);
		config.setAclAuth("xff:xff");
		ZooKeeperHolder zkh = new ZooKeeperHolder(config);

		return new ZooKeeperSequenceManager("GLOBAL", zkh);
	}

}

package io.github.icodegarden.commons.zookeeper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.zookeeper.NewZooKeeperListener;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder.Config;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZooKeeperHolderTests extends PropertiesConfig {

	@Test
	void addNewZooKeeperListener() throws Exception {
		Config config = new ZooKeeperHolder.Config(zkConnectString, 30000, 10000);
		config.setAclAuth("xff:xff");
		ZooKeeperHolder zooKeeperHolder = new ZooKeeperHolder(config);
		
		MyNewZooKeeperListener1 l1 = new MyNewZooKeeperListener1();
		MyNewZooKeeperListener2 l2 = new MyNewZooKeeperListener2();
		zooKeeperHolder.addNewZooKeeperListener(l1);
		zooKeeperHolder.addNewZooKeeperListener(l2);

		List<NewZooKeeperListener> listNewZooKeeperListeners = zooKeeperHolder.listNewZooKeeperListeners();

		assertThat(listNewZooKeeperListeners).isNotNull();
		assertThat(listNewZooKeeperListeners).hasSize(2);
		assertThat(listNewZooKeeperListeners.get(0)).isEqualTo(l2);
		assertThat(listNewZooKeeperListeners.get(1)).isEqualTo(l1);

	}

	class MyNewZooKeeperListener1 implements NewZooKeeperListener {
		@Override
		public void onNewZooKeeper() {
		}

		@Override
		public int order() {
			return 1;
		}
	}

	class MyNewZooKeeperListener2 implements NewZooKeeperListener {
		@Override
		public void onNewZooKeeper() {
		}

		@Override
		public int order() {
			return -1;
		}
	}
}

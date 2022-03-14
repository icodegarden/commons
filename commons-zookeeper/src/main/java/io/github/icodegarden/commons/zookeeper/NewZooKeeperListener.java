package io.github.icodegarden.commons.zookeeper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface NewZooKeeperListener {
	/**
	 * 当zk session过期，new ZooKeeper后触发<br>
	 * 可用于给zk设置watcher，因为新的zk创建后需要重新设置
	 */
	void onNewZooKeeper();

	/**
	 * 越小越靠前触发，可以为负数
	 * @return
	 */
	default int order() {
		return 0;
	}

}

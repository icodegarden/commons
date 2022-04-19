package io.github.icodegarden.commons.zookeeper.concurrent.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CuratorSupportLock implements DistributedLock {
	private static final Logger log = LoggerFactory.getLogger(CuratorSupportLock.class);

	private final CuratorFramework client;
	private final AutoReleaseIfLostListener autoReleaseIfLostListener;

	public CuratorSupportLock(CuratorFramework client) {
		if (CuratorFrameworkState.LATENT == client.getState()) {
			synchronized (client) {
				if (CuratorFrameworkState.LATENT == client.getState()) {
					client.start();
				}
			}
		}
		this.client = client;

		this.autoReleaseIfLostListener = new AutoReleaseIfLostListener(this);

		client.getConnectionStateListenable().addListener(autoReleaseIfLostListener);
	}

	/**
	 * 当不使用该对象时调用<br>
	 * 因为锁对象是可重用的，一般情况不需要destory
	 */
	public void destory() {
		// 因为这个
		client.getConnectionStateListenable().removeListener(autoReleaseIfLostListener);
		if (isAcquired()) {
			try {
				release();
			} catch (Exception ignore) {
				if (log.isWarnEnabled()) {
					log.warn("release lock failed on destory", ignore);
				}
			}
		}
	}

}

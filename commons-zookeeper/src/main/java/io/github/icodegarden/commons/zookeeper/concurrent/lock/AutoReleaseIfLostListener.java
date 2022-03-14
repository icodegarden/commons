package io.github.icodegarden.commons.zookeeper.concurrent.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;

/**
 * 
 * Error Handling It is strongly recommended that you add a
 * ConnectionStateListener and watch for SUSPENDED and LOST state changes. If a
 * SUSPENDED state is reported you cannot be certain that you still hold the
 * lock unless you subsequently receive a RECONNECTED state. If a LOST state is
 * reported it is certain that you no longer hold the lock.
 * 
 * @author Fangfang.Xu
 *
 */
class AutoReleaseIfLostListener implements ConnectionStateListener {

	private final DistributedLock distributedLock;

	public AutoReleaseIfLostListener(DistributedLock distributedLock) {
		this.distributedLock = distributedLock;
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		if (ConnectionState.LOST == newState) {
			/**
			 * isAcquired是通过本地变量判断的，并不需要查询zk，当lost时该方法是安全的且识别的是本地状态
			 */
			if (distributedLock.isAcquired()) {
				try {
					/**
					 * 由于lost，release会进行zk通讯一定会异常，但该方法首先是把本地的状态给处理掉，然后再处理zk，因此本地的isAcquired会被正确处理为false
					 */
					distributedLock.release();
				} catch (Exception ignore) {
				}
			}
		}
//		if(ConnectionState.CONNECTED == newState || ConnectionState.RECONNECTED == newState) {
//		}
	}
}

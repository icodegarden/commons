package io.github.icodegarden.commons.zookeeper;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.zookeeper.exception.ConnectTimeoutZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.ExceedExpectedZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.ZooKeeperException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperHolder implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(ZooKeeperHolder.class);

	private volatile boolean closeCalled;
	
	private final Config config;

	private volatile ZooKeeper zk;
	
	private boolean authInfoAdded;

	private List<NewZooKeeperListener> listeners = new CopyOnWriteArrayList<NewZooKeeperListener>();

	public ZooKeeperHolder(String connectString, int sessionTimeout, int connectTimeout) {
		this(new Config(connectString, sessionTimeout, connectTimeout));
	}
	
	public ZooKeeperHolder(Config config) {
		Objects.requireNonNull(config, "config must not null");
		
		this.config = config;
		newZooKeeper();
	}

	private void newZooKeeper() throws ZooKeeperException {
		authInfoAdded = false;
		try {
			StateWatcher stateWatcher = new StateWatcher();

			ZKClientConfig zkClientConfig = new ZKClientConfig();
			zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL, "zookeeper/" + config.getConnectString());

			zk = new ZooKeeper(config.getConnectString(), config.getSessionTimeout(), stateWatcher, zkClientConfig);
			if (log.isInfoEnabled()) {
				log.info("success new ZooKeeper, connectString:{}, sessionTimeout:{}", config.getConnectString(), config.getSessionTimeout());
			}
			stateWatcher.setZooKeeper(zk);

			if (!listeners.isEmpty()) {
				listeners.forEach(m -> {
					m.onNewZooKeeper();
				});
			}
		} catch (IOException e) {
			throw new ExceedExpectedZooKeeperException("ex on new ZooKeeper", e);
		}
	}

	public ZooKeeper getZK() throws IllegalStateException {
		if(closeCalled) {
			throw new IllegalStateException(ZooKeeperHolder.class.getSimpleName() + " was closed");
		}
		return zk;
	}

	/**
	 * 确保得到的zk当前的状态是connected的，但不保证在使用时还是连接上的，受限于zk自身的状态变化及时性
	 */
	public ZooKeeper getConnectedZK() throws ZooKeeperException {
		if(closeCalled) {
			throw new IllegalStateException(ZooKeeperHolder.class.getSimpleName() + " was closed");
		}
		if (zk.getState() != States.CONNECTED) {
			synchronized (this) {
				if (zk.getState() != States.CONNECTED) {
					try {
						this.wait(config.getConnectTimeout());
					} catch (InterruptedException ignore) {
					}
					if (zk.getState() != States.CONNECTED) {
						throw new ConnectTimeoutZooKeeperException("zookeeper connected timeout:" + config.getConnectTimeout());
					}
				}
			}
		}
		/**
		 * 当有配置auth时，在获取zk前把authInfo加进去
		 */
		if(config.getAclAuth() != null && !authInfoAdded) {
			zk.addAuthInfo("digest", config.getAclAuth().getBytes());
			authInfoAdded = true;
		}
		
		return zk;
	}

	public void addNewZooKeeperListener(NewZooKeeperListener listener) {
		listeners.add(listener);
		listeners.sort(Comparator.comparingInt(NewZooKeeperListener::order));
	}

	List<NewZooKeeperListener> listNewZooKeeperListeners() {
		return listeners;
	}
	/**
	 * 给外部使用
	 */
	@Override
	public void close() throws IOException {
		internalClose();
		closeCalled = true;
	}
	/**
	 * 给内部使用
	 * @throws IOException
	 */
	private void internalClose() throws IOException {
		try {
			zk.close();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private class StateWatcher implements Watcher {
		private ZooKeeper zk;

		void setZooKeeper(ZooKeeper zk) {
			this.zk = zk;
		}

		@Override
		public void process(WatchedEvent event) {
			/**
			 * Event.EventType.None连接状态事件
			 */
			if (event.getType() == Event.EventType.None) {
				if (log.isInfoEnabled()) {
					log.info("zk.state:{}, sessionId:{}", event.getState(), zk.getSessionId());
				}
				switch (event.getState()) {
				/**
				 * 每当建立连接时触发，包括new ZooKeeper、自动重连成功（只要session没过期就会在Disconnected后SyncConnected）
				 */
				case SyncConnected:
					synchronized (ZooKeeperHolder.this) {
						ZooKeeperHolder.this.notify();
					}
					break;
				/**
				 * 当网络不可用一段时间就会触发
				 */
				case Disconnected:
					break;
				/**
				 * 当由于网络等问题时间超过sessionTimeout就会触发
				 */
				case Expired:
					while (true) {
						log.warn("start create a new ZooKeeper after session Expired");
						try {
							/**
							 * IMPT 不可以直接使用close()方法，那是给外部调用使用的
							 */
							internalClose();
						} catch (IOException ignore) {
						}
						try {
							/**
							 * 当触发session过期时必须new新的ZooKeeper，否则永远无法再自动建立连接
							 */
							newZooKeeper();
							break;// break loop if success
						} catch (Exception e) {
							log.warn("ex on newZooKeeper", e);
							// continue loop
							try {
								// 稍后重试
								Thread.sleep(3000);
							} catch (InterruptedException e1) {
							}
						}
					}

					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * create if not exists
	 * 
	 * @param root
	 * @throws ZooKeeperException
	 */
	public void ensureRootNode(String root) throws ZooKeeperException {
		/**
		 * /a/b/c -> a/b/c
		 */
		String substring = root.substring(1);
		String[] nodes = substring.split("/");

		String path = "";
		for (String node : nodes) {
			path += "/" + node;
			try {
				Stat exists = getConnectedZK().exists(path, false);
				if (exists == null) {
					try {
						getConnectedZK().create(path, new byte[0], ACLs.AUTH_ALL_ACL, CreateMode.PERSISTENT);
					} catch (InterruptedException ignore) {
					} catch (KeeperException.NodeExistsException ignore) {
					}
				}
			} catch (InterruptedException ignore) {
			} catch (KeeperException e) {
				throw new ExceedExpectedZooKeeperException(String.format("ex on ensure root node of exists [%s]", path),
						e);
			}
		}
	}
	
	public static class Config {
		private final String connectString;
		private final int sessionTimeout;
		private final int connectTimeout;
		private String aclAuth;
		public Config(String connectString, int sessionTimeout, int connectTimeout) {
			if(connectString == null || connectString.isEmpty()) {
				throw new IllegalArgumentException("connectString must not empty");
			}
			this.connectString = connectString;
			this.sessionTimeout = sessionTimeout;
			this.connectTimeout = connectTimeout;
		}
		public String getAclAuth() {
			return aclAuth;
		}
		public void setAclAuth(String aclAuth) {
			this.aclAuth = aclAuth;
		}
		public String getConnectString() {
			return connectString;
		}
		public int getSessionTimeout() {
			return sessionTimeout;
		}
		public int getConnectTimeout() {
			return connectTimeout;
		}
		@Override
		public String toString() {
			return "Config [connectString=" + connectString + ", sessionTimeout=" + sessionTimeout + ", connectTimeout="
					+ connectTimeout + ", aclAuth=" + aclAuth + "]";
		}
		
	}
}

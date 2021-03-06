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
			zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL,
					"zookeeper/" + config.getConnectString());

			zk = new ZooKeeper(config.getConnectString(), config.getSessionTimeout(), stateWatcher, zkClientConfig);
			if (log.isInfoEnabled()) {
				log.info("success new ZooKeeper, connectString:{}, sessionTimeout:{}", config.getConnectString(),
						config.getSessionTimeout());
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
		if (closeCalled) {
			throw new IllegalStateException(ZooKeeperHolder.class.getSimpleName() + " was closed");
		}
		return zk;
	}

	/**
	 * ???????????????zk??????????????????connected????????????????????????????????????????????????????????????zk??????????????????????????????
	 */
	public ZooKeeper getConnectedZK() throws ZooKeeperException {
		if (closeCalled) {
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
						throw new ConnectTimeoutZooKeeperException(
								String.format("zookeeper connected timeout:%d, connectString:%s",
										config.getConnectTimeout(), config.getConnectString()));
					}
				}
			}
		}
		/**
		 * ????????????auth???????????????zk??????authInfo?????????
		 */
		if (!authInfoAdded && config.getAclAuth() != null) {
			synchronized (this) {
				if (!authInfoAdded) {
					zk.addAuthInfo("digest", config.getAclAuth().getBytes());
					authInfoAdded = true;
				}
			}
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
	 * ???????????????
	 */
	@Override
	public void close() throws IOException {
		internalClose();
		closeCalled = true;
	}

	/**
	 * ???????????????
	 * 
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
			 * Event.EventType.None??????????????????
			 */
			if (event.getType() == Event.EventType.None) {
				if (log.isInfoEnabled()) {
					log.info("zk.state:{}, sessionId:{}", event.getState(), zk.getSessionId());
				}
				switch (event.getState()) {
				/**
				 * ????????????????????????????????????new ZooKeeper??????????????????????????????session??????????????????Disconnected???SyncConnected???
				 */
				case SyncConnected:
					synchronized (ZooKeeperHolder.this) {
						ZooKeeperHolder.this.notify();
					}
					break;
				/**
				 * ??????????????????????????????????????????
				 */
				case Disconnected:
					break;
				/**
				 * ????????????????????????????????????sessionTimeout????????????
				 */
				case Expired:
					while (true) {
						log.warn("start create a new ZooKeeper after session Expired");
						try {
							/**
							 * IMPT ?????????????????????close()???????????????????????????????????????
							 */
							internalClose();
						} catch (IOException ignore) {
						}
						try {
							/**
							 * ?????????session???????????????new??????ZooKeeper??????????????????????????????????????????
							 */
							newZooKeeper();
							break;// break loop if success
						} catch (Exception e) {
							log.warn("ex on newZooKeeper", e);
							// continue loop
							try {
								// ????????????
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
			if (connectString == null || connectString.isEmpty()) {
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

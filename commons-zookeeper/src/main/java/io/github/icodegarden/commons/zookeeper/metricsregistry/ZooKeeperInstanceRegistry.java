package io.github.icodegarden.commons.zookeeper.metricsregistry;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.metricsregistry.InstanceRegistry;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.zookeeper.ACLs;
import io.github.icodegarden.commons.zookeeper.NewZooKeeperListener;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.exception.ExceedExpectedZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.InvalidDataSizeZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.ZooKeeperException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperInstanceRegistry implements InstanceRegistry<ZooKeeperRegisteredInstance>, NewZooKeeperListener {
	private static final Logger log = LoggerFactory.getLogger(ZooKeeperInstanceRegistry.class);

	private static final Pattern ZNODE_PATTERN = Pattern.compile("(.*)/(.*)/instances/(.*):(\\d+)-(.*)");
	private static final String IP = SystemUtils.getIp();

	private ZooKeeperHolder zooKeeperHolder;
//	private final String root;
	private final String serviceName;
	private final String bindIp;
	private final int port;
	private final String path;

	private boolean closed;

	private volatile ZooKeeperRegisteredInstance instance;
	private boolean deregistered;

	/**
	 * use default ip
	 * 
	 * @param zooKeeperHolder
	 * @param root            例如/beecomb
	 * @param serviceName     例如master
	 * @param port
	 */
	public ZooKeeperInstanceRegistry(ZooKeeperHolder zooKeeperHolder, String root, String serviceName, int port)
			throws IllegalArgumentException {
		this(zooKeeperHolder, root, serviceName, IP, port);
	}

	/**
	 * 
	 * @param zooKeeperHolder
	 * @param root            例如/beecomb
	 * @param serviceName     例如master
	 * @param bindIp
	 * @param port
	 */
	public ZooKeeperInstanceRegistry(ZooKeeperHolder zooKeeperHolder, String root, String serviceName, String bindIp,
			int port) throws IllegalArgumentException {
		if (zooKeeperHolder == null) {
			throw new IllegalArgumentException("param zooKeeperHolder must not null");
		}
		if (root == null || root.isEmpty()) {
			throw new IllegalArgumentException("param root must not empty");
		}
		if (!root.startsWith("/")) {
			throw new IllegalArgumentException("param root must start with /");
		}
		if (serviceName == null || serviceName.isEmpty()) {
			throw new IllegalArgumentException("param serviceName must not empty");
		}
		if (serviceName.startsWith("/")) {
			throw new IllegalArgumentException("param serviceName must not start with /");
		}
		if (bindIp == null || bindIp.isEmpty()) {
			throw new IllegalArgumentException("param bindIp must not empty");
		}
		this.zooKeeperHolder = zooKeeperHolder;
		this.serviceName = serviceName;
		this.bindIp = bindIp;
		this.port = port;

		zooKeeperHolder.addNewZooKeeperListener(this);

		path = RegistryServiceNamePath.ensureServiceNamePath(zooKeeperHolder, root, serviceName);
	}

	/**
	 * 
	 * @param znode /beecomb/worker/instances/10.33.211.12:10000-0000000115
	 * @throws IllegalArgumentException if not match
	 * @return
	 */
	public static ZooKeeperRegisteredInstance resovleRegisteredInstance(String znode) throws IllegalArgumentException {
		Matcher matcher = ZNODE_PATTERN.matcher(znode);
		if (matcher.find()) {
//			String root = matcher.group(1);
			String serviceName = matcher.group(2);
			String ip = matcher.group(3);
			String port = matcher.group(4);
			String seq = matcher.group(5);
			return new DefaultZooKeeperRegisteredInstance(znode, serviceName, ip + ":" + port + "-" + seq, ip,
					Integer.parseInt(port));
		}
		throw new IllegalArgumentException(
				String.format("can not resovle to RegisteredInstance, znode [%s] not match", znode));
	}

	@Override
	public synchronized ZooKeeperRegisteredInstance registerIfNot() throws ZooKeeperException {
		if (instance != null) {
			return instance;
//			throw new IllegalStateException(
//					String.format("node was registered [%s]", registered.get().getInstanceName()));
		}

		String nodeName = path + "/" + bindIp + ":" + port + "-";
		try {
			if (log.isInfoEnabled()) {
				log.info("register znode with prefix:{}", nodeName);
			}

			nodeName = zooKeeperHolder.getConnectedZK().create(nodeName, new byte[0], ACLs.AUTH_ALL_ACL,
					CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (KeeperException.NodeExistsException ignore) {
			// 当SEQUENTIAL时不会发生
			if (log.isInfoEnabled()) {
				log.info("found node:{} was exists on register, do re register", nodeName);
			}
			// continue code ...
		} catch (KeeperException | InterruptedException e) {
			throw new ExceedExpectedZooKeeperException(String.format("ex on register znode [%s]", nodeName), e);
		}

		ZooKeeperRegisteredInstance registerResult = new DefaultZooKeeperRegisteredInstance(nodeName, serviceName,
				nodeName.substring(nodeName.lastIndexOf("/") + 1, nodeName.length()), bindIp, port);
		instance = registerResult;
		return registerResult;
	}

	/**
	 * 在注册节点上设置数据，目前仅用于test
	 * 
	 * @param data
	 * @throws IllegalStateException
	 * @throws ZooKeeperException
	 */
	public void setData(byte[] data) throws IllegalStateException, ZooKeeperException {
		ZooKeeperRegisteredInstance instance = getRegistered();
		if (instance == null) {
			log.warn("registered insatnce not found on setData, cancel setData");
			return;
		}

		if (data.length >= InvalidDataSizeZooKeeperException.MAX_DATA_SIZE) {
			throw new InvalidDataSizeZooKeeperException(data.length);
		}
		try {
			zooKeeperHolder.getConnectedZK().setData(instance.getZnode(), data, -1);
		}
//		不可能发生
//		catch (KeeperException.BadVersionException ignore) {
//			try {
//				Stat stat = zooKeeperHolder.getConnectedZK().exists(instance.getZnode(), false);
//				zooKeeperHolder.getConnectedZK().setData(instance.getZnode(), data, stat.getVersion());
//			} catch (ZooKeeperException | KeeperException | InterruptedException e) {
//				throw new ExceedExpectedZooKeeperException(
//						String.format("ex on setData znode [%s]", instance.getZnode()), e);
//			}
//		} 
		catch (KeeperException | InterruptedException e) {
			throw new ExceedExpectedZooKeeperException(String.format("ex on setData znode [%s]", instance.getZnode()),
					e);
		}
	}

	@Override
	public void deregister() throws ZooKeeperException {
		deregisterInternal();

		deregistered = true;
	}

	private void deregisterInternal() throws ZooKeeperException {
		ZooKeeperRegisteredInstance registeredInstance = instance;
		if (registeredInstance != null) {// 防止重复调用deregister
			doDeregister(registeredInstance.getZnode());
		}
	}

	private void doDeregister(String znode) throws ZooKeeperException {
		try {
			/**
			 * 节点不存在，则忽略
			 */
			Stat stat = zooKeeperHolder.getConnectedZK().exists(znode, false);
			if (stat != null) {
				try {
					zooKeeperHolder.getConnectedZK().delete(znode, stat.getVersion());
				} catch (KeeperException.NoNodeException ignore) {
				}
			}
		} catch (KeeperException | InterruptedException ignore) {
			throw new ExceedExpectedZooKeeperException(String.format("ex on deregister znode [%s]", znode), ignore);
		}

		instance = null;
	}

	@Override
	public ZooKeeperRegisteredInstance getRegistered() {
		return instance;
	}

	@Override
	public void close() throws IOException {
		/**
		 * 只需要deregister
		 */
		try {
			deregister();
		} catch (Exception e) {
			throw new IOException(e);
		}

		closed = true;
	}

	private boolean isActive() {
		return !closed && !deregistered;
	}

	@Override
	public void onNewZooKeeper() {
		/**
		 * 已经被外部操作了，则不需要继续处理
		 */
		if (!isActive()) {
			return;
		}
		if (log.isInfoEnabled()) {
			log.info("registered node:{} after new ZooKeeper, do re register", instance.getInstanceName());
		}
		/**
		 * 重新注册直到成功<br>
		 * FIXME 需要最大重试次数?否则可能会一直阻塞ZooKeeperReconnectTimerTask的运行
		 */
		while (isActive()) {
			try {
				deregisterInternal();

				registerIfNot();
				break;
			} catch (Exception e) {
				log.error("ex on re register after new ZooKeeper re SyncConnected", e);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ignore) {
				}
				// continue loop
			}
		}
	}

	@Override
	public int order() {
		return Integer.MIN_VALUE;
	}
}

package io.github.icodegarden.commons.zookeeper.registry;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.registry.InstanceRegistry;
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

	private AtomicInteger versionRef = new AtomicInteger();// 初始是0
	private ZooKeeperHolder zooKeeperHolder;
//	private final String root;
	private final String serviceName;
	private final String bindIp;
	private final int port;
	private final String path;

	private volatile ZooKeeperRegisteredInstance registered;

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

		path = ServiceNamePath.ensureServiceNamePath(zooKeeperHolder, root, serviceName);
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
		if (registered != null) {
			return registered;
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
		registered = registerResult;
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
			zooKeeperHolder.getConnectedZK().setData(instance.getZnode(), data, versionRef.get());
			versionRef.incrementAndGet();
		} catch (KeeperException.BadVersionException ignore) {
			try {
				Stat stat = zooKeeperHolder.getConnectedZK().exists(instance.getZnode(), false);
				versionRef.set(stat.getVersion());
				zooKeeperHolder.getConnectedZK().setData(instance.getZnode(), data, stat.getVersion());
			} catch (ZooKeeperException | KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(
						String.format("ex on setData znode [%s]", instance.getZnode()), e);
			}
		} catch (KeeperException | InterruptedException e) {
			throw new ExceedExpectedZooKeeperException(String.format("ex on setData znode [%s]", instance.getZnode()),
					e);
		}
	}

	@Override
	public void deregister() throws ZooKeeperException {
		ZooKeeperRegisteredInstance registeredInstance = registered;
		if (registeredInstance != null) {// 防止重复调用deregister
			deregister(registeredInstance.getZnode());
		}
	}

	private void deregister(String znode) throws ZooKeeperException {
		try {
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

		registered = null;
	}

	@Override
	public ZooKeeperRegisteredInstance getRegistered() {
		return registered;
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
	}

	@Override
	public void onNewZooKeeper() {
		/**
		 * 已经通过deregister删除
		 */
		if (registered == null) {
			return;
		}
		if (log.isInfoEnabled()) {
			log.info("registered node:{} which session was expired, do re register", registered.getInstanceName());
		}
		/**
		 * 重新注册直到成功
		 */
		while (true) {
			try {
				/**
				 * 当出现NewZooKeeper时，zk中的临时节点一定没了，不需要进行deregister
				 * deregister(registered.getZnode());
				 */
				registered = null;
				registerIfNot();
				break;
			} catch (Exception e) {
				log.error("ex on re register after session re SyncConnected", e);
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

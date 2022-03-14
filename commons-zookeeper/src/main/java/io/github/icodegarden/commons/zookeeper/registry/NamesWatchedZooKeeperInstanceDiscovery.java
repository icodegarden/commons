package io.github.icodegarden.commons.zookeeper.registry;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.NamesCachedObjectReader;
import io.github.icodegarden.commons.zookeeper.NewZooKeeperListener;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.exception.ExceedExpectedZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.ZooKeeperException;

/**
 * 只关注给定的serviceNames<br>
 * NamesCached + watch实时变更<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class NamesWatchedZooKeeperInstanceDiscovery
		implements ZooKeeperInstanceDiscovery<ZooKeeperRegisteredInstance>, NewZooKeeperListener, Watcher {
	private static final Logger log = LoggerFactory.getLogger(NamesWatchedZooKeeperInstanceDiscovery.class);

	private ZooKeeperHolder zooKeeperHolder;
	private String root;
	private final List<String> serviceNames;
	private final NamesCachedObjectReader namesCachedObjectReader;

	/**
	 * 
	 * @param zooKeeperHolder
	 * @param root
	 * @param serviceNames               需要watch的serviceNames
	 * @param cacheRefreshIntervalMillis
	 * @throws IllegalArgumentException
	 */
	public NamesWatchedZooKeeperInstanceDiscovery(
			ZooKeeperInstanceDiscovery<? extends ZooKeeperRegisteredInstance> delegator,
			ZooKeeperHolder zooKeeperHolder, String root, List<String> serviceNames, long cacheRefreshIntervalMillis)
			throws IllegalArgumentException {
		if (zooKeeperHolder == null) {
			throw new IllegalArgumentException("param zooKeeperHolder must not null");
		}
		if (root == null || root.isEmpty()) {
			throw new IllegalArgumentException("param root must not empty");
		}
		if (!root.startsWith("/")) {
			throw new IllegalArgumentException("param root must start with /");
		}
		if (root.endsWith("/")) {
			throw new IllegalArgumentException("param root must not end with /");
		}
		if (serviceNames == null || serviceNames.isEmpty()) {
			throw new IllegalArgumentException("serviceNames must not empty");
		}
		this.zooKeeperHolder = zooKeeperHolder;
		this.root = root;
		this.serviceNames = serviceNames;

		namesCachedObjectReader = new NamesCachedObjectReader(serviceNames, delegator, cacheRefreshIntervalMillis);

		addWatchers();
	}

	@Override
	public void onNewZooKeeper() {
		/**
		 * 当new zk创建后需要重新add watcher
		 */
		addWatchers();
	}

	private void addWatchers() throws ZooKeeperException {
		for (String serviceName : serviceNames) {
			String path = ServiceNamePath.buildServiceNamePath(root, serviceName);
			try {
				// 递归他的子节点
				zooKeeperHolder.getConnectedZK().addWatch(path, this, AddWatchMode.PERSISTENT_RECURSIVE);
			} catch (KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(
						String.format("ex on addWatch,serviceName [%s], path [%s]", serviceName, path), e);
			}
		}
	}
	
	private void removeWatchers() throws ZooKeeperException {
		for (String serviceName : serviceNames) {
			String path = ServiceNamePath.buildServiceNamePath(root, serviceName);
			try {
				zooKeeperHolder.getConnectedZK().removeWatches(path, this, WatcherType.Any, true/*true时，当connection不可用时可以只移除本地*/);
			} catch (KeeperException.NoWatcherException e) {
				/**
				 * 没有该watcher，忽略
				 */
			} catch (KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(
						String.format("ex on removeWatch,serviceName [%s], path [%s]", serviceName, path), e);
			}
		}
	}

	@Override
	public List<ZooKeeperRegisteredInstance> listNamedObjects(String serviceName) throws ZooKeeperException {
		return (List) namesCachedObjectReader.listNamedObjects(serviceName);
	}

	/**
	 * path: /beecomb/worker/instances/10.33.211.12:10000-0000000115
	 */
	@Override
	public void process(WatchedEvent event) {
		switch (event.getType()) {
		case NodeCreated: {
			String znode = event.getPath();
			if (log.isInfoEnabled()) {
				log.info("watched znode was created, a instance was registered, znode:{}", znode);
			}
			ZooKeeperRegisteredInstance registeredInstance = ZooKeeperInstanceRegistry.resovleRegisteredInstance(znode);
			namesCachedObjectReader.addObject(registeredInstance);
		}
			break;
		case NodeDeleted: {
			String znode = event.getPath();
			if (log.isInfoEnabled()) {
				log.info("watched znode was deleted, a instance was deregistered, znode:{}", znode);
			}
			ZooKeeperRegisteredInstance registeredInstance = ZooKeeperInstanceRegistry.resovleRegisteredInstance(znode);
			namesCachedObjectReader.removeObject(registeredInstance);
		}

			break;
		default:
			break;
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			removeWatchers();
		}catch (Exception e) {
			log.error("WARN ex on removeWatchers when close", e);
		}
		
		namesCachedObjectReader.close();
	}
	
}

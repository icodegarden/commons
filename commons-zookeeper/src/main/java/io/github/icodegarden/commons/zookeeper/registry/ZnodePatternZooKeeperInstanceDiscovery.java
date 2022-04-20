package io.github.icodegarden.commons.zookeeper.registry;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.exception.ExceedExpectedZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.ZooKeeperException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZnodePatternZooKeeperInstanceDiscovery implements ZooKeeperInstanceDiscovery<ZooKeeperRegisteredInstance> {
	private static final Logger log = LoggerFactory.getLogger(ZnodePatternZooKeeperInstanceDiscovery.class);

	private ZooKeeperHolder zooKeeperHolder;
	private String root;

	/**
	 * 
	 * @param zooKeeperHolder
	 * @param root            例如/beecomb
	 */
	public ZnodePatternZooKeeperInstanceDiscovery(ZooKeeperHolder zooKeeperHolder, String root) throws IllegalArgumentException {
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
		this.zooKeeperHolder = zooKeeperHolder;
		this.root = root;
	}

	@Override
	public List<ZooKeeperRegisteredInstance> listNamedObjects(String serviceName) throws ZooKeeperException {
		if (serviceName == null || serviceName.isEmpty() || serviceName.startsWith("/")) {
			throw new IllegalArgumentException("param serviceName must not empty and not start with /");
		}
		String path = ServiceNamePath.ensureServiceNamePath(zooKeeperHolder, root, serviceName);
		try {
			List<String> children = zooKeeperHolder.getConnectedZK().getChildren(path, false);
			List<ZooKeeperRegisteredInstance> instances = children.stream().map(child -> {
				String nodeName = path + "/" + child;

//					String realName = child;
//					String[] ipport_seq = realName.split("-");
//					String[] ip_port = ipport_seq[0].split(":");
//					return new ZooKeeperRegisteredInstance.Default(nodeName, serviceName, child, ip_port[0],
//							Integer.parseInt(ip_port[1]));

				return parseInstance(nodeName);
			}).collect(Collectors.toList());

			return instances;
		} catch (KeeperException | InterruptedException e) {
			throw new ExceedExpectedZooKeeperException(String.format("ex on list instances where path [%s]", path), e);
		}
	}

	/**
	 * 需要完整znode
	 */
	@Override
	public ZooKeeperRegisteredInstance parseInstance(Object znodeStr) {
		if (znodeStr != null) {
			try {
				return ZooKeeperInstanceRegistry.resovleRegisteredInstance(znodeStr.toString());
			} catch (IllegalArgumentException e) {
				if (log.isWarnEnabled()) {
					log.warn("parseInstance failed", e);
				}
			}
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		/**
		 * 没有需要处理的
		 */
	}
}
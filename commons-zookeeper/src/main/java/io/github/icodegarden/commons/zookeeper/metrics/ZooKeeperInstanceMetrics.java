package io.github.icodegarden.commons.zookeeper.metrics;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import io.github.icodegarden.commons.zookeeper.ACLs;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.exception.ExceedExpectedZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.InvalidDataSizeZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.ZooKeeperException;

/**
 * ZooKeeperInstanceMetrics属于ZooKeeper系列的InstanceMetrics，因此是个接口而不是实现类，以便应对可能的扩展需求
 * @author Fangfang.Xu
 *
 */
public interface ZooKeeperInstanceMetrics<M extends Metrics> extends InstanceMetrics<M> {

	public class Default implements ZooKeeperInstanceMetrics<Metrics> {
		private static final Logger log = LoggerFactory.getLogger(Default.class);

		private ZooKeeperHolder zooKeeperHolder;
		private final String root;

		private AtomicInteger versionRef = new AtomicInteger();// 初始是0
		private Serializer<Object> serializer = new KryoSerializer();
		private Deserializer<Object> deserializer = new KryoDeserializer();

		/**
		 * 
		 * @param zooKeeperHolder
		 * @param root               例如注册的root是/beecomb/masters ， 那么这里应该是/beecomb
		 * @param registeredSupplier instanceName
		 */
		public Default(ZooKeeperHolder zooKeeperHolder, String root) throws IllegalArgumentException {
			if (zooKeeperHolder == null) {
				throw new IllegalArgumentException("param zooKeeperHolder must not null");
			}
			if (root == null || root.isEmpty()) {
				throw new IllegalArgumentException("param root must not empty");
			}
			if (!root.startsWith("/")) {
				throw new IllegalArgumentException("param root must start with /");
			}
			this.zooKeeperHolder = zooKeeperHolder;
			this.root = root;
		}

		@Override
		public <T extends RegisteredInstance> void setMetrics(T instance, Metrics metrics) throws ZooKeeperException {
			if (instance == null) {
				log.warn("registered insatnce not found on updateMetrics, cancel updateMetrics");
				return;
			}

			String path = ServiceNamePath.ensureServiceNamePath(zooKeeperHolder, root, instance.getServiceName());

			String nodeName = path + "/" + instance.getInstanceName();

			if (log.isDebugEnabled()) {
				log.debug("set metrics to zookeeper, znode:{}, metrics:{}", nodeName, metrics);
			}

			byte[] data = serializer.serialize(metrics);
			if (data.length >= InvalidDataSizeZooKeeperException.MAX_DATA_SIZE) {
				throw new InvalidDataSizeZooKeeperException(data.length);
			}
			try {
				zooKeeperHolder.getConnectedZK().setData(nodeName, data, versionRef.get());
				versionRef.incrementAndGet();
			} catch (KeeperException.NoNodeException ignore) {
				try {
					zooKeeperHolder.getConnectedZK().create(nodeName, data, ACLs.AUTH_ALL_ACL, CreateMode.EPHEMERAL);
				} catch (KeeperException | InterruptedException e) {
					throw new ExceedExpectedZooKeeperException(
							String.format("ex on updateMetrics after NoNodeException, znode [%s]", nodeName), e);
				}
				// log.warn("znode not found on updateMetrics, expect znode:{}", nodeName);
				// continue code ...
			} catch (KeeperException.BadVersionException ignore) {
				try {
					Stat stat = zooKeeperHolder.getConnectedZK().exists(nodeName, false);
					versionRef.set(stat.getVersion());
					zooKeeperHolder.getConnectedZK().setData(nodeName, data, stat.getVersion());
				} catch (ZooKeeperException | KeeperException | InterruptedException e) {
					throw new ExceedExpectedZooKeeperException(
							String.format("ex on updateMetrics znode [%s]", nodeName), e);
				}
			} catch (KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(String.format("ex on updateMetrics znode [%s]", nodeName),
						e);
			}
		}

		@Override
		public <T extends RegisteredInstance> Metrics getMetrics(T instance) throws ZooKeeperException {
			if (instance == null) {
				return null;
			}
			String path = ServiceNamePath.ensureServiceNamePath(zooKeeperHolder, root, instance.getServiceName());

			String nodeName = path + "/" + instance.getInstanceName();
			try {
				byte[] data = zooKeeperHolder.getConnectedZK().getData(nodeName, false, null);
				Metrics metrics = buildMetrics(instance.getInstanceName(), data);
				return metrics;
			} catch (KeeperException.NoNodeException ignore) {
				return null;
			} catch (KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(String.format("ex on getMetrics znode [%s]", nodeName), e);
			}
		}

		@Override
		public List<Metrics> listNamedObjects(String serviceName) throws IllegalArgumentException, ZooKeeperException {
			if (serviceName == null || serviceName.isEmpty() || serviceName.startsWith("/")) {
				throw new IllegalArgumentException("param name must not empty and not start with /");
			}
			String path = ServiceNamePath.ensureServiceNamePath(zooKeeperHolder, root, serviceName);

			List<String> children;
			try {
				children = zooKeeperHolder.getConnectedZK().getChildren(path, false);
			} catch (KeeperException.NoNodeException e) {
				log.warn("znode not found on listMetrics, expect znode:{}", path);
				return Collections.emptyList();
			} catch (KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(String.format("ex on list instances where path [%s]", path),
						e);
			}

			List<Metrics> instances = children.stream().map(child -> {
				String nodeName = path + "/" + child;
				try {
					byte[] data = zooKeeperHolder.getConnectedZK().getData(nodeName, false, null);
					Metrics metrics = buildMetrics(child, data);
					return metrics;
				} catch (KeeperException | InterruptedException e) {
					/**
					 * 跳过出错的实例
					 */
					log.error("WARNING ex on getData for listMetrics where znode:{}", nodeName, e);
					return null;
//					throw new ZooKeeperExceedExpectedException(
//							String.format("ex on getData for listMetrics where znode [%s]", nodeName), e);
				}
			}).filter(m -> m != null).collect(Collectors.toList());

			return instances;
		}

		private Metrics buildMetrics(String instanceName, byte[] data) {
			Metrics metrics = (Metrics) deserializer.deserialize(data);
			metrics.setInstanceName(instanceName);
			return metrics;
		}

		@Override
		public <T extends RegisteredInstance> void removeMetrics(T instance) throws ZooKeeperException {
			String znode = ServiceNamePath.buildServiceNamePath(root, instance.getServiceName()) + "/"
					+ instance.getInstanceName();

			try {
				Stat stat = zooKeeperHolder.getConnectedZK().exists(znode, false);
				if (stat != null) {
					try {
						zooKeeperHolder.getConnectedZK().delete(znode, stat.getVersion());
					} catch (KeeperException.NoNodeException ignore) {
					}
				}
			} catch (KeeperException | InterruptedException ignore) {
				throw new ExceedExpectedZooKeeperException(String.format("ex on removeMetrics znode [%s]", znode),
						ignore);
			}
		}
		
		@Override
		public void close() throws IOException {
			/**
			 * 没有需要处理的
			 */
		}
	}
}

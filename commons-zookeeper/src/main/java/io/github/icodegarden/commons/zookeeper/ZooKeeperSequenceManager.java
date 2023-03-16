package io.github.icodegarden.commons.zookeeper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.sequence.AtomicSequenceManager;
import io.github.icodegarden.commons.zookeeper.exception.ExceedExpectedZooKeeperException;

/**
 * 使用redis hash的原子操作，该hash key及其field不需要提前建
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperSequenceManager extends AtomicSequenceManager {

	private final long increment = 1;

	private ZooKeeperHolder zooKeeperHolder;

	private final String path;

	public ZooKeeperSequenceManager(String moduleName, ZooKeeperHolder zooKeeperHolder) {
		super(moduleName);
		Assert.notNull(zooKeeperHolder, "zooKeeperHolder must not null");
		this.zooKeeperHolder = zooKeeperHolder;

		path = ModuleNamePath.ensureModuleNamePath(zooKeeperHolder, "/icodegarden", moduleName);
	}

	@Override
	public long getIncrement() {
		return increment;
	}

	@Override
	public long nextMaxId() {
		String nodeName = path + "/seq-";
		try {
			/**
			 * 若使用临时节点模式，则需要再调用delete进行删除（不删除zkServer内存暴涨性能低） 或 session关闭（需要经常打开关闭连接性能低）<br>
			 */
			String node = zooKeeperHolder.getConnectedZK().create(nodeName, new byte[0], ACLs.AUTH_ALL_ACL,
					CreateMode.PERSISTENT_SEQUENTIAL_WITH_TTL/* TTL只支持持久化模式 */, null, 1/* 毫秒，必须大于0 */);
			
//			String node = zooKeeperHolder.getConnectedZK().create(nodeName, new byte[0], ACLs.AUTH_ALL_ACL,
//					CreateMode.EPHEMERAL_SEQUENTIAL);
//			zooKeeperHolder.getConnectedZK().delete(node, -1);

			String[] split = node.split("-");
			long id = Long.parseLong(split[1]);
			if (id == 0) {
				/**
				 * zk seq是从0开始的
				 */
				return nextMaxId();
			}
			return id;
		} catch (KeeperException | InterruptedException e) {
			throw new ExceedExpectedZooKeeperException(String.format("ex on nextMaxId [%s]", nodeName), e);
		}
	}

	private static class ModuleNamePath {

		private static Map<String, String> map = new ConcurrentHashMap<String, String>();

		static String ensureModuleNamePath(ZooKeeperHolder zooKeeperHolder, String root, String moduleName) {
			String path = map.get(moduleName);
			if (path == null) {
				path = buildModuleNamePath(root, moduleName);
				zooKeeperHolder.ensureRootNode(path);
				map.put(moduleName, path);
			}
			return path;
		}

		static String buildModuleNamePath(String root, String moduleName) {
			return root + "/" + moduleName + "/sequences";
		}
	}
}

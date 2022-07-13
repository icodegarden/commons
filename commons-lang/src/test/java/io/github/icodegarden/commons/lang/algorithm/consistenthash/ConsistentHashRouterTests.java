package io.github.icodegarden.commons.lang.algorithm.consistenthash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.algorithm.HashFunction;
import io.github.icodegarden.commons.lang.algorithm.MD5Function;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ConsistentHashRouterTests {

	@Test
	void new_withNullPNodes() {
		ConsistentHashRouter<PhysicalNode> router = new ConsistentHashRouter<PhysicalNode>(null,
				160/* 每个物理节点有多少个虚拟节点 */);

		int count = router.countExistingVirtualNodes();
		Assertions.assertThat(count).isEqualTo(0);
	}

	@Test
	void addOrUpdatePhysicalNode() {
		PhysicalNode c1 = new PhysicalNode("id-1000000");
		ConsistentHashRouter<PhysicalNode> router = new ConsistentHashRouter<PhysicalNode>(Arrays.asList(c1),
				160/* 每个物理节点有多少个虚拟节点 */);

		int count = router.countExistingVirtualNodes(c1);
		Assertions.assertThat(count).isEqualTo(160);

		router.addOrUpdatePhysicalNode(c1, 100);
		count = router.countExistingVirtualNodes(c1);
		Assertions.assertThat(count).isEqualTo(260);

		PhysicalNode c2 = new PhysicalNode("id-2000000");
		router.addOrUpdatePhysicalNode(c2, 200);
		count = router.countExistingVirtualNodes(c2);
		Assertions.assertThat(count).isEqualTo(200);
	}

	@Test
	void removePhysicalNode() {
		PhysicalNode c1 = new PhysicalNode("id-1000000");
		ConsistentHashRouter<PhysicalNode> router = new ConsistentHashRouter<PhysicalNode>(Arrays.asList(c1),
				160/* 每个物理节点有多少个虚拟节点 */);

		router.removePhysicalNode(c1);

		int count = router.countExistingVirtualNodes(c1);
		Assertions.assertThat(count).isEqualTo(0);
	}

	@Test
	void routePhysicalNode() {
		/**
		 * 物理节点
		 */
		PhysicalNode c1 = new PhysicalNode("id-1000000");
		PhysicalNode c2 = new PhysicalNode("id-2000000");
		PhysicalNode c3 = new PhysicalNode("id-3000000");

		HashFunction hashFunc = new MD5Function();
		System.out.println(hashFunc.hash(c1.getKey()));
		System.out.println(hashFunc.hash(c2.getKey()));
		System.out.println(hashFunc.hash(c3.getKey()));

		ConsistentHashRouter<PhysicalNode> router = new ConsistentHashRouter<PhysicalNode>(Arrays.asList(c1, c2, c3),
				160/* 每个物理节点有多少个虚拟节点 */);

		String key = "key-100";

		VirtualNode<PhysicalNode> node = router.routeNode(key);
		Assertions.assertThat(node).isNotNull();// 只要hash环节点数不为空，就不会null
		System.out.println(node.getKey());
	}

	/**
	 * 演示真实场景
	 */
	@Test
	void sample() {
		/**
		 * 物理节点
		 */
		List<PhysicalNode> list = new LinkedList<PhysicalNode>();
		for (int i = 0; i < 16; i++) {
			list.add(new PhysicalNode("id-" + i));
		}
		ConsistentHashRouter<PhysicalNode> router = new ConsistentHashRouter<PhysicalNode>(list,
				160/* 每个物理节点有多少个虚拟节点 */);

		// 待处理的keys
		Map<String, VirtualNode<PhysicalNode>> kns = new HashMap<String, VirtualNode<PhysicalNode>>();
		for (int i = 0; i < 100000; i++) {
			String key = UUID.randomUUID().toString();
			kns.put(key, null);
		}

		/**
		 * 把10W个key落入环中
		 */
		for (String key : kns.keySet()) {
			VirtualNode<PhysicalNode> virtualNode = router.routeNode(key);
			kns.put(key, virtualNode);
		}

		/**
		 * 随机某物理节点挂了
		 */
		int index = new Random().nextInt(16);
		PhysicalNode removeNode = list.remove(index);
		router.removePhysicalNode(removeNode);

		/**
		 * 物理节点挂掉后影响的keys需要重新路由，方式1查出影响的keys并重新路由，使用需要性能的场景
		 */
		List<String> keysToReRoute = kns.entrySet().stream().filter(entry -> {
			PhysicalNode physicalNode = entry.getValue().getPhysicalNode();
			if (physicalNode.equals(removeNode)) {
				return true;
			}
			return false;
		}).map(entry -> entry.getKey()).collect(Collectors.toList());
		for (String key : keysToReRoute) {
			router.routeNode(key);
		}

		/**
		 * 物理节点挂掉后影响的keys需要重新路由，方式2使用剩余的物理节点重建hash环，整体重新路由，适合数据规模不大的场景
		 */
		router = new ConsistentHashRouter<PhysicalNode>(list, 160/* 每个物理节点有多少个虚拟节点 */);
		for (String key : kns.keySet()) {
			VirtualNode<PhysicalNode> preVirtualNode = kns.get(key);
			VirtualNode<PhysicalNode> newVirtualNode = router.routeNode(key);
			/**
			 * 物理节点还在的，则依然落到对应的物理节点的虚拟节点
			 */
			if (!preVirtualNode.getPhysicalNode().equals(removeNode)) {
				Assertions.assertThat(preVirtualNode).isEqualTo(newVirtualNode);
			}
			/**
			 * 物理节点挂的，则落入新节点
			 */
			else {
				Assertions.assertThat(preVirtualNode).isNotEqualTo(newVirtualNode);
			}
		}
	}
}

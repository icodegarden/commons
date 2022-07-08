package io.github.icodegarden.commons.lang.algorithm.consistenthash;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import io.github.icodegarden.commons.lang.algorithm.Hasher;
import io.github.icodegarden.commons.lang.algorithm.MD5Hasher;

/**
 * 将节点对象散列到具有一定数量虚拟节点的散列环中<br>
 * 一致性hash的目的是当物理节点删除时，只需调整落到该节点的数据到新的物理节点，避免大规模的调整引起系统的不稳定
 * 
 * @author Fangfang.Xu
 */
public class ConsistentHashRouter<T extends Node> {

	private final SortedMap<Long, VirtualNode<T>> ring = new TreeMap<Long, VirtualNode<T>>();
	private final Hasher hasher;

	public ConsistentHashRouter(Collection<T> pNodes, int vNodeCount) {
		this(pNodes, vNodeCount, new MD5Hasher());
	}

	public ConsistentHashRouter(Collection<T> pNodes, int vNodeCount, Hasher hasher) {
		this.hasher = hasher;
		if (pNodes != null) {
			for (T pNode : pNodes) {
				addOrUpdatePhysicalNode(pNode, vNodeCount);
			}
		}
	}

	/**
	 * 在hash环上新增物理节点或在原物理节点上追加vNodeCount数量的虚拟节点<br>
	 * 
	 * 新增：每个虚拟机节点都新建，number递增<br>
	 * 追加：先查出已存在的虚拟节点数，在这个基础上追加vNodeCount数量的虚拟节点<br>
	 */
	public void addOrUpdatePhysicalNode(T pNode, int vNodeCount) {
		if (vNodeCount < 0)
			throw new IllegalArgumentException("illegal virtual node counts :" + vNodeCount);
		int existingCount = countExistingVirtualNodes(pNode);
		for (int i = 0; i < vNodeCount; i++) {
			VirtualNode<T> vNode = new VirtualNode<T>(pNode, i + existingCount);
			ring.put(hasher.hash(vNode.getKey()), vNode);
		}
	}

	/**
	 * 移除物理节点，把该物理节点对应的虚拟节点从环上remove
	 * 
	 * @return 移除的虚拟节点
	 */
	public void removePhysicalNode(T pNode) {
		Iterator<Entry<Long, VirtualNode<T>>> it = ring.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, VirtualNode<T>> next = it.next();
			VirtualNode<T> virtualNode = next.getValue();
			if (virtualNode.isVirtualNodeOf(pNode)) {
				it.remove();
			}
		}
	}

	/**
	 * 根据key找到该key应该落到的虚拟节点
	 */
	public VirtualNode<T> routeNode(String key) {
		if (ring.isEmpty()) {
			return null;
		}
		/**
		 * 用跟虚拟节点一样的hash算法
		 */
		Long hashVal = hasher.hash(key);
		/**
		 * tail后，得到的是 >= 传参的hash值的 那段hash环，有可能是空的
		 */
		SortedMap<Long, VirtualNode<T>> tailMap = ring.tailMap(hashVal);
		/**
		 * 不为空，应该落到最近的虚拟节点对应的物理节点； 为空，即相当于要落到整个hash环的第一个虚拟节点对应的物理节点
		 */
		Long nodeHashVal = !tailMap.isEmpty() ? tailMap.firstKey() : ring.firstKey();
		return ring.get(nodeHashVal);
	}

	/**
	 * 获取物理节点已存在的虚拟节点数量
	 */
	public int countExistingVirtualNodes(T pNode) {
		int count = 0;
		for (VirtualNode<T> vNode : ring.values()) {
			if (vNode.isVirtualNodeOf(pNode)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 获取所有的虚拟节点数量
	 */
	public int countExistingVirtualNodes() {
		return ring.size();
	}
}

package io.github.icodegarden.commons.lang.algorithm.consistenthash;

import java.util.Objects;

/**
 * 虚拟节点
 * 
 * @author Fangfang.Xu
 */
public class VirtualNode<T extends Node> implements Node {

	private final T physicalNode;
	private final int number;

	public VirtualNode(T physicalNode, int number) {
		this.number = number;
		this.physicalNode = physicalNode;
	}

	/**
	 * 虚拟节点的key=物理节点key-虚拟节点number
	 */
	@Override
	public String getKey() {
		return physicalNode.getKey() + "-" + number;
	}

	/**
	 * 是否指定物理节点的虚拟节点
	 */
	public boolean isVirtualNodeOf(T pNode) {
		return physicalNode.getKey().equals(pNode.getKey());
	}

	public T getPhysicalNode() {
		return physicalNode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(number, physicalNode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VirtualNode<T> other = (VirtualNode<T>) obj;
		return number == other.number && Objects.equals(physicalNode, other.physicalNode);
	}

	@Override
	public String toString() {
		return "VirtualNode [number=" + number + "]";
	}
	
}

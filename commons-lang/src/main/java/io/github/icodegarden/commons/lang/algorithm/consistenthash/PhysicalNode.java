package io.github.icodegarden.commons.lang.algorithm.consistenthash;

import java.util.Objects;

/**
 * 物理节点
 * 
 * @author Fangfang.Xu
 *
 */
public class PhysicalNode implements Node {

	private final String key;

	public PhysicalNode(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhysicalNode other = (PhysicalNode) obj;
		return Objects.equals(key, other.key);
	}

	@Override
	public String toString() {
		return "PhysicalNode [key=" + key + "]";
	}
	
}
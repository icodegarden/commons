package io.github.icodegarden.commons.lang.datastructure.tree;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <V>
 */
class Node {
	
	private int hash;
	private Object value;
	
	public Node(int hash, Object value) {
		this.hash = hash;
		this.value = value;
	}

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Node [hash=" + hash + ", value=" + value + "]";
	}

}
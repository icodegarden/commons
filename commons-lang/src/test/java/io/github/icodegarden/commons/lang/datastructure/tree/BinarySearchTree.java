package io.github.icodegarden.commons.lang.datastructure.tree;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 二叉查找树（二叉排序树）的定义：<br>
 * 要么是空树，要么递归 左（右）子树上的所有节点小（大）于父节点的值
 * 
 * 特性：一个无序序列可以通过构建一棵二叉查找树，从而变成一个有序序列（中序遍历）
 * 
 * @author Fangfang.Xu
 *
 */
class BinarySearchTree extends BinaryTree<Node> {

	private static final Logger log = LoggerFactory.getLogger(BinarySearchTree.class);

	private BinarySearchTree(BinarySearchTree parent, Node value, int level) {
		super(parent, value, level);
	}

	/**
	 * @param list 可以是乱序，创建后用中序遍历即为排序好的结果
	 * @return 无节点则null
	 */
	public static BinarySearchTree build(List<Node> list) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		if (list.size() == 1) {
			return new BinarySearchTree(null, list.get(0), 1);
		}

		Iterator<Node> iterator = list.iterator();

		BinarySearchTree root = new BinarySearchTree(null, iterator.next(), 1);
		while (iterator.hasNext()) {
			root.insert(iterator.next());
		}

		return root;
	}

	/**
	 * 找到节点
	 * 
	 * @param hash
	 * @return
	 */
	public Node search(int hash) {
		BinarySearchTree node = searchInternal(hash);
		return node != null ? node.value : null;
	}

	private BinarySearchTree searchInternal(int hash) {
		if (value.getHash() == hash) {
			return this;
		}
		if (hash < value.getHash()) {
			if (left == null) {
				return null;
			} else {
				return ((BinarySearchTree) left).searchInternal(hash);
			}
		}
		if (hash > value.getHash()) {
			if (right == null) {
				return null;
			} else {
				return ((BinarySearchTree) right).searchInternal(hash);
			}
		}

		return null;
	}

	/**
	 * 新增节点，必须是root节点进行操作
	 * 
	 * @param root
	 * @param node
	 */
	public void insert(Node node) {
		if (level != 1) {
			throw new IllegalArgumentException("append node must be a root node");
		}
		append(this, node, 2);
	}

	/**
	 * 二叉查找树T新增节点N的算法： 若T是一棵空树，根节点指针（引用）指向节点N，N成树的根节点，否则； 若N的键值等于根节点的键值，返回失败，否则；
	 * 若N的键值小于根节点的键值，将N节点新增到左子树上，否则； 把N新增到右子树上；
	 * 
	 * @param node  正在匹配的节点
	 * @param next  正在增加的节点
	 * @param level 对应所在层级
	 */
	private void append(BinarySearchTree node, Node next, int level) {
		if (log.isDebugEnabled()) {
			log.debug("append on level:{}", level);
		}
		if (next.getHash() == node.value.getHash()) {
			throw new IllegalArgumentException("replicate hash node:" + next);
		}
		if (next.getHash() < node.value.getHash()) {
			if (node.left == null) {
				BinarySearchTree nodeL = new BinarySearchTree(node, next, level);
				node.left = nodeL;
			} else {
				append((BinarySearchTree) node.left, next, level + 1);
			}
		}

		if (next.getHash() > node.value.getHash()) {
			if (node.right == null) {
				BinarySearchTree nodeR = new BinarySearchTree(node, next, level);
				node.right = nodeR;
			} else {
				append((BinarySearchTree) node.right, next, level + 1);
			}
		}
	}

	public BinarySearchTree remove(int hash) {
		BinarySearchTree node = searchInternal(hash);
		if (node != null) {
			BinaryTree<Node> parent = node.parent;
			if (parent == null) {// root
				throw new IllegalArgumentException("can not remove root");// FIXME root怎么删除
			}

			if (node.left == null && node.right == null) {
				// 结点 p 为叶子结点，此时只需要删除该结点，并修改其双亲结点的指针即可；
				if (parent.left != null && parent.left.value.getHash() == node.value.getHash()) {
					parent.left = null;
				} else if (parent.right != null && parent.right.value.getHash() == node.value.getHash()) {
					parent.right = null;
				} else {
					throw new IllegalStateException("tree has a problem found by remove");// 不可能发生
				}
			} else if (node.left != null && node.right == null) {
				// 结点 p 只有左子树或者只有右子树，此时只需要将其左子树或者右子树直接变为结点 p 双亲结点的左子树即可；
				node.left.level = node.level;
				if (parent.left != null && parent.left.value.getHash() == node.value.getHash()) {
					parent.left = node.left;
				} else if (parent.right != null && parent.right.value.getHash() == node.value.getHash()) {

					parent.right = node.left;
				} else {
					throw new IllegalStateException("tree has a problem found by remove");// 不可能发生
				}
			} else if (node.right != null && node.left == null) {
				// 结点 p 只有左子树或者只有右子树，此时只需要将其左子树或者右子树直接变为结点 p 双亲结点的左子树即可；
				node.right.level = node.level;
				if (parent.left != null && parent.left.value.getHash() == node.value.getHash()) {
					parent.left = node.right;
				} else if (parent.right != null && parent.right.value.getHash() == node.value.getHash()) {
					parent.right = node.right;
				} else {
					throw new IllegalStateException("tree has a problem found by remove");// 不可能发生
				}
			} else {
				// 结点 p 左右子树都有，此时有两种处理方式：

				// 1）令结点 p 的左子树为其双亲结点的左子树；结点 p 的右子树为其自身直接前驱结点的右子树
				node.left.level = node.level;// left的level上升到P的level
				parent.left = node.left;
				append((BinarySearchTree) node.left/* 新的直接前驱节点=P的左节点 */, node.right.value, node.left.level/* 新节点的层级 */);
			}
		}
		return node;
	}

	public BinarySearchTree getLeft() {
		return (BinarySearchTree) left;
	}

	public BinarySearchTree getRight() {
		return (BinarySearchTree) right;
	}

}

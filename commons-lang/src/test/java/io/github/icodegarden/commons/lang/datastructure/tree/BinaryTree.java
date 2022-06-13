package io.github.icodegarden.commons.lang.datastructure.tree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 二叉树的定义：<br>
 * 它是有序树，即数据是有序的，例如1,2,3,4,5... A,B,C,D,E...
 *             图1  1
 *  	          |   |
 *                2   3
 *               | | 
 *              4  5 
 *              
 *             图2  A
 *  	          |   |
 *                B   C
 *               | | | |
 *               D E F G
 * 节点的度（分支数）最多2个，可以是0、1、2个
 * 
 * 二叉树的特性：
 * 第i层最多有2(i-1次)个节点，即树高3则第3层最多4个节点.
 * 如果深度为K，那么此树最多有2(K次)-1个节点，即树高3最多7个节点.
 * 叶子节点数(n0)=度为2的节点数(n2)+1，如图1叶子是3、4、5，度为2的节点是1、2，图2叶子是D、E、F、G，度为2个节点是A、B、C
 * 
 * 二叉树的形态：
 * 空树
 * 只有根节点
 * 只有左子树
 *      1
 *     |
 *     2
 * 只有右子树
 *      1
 *       |
 *       3
 *        
 * 满二叉树，除了叶子结点，每个结点的度都为 2，则此二叉树称为满二叉树
 * 例如图2
满二叉树除了满足普通二叉树的性质，还具有以下性质：
满二叉树中第 i 层的节点数为 2n-1 个。
深度为 k 的满二叉树必有 2k-1 个节点 ，叶子数为 2k-1。
满二叉树中不存在度为 1 的节点，每一个分支点中都两棵深度相同的子树，且叶子节点都在最底层。
具有 n 个节点的满二叉树的深度为 log2(n+1)。
 * 
 * 完全二叉树，如果除去最后一层节点为满二叉树，且最后一层的结点依次从左到右分布，则此二叉树被称为完全二叉树
 *                  1  是
 *  	          |   |
 *                2   3
 *               | | | 
 *              4  5 6
 *              
 *                  1  不是，最后不是从左到右
 *  	          |   |
 *                2   3
 *               | |   | 
 *              4  5   7
完全二叉树除了具有普通二叉树的性质，它自身也具有一些独特的性质，比如说，n 个结点的完全二叉树的深度为 ⌊log2n⌋+1。
⌊log2n⌋ 表示取小于 log2n 的最大整数。例如，⌊log24⌋ = 2，而 ⌊log25⌋ 结果也是 2。

对于任意一个完全二叉树来说，如果将含有的结点按照层次从左到右依次标号（如图 3a)），对于任意一个结点 i ，完全二叉树还有以下几个结论成立：
当 i>1 时，父亲结点为结点 [i/2] 。（i=1 时，表示的是根结点，无父亲结点）
如果 2*i>n（总结点的个数） ，则结点 i 肯定没有左孩子（为叶子结点）；否则其左孩子是结点 2*i 。
如果 2*i+1>n ，则结点 i 肯定没有右孩子；否则右孩子是结点 2*i+1 。


http://data.biancheng.net/view/192.html
 * @author Fangfang.Xu
 *
 */
class BinaryTree<T> {
	
	private static final Logger log = LoggerFactory.getLogger(BinaryTree.class);
	
	@JsonIgnore
	protected BinaryTree<T> parent;
	protected T value;
	protected BinaryTree<T> left;
	protected BinaryTree<T> right;
	/**
	 * 第几层
	 */
	protected int level;

	protected BinaryTree(BinaryTree<T> parent, T value, int level) {
		this.parent = parent;
		this.value = value;
		this.level = level;
	}

	/**
	 * @param list 已排序好的数据，允许包含null元素
	 * @return 无节点则null
	 */
	public static <T>BinaryTree<T> buildBySortedList(List<T> list) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		if (list.size() == 1) {
			return new BinaryTree<T>(null, list.get(0), 1);
		}

		Iterator<T> iterator = list.iterator();

		BinaryTree<T> root = new BinaryTree<T>(null, iterator.next(), 1);
		appendLeafNodes(Arrays.asList(root), iterator, 2);

		return root;
	}
	
	/**
	 * 
	 * @param leafNodes 所有叶子节点，从左到右
	 * @param iterator
	 * @param level     第几层
	 * @return
	 */
	private static <T> void appendLeafNodes(List<BinaryTree<T>> leafNodes, Iterator<T> iterator,
			int level) {
		if (!iterator.hasNext()) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("appendLeafNodes on level:{}", level);
		}
		LinkedList<BinaryTree<T>> newLeafNodes = new LinkedList<BinaryTree<T>>();

		for (BinaryTree<T> leafNode : leafNodes) {
			if (iterator.hasNext()) {
				T valueL = iterator.next();
				if (valueL != null) {
					BinaryTree<T> nodeL = new BinaryTree<T>(leafNode, valueL, level);
					leafNode.left = nodeL;

					newLeafNodes.add(nodeL);
				}
			}
			if (iterator.hasNext()) {
				T valueR = iterator.next();
				if (valueR != null) {
					BinaryTree<T> nodeR = new BinaryTree<T>(leafNode, valueR, level);
					leafNode.right = nodeR;

					newLeafNodes.add(nodeR);
				}
			}
		}

		appendLeafNodes(newLeafNodes, iterator, ++level);
	}

	/**
	 * 树高
	 * 
	 * @return
	 */
	public int treeHeight() {
		int h = level;
		if (left != null) {
			h = Math.max(h, left.treeHeight());
		}
		if (right != null) {
			h = Math.max(h, right.treeHeight());
		}

		return h;
	}
	
	/**
	 * 第几层有多少个节点
	 * @param level
	 * @return
	 */
	public int totalNodes(int level) {
		/**
		 * root层或自己这一层
		 */
		if (this.level == level) {
			return 1;
		}
		
		int count = 0;
//		if (this.level + 1 == level) {
//			count = this.left != null ? count + 1 : count;
//			count = this.right != null ? count + 1 : count;
//			return count;
//		}

		if (left != null) {
			count += left.totalNodes(level);
		}
		if (right != null) {
			count += right.totalNodes(level);
		}
		return count;
	}
	/**
	 * 整个树一共多少个节点，包括根
	 * @return
	 */
	public int totalNodes() {
		int treeHeight = treeHeight();

		int totalNodes = 0;
		for (int level = 1; level <= treeHeight; level++) {// 层数是从1开始数的
			int totalNodesOfLevel = totalNodes(level);
			if (log.isDebugEnabled()) {
				log.debug("level:{} having totalNodes:{}", level, totalNodesOfLevel);
			}
			totalNodes += totalNodesOfLevel;
		}
		return totalNodes;
	}
	/**
	 * 整个树一共多少叶子节点
	 * @return
	 */
	public int totalLeafNodes() {
		/**
		 * 根节点或叶子节点
		 */
		if (left == null && right == null) {
			return 1;
		}

		int count = 0;
		if (left != null) {
			count += left.totalLeafNodes();
		}
		if (right != null) {
			count += right.totalLeafNodes();
		}
		return count;
	}
	
	/**
	 * 整个树一共多少度为2的节点
	 * @return
	 */
	public int totalFullDegreeNodes() {
		int count = 0;
		if (left != null && right != null) {
			count = 1;
		}
		
		if (left != null) {
			count += left.totalFullDegreeNodes();
		}
		if (right != null) {
			count += right.totalFullDegreeNodes();
		}
		return count;
	}
	
	/**
访问该二叉树的根节点，找到 1；
访问节点 1 的左子树，找到节点 2；
访问节点 2 的左子树，找到节点 4；
由于访问节点 4 左子树失败，且也没有右子树，因此以节点 4 为根节点的子树遍历完成。但节点 2 还没有遍历其右子树，因此现在开始遍历，即访问节点 5；
由于节点 5 无左右子树，因此节点 5 遍历完成，并且由此以节点 2 为根节点的子树也遍历完成。现在回到节点 1 ，并开始遍历该节点的右子树，即访问节点 3；
访问节点 3 左子树，找到节点 6；
由于节点 6 无左右子树，因此节点 6 遍历完成，回到节点 3 并遍历其右子树，找到节点 7；
节点 7 无左右子树，因此以节点 3 为根节点的子树遍历完成，同时回归节点 1。由于节点 1 的左右子树全部遍历完成，因此整个二叉树遍历完成；
	 * @param consumer
	 */
	public void preOrderTraverse(Consumer<T> consumer) {
		consumer.accept(this.value);
		if (left != null) {
			left.preOrderTraverse(consumer);
		}
		if (right != null) {
			right.preOrderTraverse(consumer);
		}
	}
	
	/**
访问该二叉树的根节点，找到 1；
遍历节点 1 的左子树，找到节点 2；
遍历节点 2 的左子树，找到节点 4；
由于节点 4 无左孩子，因此找到节点 4，并遍历节点 4 的右子树；
由于节点 4 无右子树，因此节点 2 的左子树遍历完成，访问节点 2；
遍历节点 2 的右子树，找到节点 5；
由于节点 5 无左子树，因此访问节点 5 ，又因为节点 5 没有右子树，因此节点 1 的左子树遍历完成，访问节点 1 ，并遍历节点 1 的右子树，找到节点 3；
遍历节点 3 的左子树，找到节点 6；
由于节点 6 无左子树，因此访问节点 6，又因为该节点无右子树，因此节点 3 的左子树遍历完成，开始访问节点 3 ，并遍历节点 3 的右子树，找到节点 7；
由于节点 7 无左子树，因此访问节点 7，又因为该节点无右子树，因此节点 1 的右子树遍历完成，即整棵树遍历完成；
	 * @param consumer
	 */
	public void inOrderTraverse(Consumer<T> consumer) {
		if (left != null) {
			left.inOrderTraverse(consumer);
		}
		consumer.accept(this.value);
		if (right != null) {
			right.inOrderTraverse(consumer);
		}
	}
	
	/**
从根节点 1 开始，遍历该节点的左子树（以节点 2 为根节点）；
遍历节点 2 的左子树（以节点 4 为根节点）；
由于节点 4 既没有左子树，也没有右子树，此时访问该节点中的元素 4，并回退到节点 2 ，遍历节点 2 的右子树（以 5 为根节点）；
由于节点 5 无左右子树，因此可以访问节点 5 ，并且此时节点 2 的左右子树也遍历完成，因此也可以访问节点 2；
此时回退到节点 1 ，开始遍历节点 1 的右子树（以节点 3 为根节点）；
遍历节点 3 的左子树（以节点 6 为根节点）；
由于节点 6 无左右子树，因此访问节点 6，并回退到节点 3，开始遍历节点 3 的右子树（以节点 7 为根节点）；
由于节点 7 无左右子树，因此访问节点 7，并且节点 3 的左右子树也遍历完成，可以访问节点 3；节点 1 的左右子树也遍历完成，可以访问节点 1；
到此，整棵树的遍历结束。
	 * @param consumer
	 */
	public void postOrderTraverse(Consumer<T> consumer) {
		if (left != null) {
			left.postOrderTraverse(consumer);
		}
		if (right != null) {
			right.postOrderTraverse(consumer);
		}
		consumer.accept(this.value);
	}
	
	public T getValue() {
		return value;
	}

	public BinaryTree<T> getLeft() {
		return left;
	}

	public BinaryTree<T> getRight() {
		return right;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return "BinaryTree [value=" + value + ", left=" + left + ", right=" + right + ", level=" + level + "]";
	}
	
}

package io.github.icodegarden.commons.lang.datastructure.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;


/**
 * 
 * @author Fangfang.Xu
 *
 */
class BinaryTreeTests {

	Random random = new Random();

	LinkedList<Integer> list = new LinkedList<Integer>();
	{
		int j = random.nextInt(10000) + 10000;
		for (int i = 0; i < j; i++) {
			if (i % 1000 == 0) {
				System.out.println(i);
			}
			list.add(i);
		}
	}
	
	@Test
	void buildBySortedList_simple() {
		List<Integer> list = Arrays.asList(1, 2, 3, null, 5, 6, 7);

		BinaryTree tree = BinaryTree.buildBySortedList(list);
//		System.out.println(JsonUtils.serialize(tree));
		assertThat(tree.getLeft().getLeft()).isNull();//4是null值，所以没有该节点
	}
	
	@Test
	void totalNodes() {
		BinaryTree tree = BinaryTree.buildBySortedList(list);
		assertThat(tree.totalNodes()).isEqualTo(list.size());
	}
	
	@Test
	void totalLeafNodes() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

		BinaryTree tree = BinaryTree.buildBySortedList(list);
		assertThat(tree.totalLeafNodes()).isEqualTo(3);
		//---------------------------------------------
		list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

		tree = BinaryTree.buildBySortedList(list);
		assertThat(tree.totalLeafNodes()).isEqualTo(4);
	}
	
	@Test
	void totalFullDegreeNodes() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

		BinaryTree tree = BinaryTree.buildBySortedList(list);
		assertThat(tree.totalFullDegreeNodes()).isEqualTo(2);
		//---------------------------------------------
		list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

		tree = BinaryTree.buildBySortedList(list);
		assertThat(tree.totalFullDegreeNodes()).isEqualTo(3);
	}
	
	/**
 * 二叉树的特性：
 * 第i层最多有2(i-1次)个节点，即树高3则第3层最多4个节点.
 * 如果深度为K，那么此树最多有2(K次)-1个节点，即树高3最多7个节点.
 * 叶子节点数(n0)=度为2的节点数(n2)+1，如图1叶子是3、4、5，度为2的节点是1、2，图2叶子是D、E、F、G，度为2个节点是A、B、C
	 */
	@Test
	void features() {
		BinaryTree tree = BinaryTree.buildBySortedList(list);
		int treeHeight = tree.treeHeight();
		// -------------------------------------
		for (int level = 0; level < treeHeight; level++) {
			int totalNodes = tree.totalNodes(level);
			int maxNodesOfLevel = (int) Math.pow(2, level - 1);
			System.out.println(
					String.format("assert 第i层最多有2(i-1次)个节点 level=%d %d <= %d", level, totalNodes, maxNodesOfLevel));
			assertThat(tree.totalNodes(level)).isLessThanOrEqualTo(maxNodesOfLevel);
		}
		// -------------------------------------
		int totalNodes = tree.totalNodes();
		int lteTotalNodes = (int) Math.pow(2, treeHeight);
		System.out.println(String.format("assert 如果深度为K，那么此树最多有2(K次)-1个节点  %d <= %d", totalNodes, lteTotalNodes));
		assertThat(totalNodes).isLessThanOrEqualTo(lteTotalNodes);
		// -------------------------------------
		int totalLeafNodes = tree.totalLeafNodes();
		int totalDegree2Nodes = tree.totalFullDegreeNodes();
		System.out.println(
				String.format("assert 叶子节点数(n0)=度为2的节点数(n2)+1  %d = %d", totalLeafNodes, totalDegree2Nodes + 1));
		assertThat(totalLeafNodes).isEqualTo(totalDegree2Nodes + 1);
	}
	
	@Test
	void preOrderTraverse() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
		BinaryTree<Integer> tree = BinaryTree.buildBySortedList(list);

		LinkedList<Integer> linkedList = new LinkedList<Integer>();
		Consumer<Integer> c = v -> linkedList.add(v);
		tree.preOrderTraverse(c);
		
		assertThat(linkedList).isEqualTo(Arrays.asList(1, 2, 4, 5, 3, 6, 7));
	}
	
	@Test
	void inOrderTraverse() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
		BinaryTree<Integer> tree = BinaryTree.buildBySortedList(list);

		LinkedList<Integer> linkedList = new LinkedList<Integer>();
		Consumer<Integer> c = v -> linkedList.add(v);
		tree.inOrderTraverse(c);
		
		assertThat(linkedList).isEqualTo(Arrays.asList(4, 2, 5, 1, 6, 3, 7));
	}
	
	@Test
	void postOrderTraverse() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
		BinaryTree<Integer> tree = BinaryTree.buildBySortedList(list);

		LinkedList<Integer> linkedList = new LinkedList<Integer>();
		Consumer<Integer> c = v -> linkedList.add(v);
		tree.postOrderTraverse(c);
		
		assertThat(linkedList).isEqualTo(Arrays.asList(4, 5, 2, 6, 7, 3, 1));
	}
}

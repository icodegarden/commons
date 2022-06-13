package io.github.icodegarden.commons.lang.datastructure.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.util.JsonUtils;


/**
 * 
 * @author Fangfang.Xu
 *
 */
class BinarySearchTreeTests {

	Random random = new Random();

	LinkedList<Node> list = new LinkedList<Node>();
	{
		for (int i = 0; i < 3000; i++) {
			int j = random.nextInt(100000000);
			Node node = new Node(j, j);
			list.add(node);
		}
	}
	
	@Test
	void build_simple() {
		List<Node> list = Arrays.asList(new Node(3, new Object()),new Node(5, new Object()),
				new Node(7, 77),new Node(2, new Object()),new Node(1, new Object()));

		BinarySearchTree tree = BinarySearchTree.build(list);
		System.out.println(JsonUtils.serialize(tree));
	}
	/**
	 * 查找节点
	 */
	@Test
	void search() {
		List<Node> list = Arrays.asList(new Node(3, new Object()),new Node(5, new Object()),
				new Node(7, 77),new Node(2, new Object()),new Node(1, new Object()));

		BinarySearchTree tree = BinarySearchTree.build(list);
		
		assertThat(tree.search(7)).isNotNull();
		assertThat(tree.search(7).getValue()).isEqualTo(77);
		assertThat(tree.search(4)).isNull();
	}
	
	/**
	 * 新增后用排序验证
	 */
	@Test
	void insert() {
		List<Node> list = Arrays.asList(new Node(3, new Object()),new Node(5, new Object()),
				new Node(7, 77),new Node(2, new Object()));
		BinarySearchTree tree = BinarySearchTree.build(list);
		
		tree.insert(new Node(1, new Object()));
		
		LinkedList<Integer> linkedList = new LinkedList<Integer>();
		Consumer<Node> c = n -> linkedList.add(n.getHash());
		tree.inOrderTraverse(c);
		
		assertThat(linkedList).isEqualTo(Arrays.asList(1,2,3,5,7));
	}
	
	/**
	 * 利用中序遍历验证大规模节点时的顺序性
	 */
	@Test
	void inOrderTraverse() {
		BinarySearchTree tree = BinarySearchTree.build(list);

		LinkedList<Integer> linkedList = new LinkedList<Integer>();
		Consumer<Node> c = n -> linkedList.add(n.getHash());
		tree.inOrderTraverse(c);
		
		List<Integer> ordered = list.stream().map(n->n.getHash()).sorted().collect(Collectors.toList());
		assertThat(linkedList).isEqualTo(ordered);
	}
	
	@Test
	void remove() {
		BinarySearchTree tree = remove(1);
		
		LinkedList<Integer> linkedList = new LinkedList<Integer>();
		Consumer<Node> c = n -> linkedList.add(n.getHash());
		tree.inOrderTraverse(c);
		
		assertThat(linkedList).isEqualTo(Arrays.asList(2,3,5,7));
		//-------------------------------------------------------------------
		tree = remove(7);
		
		LinkedList<Integer> linkedList2 = new LinkedList<Integer>();
		c = n -> linkedList2.add(n.getHash());
		tree.inOrderTraverse(c);
		
		assertThat(linkedList2).isEqualTo(Arrays.asList(1,2,3,5));
		//-------------------------------------------------------------------
		tree = remove(2);
		
		LinkedList<Integer> linkedList3 = new LinkedList<Integer>();
		c = n -> linkedList3.add(n.getHash());
		tree.inOrderTraverse(c);
		
		assertThat(linkedList3).isEqualTo(Arrays.asList(1,3,5,7));
		//-------------------------------------------------------------------
		tree = remove(5);
		
		LinkedList<Integer> linkedList4 = new LinkedList<Integer>();
		c = n -> linkedList4.add(n.getHash());
		tree.inOrderTraverse(c);
		
		assertThat(linkedList4).isEqualTo(Arrays.asList(1,2,3,7));
	}
	
	private BinarySearchTree remove(int hash) {
		List<Node> list = Arrays.asList(new Node(3, new Object()),new Node(5, new Object()),
				new Node(7, 77),new Node(2, new Object()),new Node(1, new Object()));
		BinarySearchTree tree = BinarySearchTree.build(list);
		
		BinarySearchTree node = tree.remove(hash);
		
		assertThat(node).isNotNull();
		
		return tree;
	}
}

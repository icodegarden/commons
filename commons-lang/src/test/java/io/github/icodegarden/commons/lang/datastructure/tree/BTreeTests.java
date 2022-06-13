package io.github.icodegarden.commons.lang.datastructure.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.util.JsonUtils;


/**
 * 
 * @author Fangfang.Xu
 *
 */
class BTreeTests {
	
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
	void build_insert() {
		//用例 https://blog.csdn.net/baidu_37964071/article/details/79533027
		
		final int m = 3;
		
		List<Node> list = Arrays.asList(new Node(53, new Object()),new Node(75, new Object()),new Node(139, new Object()));
		
		BTree tree = BTree.build(list, m);
		System.out.println(JsonUtils.serialize(tree.root()));
		assertThat(JsonUtils.serialize(tree.root())).isEqualTo("{\"values\":[{\"hash\":75,\"value\":{}}],\"children\":[{\"values\":[{\"hash\":53,\"value\":{}}]},{\"values\":[{\"hash\":139,\"value\":{}}]}]}");
		
		tree.insert(new Node(49, new Object()));
		tree.insert(new Node(145, new Object()));
		System.out.println(JsonUtils.serialize(tree.root()));
		assertThat(JsonUtils.serialize(tree.root())).isEqualTo("{\"values\":[{\"hash\":75,\"value\":{}}],\"children\":[{\"values\":[{\"hash\":49,\"value\":{}},{\"hash\":53,\"value\":{}}]},{\"values\":[{\"hash\":139,\"value\":{}},{\"hash\":145,\"value\":{}}]}]}");
		
		tree.insert(new Node(36, new Object()));
		System.out.println(JsonUtils.serialize(tree.root()));
		
		assertThat(JsonUtils.serialize(tree.root())).isEqualTo("{\"values\":[{\"hash\":49,\"value\":{}},{\"hash\":75,\"value\":{}}],\"children\":[{\"values\":[{\"hash\":36,\"value\":{}}]},{\"values\":[{\"hash\":53,\"value\":{}}]},{\"values\":[{\"hash\":139,\"value\":{}},{\"hash\":145,\"value\":{}}]}]}");
		
		Node node = tree.search(36);
		assertThat(node).isNotNull();
		assertThat(node.getHash()).isEqualTo(36);
		node = tree.search(145);
		assertThat(node).isNotNull();
		assertThat(node.getHash()).isEqualTo(145);

		node = tree.search(200);
		assertThat(node).isNull();
	}
	
	@Test
	void build_more() {
		final int m = 3;
		
		BTree tree = BTree.build(list, m);
		System.out.println(tree.root());
	}
}

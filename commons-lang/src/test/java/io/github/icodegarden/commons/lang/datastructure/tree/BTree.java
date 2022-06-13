package io.github.icodegarden.commons.lang.datastructure.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * B树（平衡多路查找树）的定义：<br>
 * 它=平衡二叉树+多个度
 * 
 * B树存在的原因：
 * 目的是在大量数据时无法把整个树的数据加载到内存，需要存放在磁盘，查询时树高越大IO次数越多，
 * 为了减少IO次数，节点的度就要>2，以系统页为单位加载到磁盘
 * 
 * https://www.jianshu.com/p/a858bb15cbf0
 * 目前仅用于模拟，不做使用，删除节点不做实现了
 * @author Fangfang.Xu
 *
 */
class BTree {

	private static final Logger log = LoggerFactory.getLogger(BTree.class);

	private int m;// m要求奇数且是2N次+1，5阶B树中，结点最多有4个key,最少有2个key

	@JsonIgnore
	protected BTree parent;
	protected List<Node> values;
	protected List<BTree> children;

	private static final AtomicLong incr = new AtomicLong();
	private long seq = incr.getAndIncrement();

	/**
	 * 用于在children中indexOf
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BTree other = (BTree) obj;
		return seq == other.seq;
	}

	protected BTree(BTree parent, List<Node> values, int m) {
		this.parent = parent;
		this.values = values;
		this.m = m;
	}

	/**
	 * 一个节点最多的key数量
	 * 
	 * @return
	 */
	private int maxKeys() {
		return m - 1;
	}

	/**
	 * @param list
	 * @return 无节点则null
	 */
	public static BTree build(List<Node> list, int m) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		if (list.size() == 1) {
			return new BTree(null, list, m);
		}

		Iterator<Node> iterator = list.iterator();
		BTree root = newRoot(iterator.next(), null, m);

		while (iterator.hasNext()) {
			root.insert(iterator.next());
		}

		return root;
	}

	private static BTree newRoot(Node node, BTree child, int m) {
		List<Node> values = new ArrayList<Node>();
		values.add(node);
		BTree root = new BTree(null, values, m);

		if (child != null) {
			List<BTree> children = new ArrayList<BTree>();
			children.add(child);
			root.children = children;
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
		Node node = root().searchInternal(hash, 1);
		return node != null ? node : null;
	}

	private Node searchInternal(int hash, int level) {
		if(log.isDebugEnabled()) {
			log.debug("search on level:{}", level);
		}
		for (Node node : values) {
			if (node.getHash() == hash) {
				return node;
			}
		}

		if (!CollectionUtils.isEmpty(children)) {
			Optional<BTree> first = children.stream().filter(child -> {
				/**
				 * 要求处于区间
				 */
				if (hash >= child.min().getHash() && hash <= child.max().getHash()) {
					return true;
				}
				return false;
			}).findFirst();
			if (first.isPresent()) {
				return first.get().searchInternal(hash, ++level);
			}
		}

		return null;
	}

	public BTree root() {
		BTree root = this;
		while (root.parent != null) {
			root = this.parent;
		}
		return root;
	}

	/**
	 * 新增节点，必须是root节点进行操作
	 * 
	 * @param root
	 * @param node
	 */
	public void insert(Node node) {
		BTree root = root();
		append(root, node, m);
		if (log.isDebugEnabled()) {
			log.debug("insert success hash:{}", node.getHash());
		}
	}

	/**
	 * 1、 若该结点中关键码个数小于m-1，则直接插入即可。 2、
	 * 若该结点中关键码个数等于m-1，则将引起结点的分裂。以中间关键码为界将结点一分为二，产生一个新结点，并把中间关键码插入到父结点(ｋ-1层)中
	 * 
	 * @param node 正在匹配的节点
	 * @param next 正在增加的节点
	 */
	private static void append(BTree node, Node next, int m) {
		/**
		 * 新增在叶子中进行
		 */
		if (CollectionUtils.isEmpty(node.children)) {
			doAppend(node, next, m);
		} else {
			/**
			 * 选中一个正确区间的节点
			 */
			if (next.getHash() <= node.min().getHash()) {// 应该从最左的子节点中去
				BTree minNode = node.children.get(0);
				append(minNode, next, m);
			} else if (next.getHash() > node.max().getHash()) {// 应该从最右的子节点中去
				BTree maxNode = node.children.get(node.children.size() - 1);
				append(maxNode, next, m);
			} else {
				for (BTree child : node.children) {
					if (next.getHash() >= child.min().getHash() && next.getHash() < child.max().getHash()) {
						append(child, next, m);
					}
				}
			}
		}
	}

	private static void doAppend(BTree node, Node next, int m) {
		if (log.isDebugEnabled()) {
			log.debug("append hash:{} on node:{}-{}", next.getHash(), node.min().getHash(), node.max().getHash());
		}
		/**
		 * 寻找插入的位置
		 */
		if (next.getHash() < node.min().getHash()) {
			node.values.add(0, next);
		} else if (next.getHash() > node.max().getHash()) {
			node.values.add(node.values.size(), next);
		} else {
			for (int i = 0; i < node.values.size(); i++) {
				Node value = node.values.get(i);
				if (next.getHash() == value.getHash()) {
					value.setValue(next.getValue());// 相同则替换
				} else if (next.getHash() > value.getHash() && next.getHash() < value.getHash()) {
					node.values.add(i + 1, next);
				}
			}
		}

		/**
		 * 插入完后如果超过数量就要分裂
		 */
		if (node.values.size() > node.maxKeys()) {
			Node remove = node.values.remove(node.maxKeys() / 2);// 移除中间位
			if (node.parent == null) {// 已经是root，需要新的root
				BTree root = newRoot(remove, node, m);
				node.parent = root;
			} else {
				doAppend(node.parent, remove, m);// 上移到parent
			}

			// 中间位移除后，小于中间位部分要成为新的节点才能达到区间平衡
			ArrayList<Node> leftNodes = new ArrayList<Node>();
			node.values.removeIf(value -> {
				boolean b = value.getHash() < remove.getHash();
				if (b) {
					leftNodes.add(value);
				}
				return b;
			});
			BTree bTree = new BTree(node.parent, leftNodes, m);

			int indexOf = node.parent.children.indexOf(node);
			node.parent.children.add(indexOf, bTree);
		}
	}

	private Node min() {
		return values.get(0);
	}

	private Node max() {
		return values.get(values.size() - 1);
	}

	public List<Node> getValues() {
		return values;
	}

	public List<BTree> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "BTree [values=" + values + ", children=" + children + "]";
	}

}

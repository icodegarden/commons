package io.github.icodegarden.commons.lang.filter;

import java.util.BitSet;
import java.util.Collection;

import io.github.icodegarden.commons.lang.algorithm.HashFunction;
import io.github.icodegarden.commons.lang.algorithm.JavaStringFunction;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class BloomFilter implements TrustFilter<String> {

	/**
	 * 2的24次=16777216，16777216/8bit=2097152byte=2m<br>
	 * 位图以long[]形式存在，对应的bit位将通过结合size的计算确定属于long[]中的哪个具体index位置
	 */
	private static final int DEFAULT_SIZE = 1 << 24;

	private final int bitSize;

	private final BitSet bits;

	private final HashFunction[] hashers;

	/**
	 * 使用默认bitSize,默认countOfHasher=3
	 */
	public BloomFilter() {
		this(DEFAULT_SIZE, 3);
	}

	/**
	 * 使用默认bitSize,使用java string的hash算法
	 * 
	 * @param countOfHasher
	 * @param shouldFilter
	 */
	public BloomFilter(int countOfHasher) {
		this(DEFAULT_SIZE, countOfHasher);
	}

	/**
	 * 使用java string的hash算法
	 * 
	 * @param bitSize
	 * @param countOfHasher
	 * @param shouldFilter
	 */
	public BloomFilter(int bitSize, int countOfHasher) {
		this(bitSize, new JavaStringFunction[countOfHasher]);

		int seed = 31 << (countOfHasher / 2);
		for (int i = 0; i < countOfHasher; i++) {
			hashers[i] = new JavaStringFunction(seed);
			seed = seed >> 1;
		}
	}

	/**
	 * 使用默认bitSize
	 * 
	 * @param hashers
	 * @param shouldFilter
	 */
	public BloomFilter(HashFunction[] hashers) {
		this(DEFAULT_SIZE, hashers);
	}

	public BloomFilter(int bitSize, HashFunction[] hashers) {
		this.bitSize = bitSize;
		bits = new BitSet(bitSize);
		this.hashers = hashers;
	}

	public void add(String value) {
		for (HashFunction f : hashers) {
			/**
			 * hash值 & (bitSize - 1)
			 * 的结果将确定位图中的哪个具体位置，把该位置设置为1，如果HashFunction有3个则会把3个bit为设置为1
			 */
			bits.set(f.hash(value) & (bitSize - 1));
		}
	}

	public void add(Collection<String> values) {
		values.forEach(v -> {
			add(v);
		});
	}

	@Override
	public boolean filter(String str) {
		return contains(str);
	}

	boolean contains(String str) {
		if (str == null) {
			return false;
		}
		boolean ret = true;
		for (HashFunction f : hashers) {
			if (ret) {
				ret = ret & bits.get(f.hash(str) & (bitSize - 1));
			}
		}
		return ret;
	}

}
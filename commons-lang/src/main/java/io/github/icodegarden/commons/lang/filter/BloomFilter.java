package io.github.icodegarden.commons.lang.filter;

import java.util.BitSet;

import io.github.icodegarden.commons.lang.algorithm.HashFunction;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class BloomFilter extends AbstractBloomFilter {

	/**
	 * 2的24次=16777216，16777216/8bit=2097152byte=2m<br>
	 * 位图以long[]形式存在，对应的bit位将通过结合size的计算确定属于long[]中的哪个具体index位置
	 */
	private static final int DEFAULT_SIZE = 1 << 24;

	private final int bitSize;

	private final BitSet bits;

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
		super(countOfHasher);
		this.bitSize = bitSize;
		this.bits = new BitSet(bitSize);
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
		super(hashers);
		this.bitSize = bitSize;
		this.bits = new BitSet(bitSize);
	}

	@Override
	protected void setbit(int hash) {
		/**
		 * hash值 & (bitSize - 1)
		 * 的结果将确定位图中的哪个具体位置，把该位置设置为1，如果HashFunction有3个则会把3个bit为设置为1
		 */
		bits.set(hash & (bitSize - 1));
	}

	@Override
	protected boolean getbit(int hash) {
		return bits.get(hash & (bitSize - 1));
	}

}
package io.github.icodegarden.commons.lang.filter;

import java.util.Collection;

import io.github.icodegarden.commons.lang.algorithm.HashFunction;
import io.github.icodegarden.commons.lang.algorithm.JavaStringFunction;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractBloomFilter implements TrustFilter<String> {

	private final HashFunction[] hashers;

	/**
	 * 使用默认bitSize,默认countOfHasher=3
	 */
	public AbstractBloomFilter() {
		this(3);
	}

	/**
	 * 使用java string的hash算法
	 * 
	 * @param bitSize
	 * @param countOfHasher
	 * @param shouldFilter
	 */
	public AbstractBloomFilter(int countOfHasher) {
		this(new JavaStringFunction[countOfHasher]);

		int seed = 31 << (countOfHasher / 2);
		for (int i = 0; i < countOfHasher; i++) {
			hashers[i] = new JavaStringFunction(seed);
			seed = seed >> 1;
		}
	}

	public AbstractBloomFilter(HashFunction[] hashers) {
		this.hashers = hashers;
	}

	protected abstract void setbit(int hash);

	protected abstract boolean getbit(int hash);

	public void add(String value) {
		for (HashFunction f : hashers) {
			setbit(f.hash(value));
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
				ret = ret & getbit(f.hash(str));
			}
		}
		return ret;
	}

}
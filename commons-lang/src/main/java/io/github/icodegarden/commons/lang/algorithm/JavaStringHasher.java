package io.github.icodegarden.commons.lang.algorithm;

/**
 * java 的string hash算法+seed
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaStringHasher implements Hasher {
	/**
	 * String类的seed默认是31
	 */
	private final int seed;

	/**
	 * 使用String默认的seed=31
	 */
	public JavaStringHasher() {
		this.seed = 31;
	}

	public JavaStringHasher(int seed) {
		this.seed = seed;
	}

	@Override
	public long hash(String value) {
		int hash = 0;

		int h = hash;
		if (h == 0 && value.length() > 0) {
			for (int i = 0; i < value.length(); i++) {
				h = seed * h + value.charAt(i);
			}
			hash = h;
		}
		return h;
	}

}
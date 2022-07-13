package io.github.icodegarden.commons.lang.algorithm;

/**
 * java 的string hash算法+seed
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaStringFunction implements HashFunction {
	/**
	 * String类的seed默认是31
	 */
	private final int seed;

	/**
	 * 使用String默认的seed=31
	 */
	public JavaStringFunction() {
		this.seed = 31;
	}

	public JavaStringFunction(int seed) {
		this.seed = seed;
	}

	@Override
	public int hash(String value) {
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
package io.github.icodegarden.commons.test.filter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.filter.AbstractBloomFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractBloomFilterTests {

	private String key = "key";
	private String key2 = "key2";

	protected abstract AbstractBloomFilter newBloomFilter(int count);

	@Test
	public void filter() throws Exception {
		AbstractBloomFilter filter = newBloomFilter(3);
		filter.add(key);

		boolean b = filter.filter(key);
		Assertions.assertThat(b).isTrue();

		b = filter.filter(key2);
		Assertions.assertThat(b).isFalse();

		filter.add(key2);
		b = filter.filter(key2);
		Assertions.assertThat(b).isTrue();
	}
}

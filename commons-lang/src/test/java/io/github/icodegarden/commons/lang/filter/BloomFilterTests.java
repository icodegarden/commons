package io.github.icodegarden.commons.lang.filter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class BloomFilterTests {

	protected String key = "key";
	protected String key2 = "key2";

	@Test
	public void filter() throws Exception {
		BloomFilter filter = new BloomFilter(3);
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

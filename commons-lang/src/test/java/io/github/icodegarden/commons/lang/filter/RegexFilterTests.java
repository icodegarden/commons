package io.github.icodegarden.commons.lang.filter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class RegexFilterTests {

	protected String key = "key";
	protected String key2 = "key2";

	@Test
	public void filter() throws Exception {
		String regex = "\\d";
		RegexFilter filter = new RegexFilter(regex);

		boolean b = filter.filter("1");
		Assertions.assertThat(b).isTrue();

		b = filter.filter(key2);
		Assertions.assertThat(b).isFalse();
	}
}

package io.github.icodegarden.commons.lang.filter;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class TrustFilterChainTests {

	@Test
	public void filter() throws Exception {
		WhiteListFilter<String> filter1 = new WhiteListFilter<>(Arrays.asList("a", "b"));
		BlackListFilter<String> filter2 = new BlackListFilter<String>(Arrays.asList("b"));

		TrustFilterChain<String> chain = new TrustFilterChain.Default<>(Arrays.asList(filter1, filter2));

		boolean b = chain.filter("a");
		Assertions.assertThat(b).isTrue();

		b = chain.filter("b");
		Assertions.assertThat(b).isFalse();

		TrustFilter<String> rejectBy = chain.rejectBy();
		Assertions.assertThat(rejectBy).isEqualTo(filter2);
	}
}

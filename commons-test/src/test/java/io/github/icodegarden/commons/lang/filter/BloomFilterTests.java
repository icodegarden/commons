package io.github.icodegarden.commons.lang.filter;

import io.github.icodegarden.commons.test.filter.AbstractBloomFilterTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class BloomFilterTests extends AbstractBloomFilterTests {

	@Override
	protected AbstractBloomFilter newBloomFilter(int count) {
		return new BloomFilter(count);
	}

}

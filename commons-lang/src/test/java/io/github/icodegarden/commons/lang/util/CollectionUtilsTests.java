package io.github.icodegarden.commons.lang.util;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class CollectionUtilsTests {

	@Test
	void subSafely() throws Exception {
		ArrayList<Integer> list = new ArrayList<Integer>() {
			{
				add(1);
				add(2);
				add(3);
				add(4);
				add(5);
			}
		};

		List<Integer> result = CollectionUtils.subSafely(list, 0, 10);
		Assertions.assertThat(result).isEqualTo(list);

		result = CollectionUtils.subSafely(list, 4, 10);
		Assertions.assertThat(result.size()).isEqualTo(1);
		Assertions.assertThat(result.get(0)).isEqualTo(5);

		result = CollectionUtils.subSafely(list, 5, 10);
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.size()).isEqualTo(0);
	}

}

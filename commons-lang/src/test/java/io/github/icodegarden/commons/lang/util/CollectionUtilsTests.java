package io.github.icodegarden.commons.lang.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.tuple.Tuple2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class CollectionUtilsTests {
	
	@Test
	void arrayStartWith() throws Exception {
		String s1 = UUID.randomUUID().toString();
		String s2 = s1+"_r";
		String s3 = s1+"_w";
		
		//[49, 100, 56, 55, 52, 53, 50, 53, 45, 102, 100, 53, 98, 45, 52, 101, 54, 48, 45, 97, 57, 99, 99, 45, 101, 48, 102, 98, 55, 102, 56, 99, 54, 55, 50, 102]
		byte[] bytes1 = s1.getBytes();
		//[49, 100, 56, 55, 52, 53, 50, 53, 45, 102, 100, 53, 98, 45, 52, 101, 54, 48, 45, 97, 57, 99, 99, 45, 101, 48, 102, 98, 55, 102, 56, 99, 54, 55, 50, 102, 95, 114]
		byte[] bytes2 = s2.getBytes();
		//[49, 100, 56, 55, 52, 53, 50, 53, 45, 102, 100, 53, 98, 45, 52, 101, 54, 48, 45, 97, 57, 99, 99, 45, 101, 48, 102, 98, 55, 102, 56, 99, 54, 55, 50, 102, 95, 119]
		byte[] bytes3 = s3.getBytes();
		
		boolean b = CollectionUtils.arrayStartWith(bytes2, bytes1);
		Assertions.assertThat(b).isTrue();
		b = CollectionUtils.arrayStartWith(bytes3, bytes1);
		Assertions.assertThat(b).isTrue();
	}
	
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

	@Test
	void mergeByKeyGroup() throws Exception {
		List<String> keys = Arrays.asList("k1", "k2");
		List<String> values = Arrays.asList("v1", "v2", "v3", "v4", "v5", "v6");

		List<String> list = CollectionUtils.mergeByKeyGroup(keys, values);

		Assertions.assertThat(list).isEqualTo(Arrays.asList("k1", "v1", "v2", "v3", "k2", "v4", "v5", "v6"));

		// -------------------------------------------------------

		list = CollectionUtils.mergeByKeyGroup(keys.toArray(new String[keys.size()]),
				values.toArray(new String[values.size()]));

		Assertions.assertThat(list).isEqualTo(Arrays.asList("k1", "v1", "v2", "v3", "k2", "v4", "v5", "v6"));
	}

	@Test
	void splitByKeyGroup() throws Exception {
		List<String> params = Arrays.asList("k1", "v1", "v2", "v3", "k2", "v4", "v5", "v6");

		Tuple2<List<String>, List<String>> tuple2 = CollectionUtils.splitByKeyGroup(params, 2);
		List<String> keys = tuple2.getT1();
		List<String> values = tuple2.getT2();

		Assertions.assertThat(keys).isEqualTo(Arrays.asList("k1", "k2"));
		Assertions.assertThat(values).isEqualTo(Arrays.asList("v1", "v2", "v3", "v4", "v5", "v6"));

		// -------------------------------------------------------

		tuple2 = CollectionUtils.splitByKeyGroup(params.toArray(new String[params.size()]), 2);
		keys = tuple2.getT1();
		values = tuple2.getT2();

		Assertions.assertThat(keys).isEqualTo(Arrays.asList("k1", "k2"));
		Assertions.assertThat(values).isEqualTo(Arrays.asList("v1", "v2", "v3", "v4", "v5", "v6"));
	}
}

package io.github.icodegarden.commons.lang.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CollectionUtils {

	/**
	 * 
	 * @param arr 长的
	 * @param startWith 短的
	 * @return
	 */
	public static boolean arrayStartWith(byte[] arr, byte[] startWith) {
		if (arr.length < startWith.length) {
			return false;
		}
		for (int i = 0; i < startWith.length; i++) {
			if (arr[i] != startWith[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 这是个环形截取工具,不会越界<br>
	 * 从fromIndex开始往后再取maxNum个
	 * 
	 * @param <T>
	 * @param elements
	 * @param fromIndex 包含该位置
	 * @param maxNum    最多截取个数，如果元素个数是10，maxNum是20，最多只返回10个
	 * @return
	 */
	public static <T> List<T> nextElements(List<T> elements, int fromIndex, int maxNum) {
		fromIndex = fromIndex < 0 ? 0 : fromIndex;
		fromIndex = Math.min(fromIndex, elements.size());
		maxNum = Math.min(maxNum, elements.size());

		/**
		 * 直接满足索引位置
		 */
		if (fromIndex + maxNum <= elements.size()) {
			return elements.subList(fromIndex, fromIndex + maxNum);
		}
		/**
		 * 截到末尾，恰好满足
		 */
		List<T> subList1 = elements.subList(fromIndex, elements.size());
		if (subList1.size() == maxNum) {
			return subList1;
		}
		/**
		 * 截到末尾，但不够，再从头开始截一段
		 */
		List<T> subList2 = elements.subList(0, maxNum - (elements.size() - fromIndex));

		ArrayList<T> arrayList = new ArrayList<T>(maxNum);
		arrayList.addAll(subList1);
		arrayList.addAll(subList2);
		return arrayList;
	}

	/**
	 * 安全截取，不会越界，最多截到最后一位
	 * 
	 * @param <T>
	 * @param elements
	 * @param fromIndex
	 * @param maxNum
	 * @return
	 */
	public static <T> List<T> subSafely(List<T> elements, int fromIndex, int maxNum) {
		fromIndex = fromIndex < 0 ? 0 : fromIndex;
		fromIndex = Math.min(fromIndex, elements.size());
		maxNum = Math.min(maxNum, elements.size());

		/**
		 * 直接满足索引位置
		 */
		if (fromIndex + maxNum <= elements.size()) {
			return elements.subList(fromIndex, fromIndex + maxNum);
		}
		/**
		 * 截到末尾
		 */
		return elements.subList(fromIndex, elements.size());
	}

	/**
	 * 每个String按UTF8转byte[]，并把集合转数组，不会去重
	 * 
	 * @param values
	 * @return
	 */
	public static byte[][] toBytesArray(Collection<String> values) {
		List<byte[]> vBytes = values.stream().map(v -> v.getBytes(StandardCharsets.UTF_8)).collect(Collectors.toList());
		byte[][] vBytesArray = vBytes.toArray(new byte[vBytes.size()][]);
		return vBytesArray;
	}

	/**
	 * 分组合并
	 * 
	 * @param <T>
	 * @param keys   k1 k2 ...
	 * @param values v1 v2 v3 v4 ...
	 * @return k1 v1 v2 k2 v3 v4 ...
	 */
	public static <T> List<T> mergeByKeyGroup(Collection<T> keys, Collection<T> values) {
		Assert.notEmpty(keys, "keys must not empty");
		Assert.notEmpty(values, "values must not empty");
		Assert.isTrue(values.size() % keys.size() == 0, "values size invalid");

		List<T> result = new ArrayList<T>(keys.size() + values.size());

		int groupSize = values.size() / keys.size();

		Iterator<T> valuesIt = values.iterator();
		for (T key : keys) {
			/**
			 * 每组的key
			 */
			result.add(key);
			/**
			 * 每组的value
			 */
			for (int i = 0; i < groupSize; i++) {
				result.add(valuesIt.next());
			}
		}

		return result;
	}

	public static <T> List<T> mergeByKeyGroup(T[] keys, T[] values) {
		Assert.notEmpty(keys, "keys must not empty");
		Assert.notEmpty(values, "values must not empty");
		return mergeByKeyGroup(Arrays.asList(keys), Arrays.asList(values));

//		Assert.isTrue(values.length % keys.length == 0, "values size invalid");
//
//		List<T> result = new ArrayList<T>(keys.length + values.length);
//
//		int groupSize = values.length / keys.length;
//		for (int g = 0; g < keys.length; g++) {
//			/**
//			 * 每组的key
//			 */
//			result.add(keys[g]);
//			/**
//			 * 每组的value
//			 */
//			for (int i = 0; i < groupSize; i++) {
//				result.add(values[groupSize * g + i]);
//			}
//		}
//		return result;
	}

	/**
	 * 分组拆分
	 * 
	 * @param <T>
	 * @param params   k1 v1 v2 k2 v3 v4 ...
	 * @param keyCount 2
	 * @return k1 k2 ... , v1 v2 v3 v4 ...
	 */
	public static <T> Tuple2<List<T>/* keys */, List<T>/* values */> splitByKeyGroup(Collection<T> params,
			int keyCount) {
		Assert.notEmpty(params, "params must not empty");
		Assert.isTrue(keyCount > 0, "keyCount must gt 0");
		Assert.isTrue(params.size() % keyCount == 0, "params size invalid");

		/**
		 * 每组有几个参数
		 */
		int groupSize = params.size() / keyCount;// keyCount即共有几组

		Iterator<T> paramsIt = params.iterator();

		List<T> keys = new ArrayList<T>(keyCount);
		List<T> values = new ArrayList<T>(keyCount * (groupSize - 1));
		for (int g = 0; g < keyCount; g++) {
			/**
			 * 取每组
			 */
			for (int i = 0; i < groupSize; i++) {
//				byte[] param = params[groupSize * g + i];
				T param = paramsIt.next();
				if (i == 0) {
					keys.add(param);/* 每组的第一个是key */
				} else {
					values.add(param);
				}
			}
		}

		return Tuples.of(keys, values);
	}

	/**
	 * 分组拆分
	 * 
	 * @param <T>
	 * @param params   k1 v1 v2 k2 v3 v4 ...
	 * @param keyCount 2
	 * @return k1 k2 ... , v1 v2 v3 v4 ...
	 */
	public static <T> Tuple2<List<T>/* keys */, List<T>/* values */> splitByKeyGroup(T[] params, int keyCount) {
		Assert.notEmpty(params, "params must not empty");
		return splitByKeyGroup(Arrays.asList(params), keyCount);
	}

	/**
	 * 
	 * @param <T>
	 * @param keysvalues key value key value ...
	 * @return
	 */
	public static <T> Map<T, T> keysValuesToMap(Collection<T> keysvalues) {
		Assert.notEmpty(keysvalues, "keysvalues must not empty");
		Assert.isTrue(keysvalues.size() % 2 == 0, "invalid keysvalues size");

		Map<T, T> map = new HashMap<>(keysvalues.size(), 1);

		Iterator<T> iterator = keysvalues.iterator();
		for (int i = 0; i < keysvalues.size(); i += 2) {
			T key = iterator.next();
			T value = iterator.next();
			map.put(key, value);
		}
		return map;
	}
	/**
	 * 
	 * @param <T>
	 * @param keysvalues key value key value ...
	 * @return
	 */
	public static <T> Map<T, T> keysValuesToMap(T[] keysvalues) {
		Assert.notEmpty(keysvalues, "keysvalues must not empty");
		return keysValuesToMap(Arrays.asList(keysvalues));
	}
}

package io.github.icodegarden.commons.lang.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CollectionUtils {

	/**
	 * 这是个环形截取工具,不会越界<br>
	 * 从fromIndex开始往后再取maxNum个
	 * 
	 * @param <T>
	 * @param elements
	 * @param fromIndex 包含该位置
	 * @param maxNum 最多截取个数，如果元素个数是10，maxNum是20，最多只返回10个
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
	 * 安全截取，不会越界
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
	 * 每个String转byte[]，并把集合转数组
	 * 不会去重
	 * 
	 * @param values
	 * @return
	 */
	public static byte[][] toBytesArray(Collection<String> values) {
		List<byte[]> vBytes = values.stream().map(v -> v.getBytes()).collect(Collectors.toList());
		byte[][] vBytesArray = vBytes.toArray(new byte[vBytes.size()][]);
		return vBytesArray;
	}
}

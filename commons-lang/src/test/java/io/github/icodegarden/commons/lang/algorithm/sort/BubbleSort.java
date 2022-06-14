package io.github.icodegarden.commons.lang.algorithm.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 默认是升序，Comparator可以自定义顺序
 * 
 * @author Fangfang.Xu
 *
 */
class BubbleSort<K> {

	private final Comparator<? super K> comparator;

	public BubbleSort() {
		comparator = null;
	}

	public BubbleSort(Comparator<? super K> comparator) {
		this.comparator = comparator;
	}

	/**
	 * length-1次比较：1跟2比，大的排2；2跟3比，大的排3；3跟4比...把最大的排最后
	 * length-2次比较：1跟2比，大的排2；2跟3比，大的排3；3跟4比...把次大的排最后 ...
	 * 
	 * 第1个循环决定循环次数，第2个循环决定比较次数
	 * 
	 * @param arr
	 */
	public void sort(K[] arr) {
		for (int i = 0; i < arr.length - 1; i++) {
			for (int j = 0; j < arr.length - 1 - i; j++) {
				if (compare(arr[j], arr[j + 1]) > 0) {
					K temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
				}
			}
		}
	}

	public void sort(List<K> arr) {
		for (int i = 0; i < arr.size() - 1; i++) {
			for (int j = 0; j < arr.size() - 1 - i; j++) {
				if (compare(arr.get(j), arr.get(j + 1)) > 0) {
					K temp = arr.get(j);
					arr.set(j, arr.get(j + 1));
					arr.set(j + 1, temp);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	final int compare(Object k1, Object k2) {
		return comparator == null ? ((Comparable<? super K>) k1).compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
	}
}

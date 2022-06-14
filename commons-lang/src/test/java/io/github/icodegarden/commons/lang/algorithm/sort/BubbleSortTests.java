package io.github.icodegarden.commons.lang.algorithm.sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class BubbleSortTests {

	@Test
	void sort_int() {
		Integer[] arr = { 49, 38, 65, 97, 76, 13, 27, 49 };

		BubbleSort<Integer> sort = new BubbleSort<>();
		sort.sort(arr);

		assertThat(Arrays.asList(arr)).isEqualTo(Arrays.asList(13, 27, 38, 49, 49, 65, 76, 97));
		// ----------------------------------------------
		List<Integer> list = Arrays.asList(49, 38, 65, 97, 76, 13, 27, 49);

		sort.sort(list);

		assertThat(list).isEqualTo(Arrays.asList(13, 27, 38, 49, 49, 65, 76, 97));
	}

	@Test
	void sort_withComparator() {
		User1[] arr = { new User1(49), new User1(38), new User1(65), new User1(97), new User1(76), new User1(13),
				new User1(27), new User1(49) };

		/**
		 * 不给Comparator则要求实现Comparable
		 */
		assertThrows(ClassCastException.class, () -> new BubbleSort().sort(arr));

		BubbleSort<User1> sort = new BubbleSort<>((o1, o2) -> {
			return o1.age > o2.age ? 1 : 0;
		});

		sort.sort(arr);

		assertThat(Arrays.asList(arr).stream().map(u -> u.age).collect(Collectors.toList()))
				.isEqualTo(Arrays.asList(13, 27, 38, 49, 49, 65, 76, 97));
		// ---------------------------------------------------------
		List<User1> list = Arrays.asList(new User1(49), new User1(38), new User1(65), new User1(97), new User1(76),
				new User1(13), new User1(27), new User1(49));

		sort.sort(list);

		assertThat(list.stream().map(u -> u.age).collect(Collectors.toList()))
				.isEqualTo(Arrays.asList(13, 27, 38, 49, 49, 65, 76, 97));

		// --------------------------------------------------------------------
		list = Arrays.asList(new User1(49), new User1(38), new User1(65), new User1(97), new User1(76), new User1(13),
				new User1(27), new User1(49));

		sort = new BubbleSort<>((o1, o2) -> {
			return o2.age > o1.age ? 1 : 0;// 倒序
		});

		sort.sort(list);

		List<Integer> target = Arrays.asList(13, 27, 38, 49, 49, 65, 76, 97);
		target.sort(Comparator.reverseOrder());
		assertThat(list.stream().map(u -> u.age).collect(Collectors.toList())).isEqualTo(target);
	}

	@Test
	void sort_withoutComparator() {
		User2[] arr = { new User2(49), new User2(38), new User2(65), new User2(97), new User2(76), new User2(13),
				new User2(27), new User2(49) };

		BubbleSort<User2> sort = new BubbleSort<>();

		sort.sort(arr);

		assertThat(Arrays.asList(arr).stream().map(u -> u.age).collect(Collectors.toList()))
				.isEqualTo(Arrays.asList(13, 27, 38, 49, 49, 65, 76, 97));
		// ----------------------------------------------------------
		List<User2> list = Arrays.asList(new User2(49), new User2(38), new User2(65), new User2(97), new User2(76),
				new User2(13), new User2(27), new User2(49));
		sort.sort(list);

		assertThat(list.stream().map(u -> u.age).collect(Collectors.toList()))
				.isEqualTo(Arrays.asList(13, 27, 38, 49, 49, 65, 76, 97));
	}

	private class User1 {
		int age;

		public User1(int age) {
			this.age = age;
		}
	}

	private class User2 implements Comparable<User2> {
		int age;

		public User2(int age) {
			this.age = age;
		}

		@Override
		public int compareTo(User2 o2) {
			return this.age > o2.age ? 1 : 0;
		}
	}
}

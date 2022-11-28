package io.github.icodegarden.commons.lang.util;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ClassUtilsTests {

	@Test
	void getGenericInterfaceTypes() throws Exception {
		/**
		 * Object无接口
		 */
		Object object = new Object();
		List<Class<?>> types = ClassUtils.getGenericInterfaceTypes(object, User.class);
		Assertions.assertThat(types).isEmpty();
		/**
		 * group的接口无泛型
		 */
		Group1 group1 = new Group1();
		types = ClassUtils.getGenericInterfaceTypes(group1, User.class);
		Assertions.assertThat(types).isEmpty();
		/**
		 * 直接接口
		 */
		User1 user1 = new User1();
		types = ClassUtils.getGenericInterfaceTypes(user1, User.class);
		Assertions.assertThat(types).hasSize(2);
		Assertions.assertThat(types.get(0)).isEqualTo(String.class);
		Assertions.assertThat(types.get(1)).isEqualTo(Long.class);
		/**
		 * 间接接口
		 */
		User2 user2 = new User2();
		types = ClassUtils.getGenericInterfaceTypes(user2, User.class);
		Assertions.assertThat(types).hasSize(2);
		Assertions.assertThat(types.get(0)).isEqualTo(String.class);
		Assertions.assertThat(types.get(1)).isEqualTo(Long.class);
	}

	private interface User<T, R> {
	}

	private class User1 implements User<String, Long> {

	}

	private class User2 extends User1 {

	}
	
	private interface Group {
	}
	private class Group1 implements Group{

	}
}

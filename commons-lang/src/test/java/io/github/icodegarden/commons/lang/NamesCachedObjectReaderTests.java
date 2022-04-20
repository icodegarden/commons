package io.github.icodegarden.commons.lang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.registry.DefaultRegisteredInstance;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class NamesCachedObjectReaderTests {

	@Test
	void listNamedObjects() throws Exception {
		NamedObjectReader<RegisteredInstance> namedObjectReader = mock(NamedObjectReader.class);
		NamesCachedObjectReader namesCachedObjectReader = new NamesCachedObjectReader(Arrays.asList("worker"),
				namedObjectReader, Long.MAX_VALUE);
		// 参数错误-------------------------
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> namesCachedObjectReader.listNamedObjects("not"))
				.withMessageContaining("not supported");

		// 还未注册，没有实例-------------------------
		List<NamedObject> workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).isEmpty();

		// 注册后，实例0，因为缓存还未刷新-------------------------
		RegisteredInstance registeredInstance1 = new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000);
		RegisteredInstance registeredInstance2 = new DefaultRegisteredInstance("worker", "worker2", "1.1.1.2", 10000);
		doReturn(Arrays.asList(registeredInstance1, registeredInstance2)).when(namedObjectReader)
				.listNamedObjects(eq("worker"));

		workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).isEmpty();

		// 刷新缓存，实例2，-------------------------
		namesCachedObjectReader.refreshCache();

		workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).hasSize(2);
	}

	@Test
	void addInstance() throws Exception {
		NamedObjectReader<RegisteredInstance> namedObjectReader = mock(NamedObjectReader.class);
		NamesCachedObjectReader namesCachedObjectReader = new NamesCachedObjectReader(Arrays.asList("worker"),
				namedObjectReader, Long.MAX_VALUE);

		// 外部增加一个实例 断言1个 -------------------------------
		RegisteredInstance outer = new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000);
		namesCachedObjectReader.addObject(outer);

		List<NamedObject> workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).hasSize(1);

		// 再注册一个实例，断言1个 ，因为此时缓存未刷新-------------------------------
		RegisteredInstance registeredInstance1 = new DefaultRegisteredInstance("worker", "worker2", "1.1.1.2", 10000);
		LinkedList<RegisteredInstance> linkedList = new LinkedList<RegisteredInstance>();
		linkedList.add(registeredInstance1);
		doReturn(linkedList).when(namedObjectReader).listNamedObjects(eq("worker"));

		workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).hasSize(1);
		assertThat(workers.get(0)).isEqualTo(outer);

		// 缓存刷新，断言1个 ，因为缓存刷新后是全新覆盖-------------------------------
		namesCachedObjectReader.refreshCache();

		workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).hasSize(1);
		assertThat(workers.get(0)).isEqualTo(registeredInstance1);

		// 再外部增加一个实例 断言2个 ，因为此时缓存未刷新-------------------------------
		namesCachedObjectReader.addObject(outer);

		workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).hasSize(2);
	}

	@Test
	void removeInstance() throws Exception {
		NamedObjectReader<RegisteredInstance> namedObjectReader = mock(NamedObjectReader.class);
		NamesCachedObjectReader namesCachedObjectReader = new NamesCachedObjectReader(Arrays.asList("worker"),
				namedObjectReader, 100);

		// 外部增加一个实例，再删除 断言0个 -------------------------------
		RegisteredInstance outer = new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000);
		namesCachedObjectReader.addObject(outer);
		// 以new方式构造来检查hashcode equals
		namesCachedObjectReader.removeObject(new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000));

		List<NamedObject> workers = namesCachedObjectReader.listNamedObjects("worker");
		assertThat(workers).hasSize(0);
	}

}

package io.github.icodegarden.commons.test.concurrent.registry;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.registry.DatabaseRegistry;
import io.github.icodegarden.commons.lang.concurrent.registry.Registration;
import io.github.icodegarden.commons.lang.concurrent.registry.Registry;
import io.github.icodegarden.commons.lang.concurrent.registry.RegistryListener;
import io.github.icodegarden.commons.lang.concurrent.registry.RegistryMaxIndexException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class RegistryTests {

	protected String name = "myservice";
	protected String identifier = "127.0.0.1:8080";
	protected String metadata = "{\"ts\":1000}";
	protected String info = "{\"ts2\":2000}";

	protected abstract Registry<Registration> newRegistry(RegistryListener registryListener);

	protected TestRegistryListener registryListener;

	@BeforeEach
	void init() {
		registryListener = new TestRegistryListener();
	}

	public class TestRegistryListener implements RegistryListener {
		public Integer index;
		public Boolean leaseExpired;

		@Override
		public void onRegistered(Registration registration, Integer index) {
			this.index = index;
		}

		@Override
		public void onLeaseExpired(Registration registration) {
			leaseExpired = true;
		}
	}

	/**
	 * 验证注册
	 */
	@Test
	void register() throws Exception {
		Registry<Registration> registry = newRegistry(registryListener);

		Registration registration = new Registration.Default(name, identifier, 30L, metadata, info);
		registry.register(registration);
		/**
		 * 验证注册成功有回调
		 */
		Assertions.assertThat(registryListener.index).isNotNull();

		boolean registered = registry.isRegistered(registration);
		Assertions.assertThat(registered).isTrue();

		registry.close();
	}

	/**
	 * 注册然后注销
	 */
	@Test
	void register_then_deregister() throws Exception {
		Registry<Registration> registry = newRegistry(registryListener);

		Registration registration = new Registration.Default(name, identifier, 30L, metadata, info);
		registry.register(registration);

		registry.deregister(registration);
		/**
		 * 验证deregister
		 */
		boolean registered = registry.isRegistered(registration);
		Assertions.assertThat(registered).isFalse();

		registry.close();
	}

	/**
	 * 注册然后close
	 */
	@Test
	void register_thenClose_willTriggerDeregister() throws Exception {
		Registry<Registration> registry = newRegistry(registryListener);

		String name = "leaseScheduleUpdate";
		for (int i = 0; i < 4; i++) {
			Registration registration = new Registration.Default(name, UUID.randomUUID().toString(), 30L, metadata,
					info);
			registry.register(registration);
		}

		registry.close();

		/**
		 * 验证close会deregister所有用这个registry注册的实例
		 */
		List<Registration> instances = registry.listInstances(name);
		Assertions.assertThat(instances).isEmpty();
	}

	/**
	 * 注册然后等待过期时间到，但由于会自动更新租期所以注册一直有效
	 */
	@Test
	void leaseScheduleUpdate() throws Exception {
		Registry<Registration> registry = newRegistry(registryListener);
		long minExpireSeconds = 3;
		((DatabaseRegistry) registry).setMinExpireSeconds(minExpireSeconds);

		Registration registration = new Registration.Default(name, identifier, 30L, metadata, info);
		registry.register(registration);

		boolean registered = registry.isRegistered(registration);
		Assertions.assertThat(registered).isTrue();

		Thread.sleep(minExpireSeconds + 1);
		/**
		 * 过期时间到，但依然注册有效
		 */
		registered = registry.isRegistered(registration);
		Assertions.assertThat(registered).isTrue();

		registry.close();
	}

	/**
	 * 最多注册数量
	 */
	@Test
	void maxIndexPerName() throws Exception {
		Registry<Registration> registry = newRegistry(registryListener);
		int maxIndexPerName = 4;
		((DatabaseRegistry) registry).setMaxIndexPerName(maxIndexPerName);

		final String name = "maxIndexPerName";//使用独立的名称以免被干扰
		
		for (int i = 0; i < maxIndexPerName; i++) {
			Registration registration = new Registration.Default(name, UUID.randomUUID().toString(), 30L, metadata,
					info);
			registry.register(registration);
		}

		Assertions.assertThatExceptionOfType(RegistryMaxIndexException.class).isThrownBy(() -> {
			Registration registration = new Registration.Default(name, UUID.randomUUID().toString(), 30L, metadata,
					info);
			registry.register(registration);
		});

		// 不同name隔离-----------------------------------------------------
		String name2 = this.name + "2";
		for (int i = 0; i < maxIndexPerName; i++) {
			Registration registration = new Registration.Default(name2, UUID.randomUUID().toString(), 30L, metadata,
					info);
			registry.register(registration);
		}
		Assertions.assertThatExceptionOfType(RegistryMaxIndexException.class).isThrownBy(() -> {
			Registration registration = new Registration.Default(name2, UUID.randomUUID().toString(), 30L, metadata,
					info);
			registry.register(registration);
		});

		registry.close();
	}

	/**
	 * 并发注册
	 */
	@Test
	void concurrentRegister() throws Exception {
		/**
		 * 进程数
		 */
		int processes = 16;
		CountDownLatch countDownLatch = new CountDownLatch(processes);

		for (int i = 0; i < processes; i++) {
			new Thread() {
				public void run() {
					/**
					 * 独立Registry，模拟进程
					 */
					Registry<Registration> registry = newRegistry(registryListener);
					((DatabaseRegistry) registry).setMaxIndexPerName(10000);//调大
					/**
					 * 每个注册50个
					 */
					for (int i = 0; i < 50; i++) {
						Registration registration = new Registration.Default(name, UUID.randomUUID().toString(), 30L,
								metadata, info);
						registry.register(registration);
					}
					registry.close();

					countDownLatch.countDown();
				};
			}.start();
		}

		Assertions.assertThat(countDownLatch.await(300, TimeUnit.SECONDS)).isTrue();// true不超时
	}
}

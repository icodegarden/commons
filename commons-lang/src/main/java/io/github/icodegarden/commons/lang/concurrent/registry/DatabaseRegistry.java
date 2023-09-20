package io.github.icodegarden.commons.lang.concurrent.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.lang.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 注册（条件是未注册的、或超时的）<br>
 * 
 * 定时 更新租期（条件是自己的票，若条件不能匹配说明已有超时，需要重新注册）
 * 
 * 查询所有已注册的 （条件已注册且未超时的）<br>
 * 
 * lease超时的视为自动过期<br>
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class DatabaseRegistry<ID> implements Registry<Registration> {

	private Map<String/* name-identifier */, Tuple2<ScheduledFuture<?>, Registration>> tasks = new ConcurrentHashMap<>();

	private ScheduledThreadPoolExecutor threadPool = ThreadUtils
			.newLightResourceScheduledThreadPool(this.getClass().getSimpleName());

	private long minExpireSeconds = 30;
	private long lockAcquireTimeoutMillis = 10000;
	private int maxIndexPerName = 1024;

	private final DatabaseRegistryRepository<ID> registryRepository;
	private final RegistryListener listener;

	public DatabaseRegistry(DatabaseRegistryRepository<ID> registryRepository, RegistryListener listener) {
		this.registryRepository = registryRepository;
		this.listener = listener;
	}

	public void setMinExpireSeconds(long minExpireSeconds) {
		this.minExpireSeconds = minExpireSeconds;
	}

	public void setLockAcquireTimeoutMillis(long lockAcquireTimeoutMillis) {
		this.lockAcquireTimeoutMillis = lockAcquireTimeoutMillis;
	}

	public void setMaxIndexPerName(int maxIndexPerName) {
		this.maxIndexPerName = maxIndexPerName;
	}

	/**
	 * @param name lock按name隔离
	 */
	protected abstract DistributedLock getRegisterLock(String name, Long expireSeconds);

	@Override
	public void register(Registration registration) throws RegistryException {
		if (registration.getExpireSeconds() < minExpireSeconds) {
			throw new IllegalArgumentException("expireSeconds must gte " + minExpireSeconds);
		}

		SimpleRegistrationDO<ID> one = registryRepository.findByRegistration(registration,
				SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
		if (one != null) {
			/*
			 * 若已注册则更新
			 */
			String metadata = registration.getMetadata() != null ? JsonUtils.serialize(registration.getMetadata())
					: null;
			String info = registration.getInfo() != null ? JsonUtils.serialize(registration.getInfo()) : null;
			registryRepository.updateRegistration(one.getId(), metadata, info);
			return;
		}

		DistributedLock lock = getRegisterLock("DatabaseRegistry-" + registration.getName(), 30L);
		if (lock.acquire(lockAcquireTimeoutMillis)) {
			try {
				one = registryRepository.findAnyAvailableByName(registration.getName(),
						SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
				if (one != null) {
					/*
					 * 查询可以使用的票据，存在则使用
					 */
					registryRepository.updateOnRegister(one.getId(), registration,
							SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));

					listener.onRegistered(registration, one.getIndex());

					scheduleUpdateLease(registration);
				} else {
					/*
					 * 否则查询最后一条，如果不存在或序号未满，则允许注册，否则满了
					 */
					one = registryRepository.findMaxIndexByName(registration.getName());
					if (one == null || one.getIndex() < maxIndexPerName) {
						int index = Optional.ofNullable(one != null ? one.getIndex() : 0).get() + 1/* 递增 */;
						registryRepository.createOnRegister(index, registration,
								SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));

						listener.onRegistered(registration, index);

						scheduleUpdateLease(registration);
					} else {
						throw new RegistryMaxIndexException(String.format("index of %s is gte maxIndex of %d",
								registration.getName(), maxIndexPerName));
					}
				}
			} finally {
				lock.release();
			}
		} else {
			throw new RegistryTimeoutException("Lock Acquire Timeout " + lockAcquireTimeoutMillis + "ms.");
		}
	}

	private void scheduleUpdateLease(Registration registration) {
		/**
		 * 过期时间的1/3作为调度频率
		 */
		long updateLeaseInterval = registration.getExpireSeconds() / 3;

		ScheduledFuture<?> f = threadPool.scheduleWithFixedDelay(() -> {
			try {
				boolean b = registryRepository.updateLease(registration,
						SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now())) == 1;
				if (!b) {
					/**
					 * 说明已有超时，需要重新注册
					 */

					listener.onLeaseExpired(registration);

					register(registration);
				}
			} catch (Exception e) {
				log.error("ex on scheduleUpdateLease.", e);
			}
		}, updateLeaseInterval, updateLeaseInterval, TimeUnit.SECONDS);

		Tuple2<ScheduledFuture<?>, Registration> pre = tasks.put(taskKey(registration), Tuples.of(f, registration));
		if (pre != null) {
			/*
			 * 重新注册后会开启新的调度，旧的停止
			 */
			pre.getT1().cancel(true);
		}
	}

	private String taskKey(Registration registration) {
		return registration.getName() + "-" + registration.getIdentifier();
	}

	@Override
	public void deregister(Registration registration) throws RegistryException {
		SimpleRegistrationDO<ID> one = registryRepository.findByRegistration(registration,
				SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
		if (one != null) {
			registryRepository.updateOnDeregister(one.getId());
		}
	}

	@Override
	public boolean isRegistered(Registration registration) {
		SimpleRegistrationDO<ID> one = registryRepository.findByRegistration(registration,
				SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
		return one != null;
	}

	@Override
	public List<Registration> listInstances(String name) {
		/**
		 * 只列出有效的注册
		 */
		return registryRepository.findAllRegistered(name, false, false,
				SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
	}

	@Override
	public void close() {
		/**
		 * 先停止调度以免自动触发重新注册
		 */
		threadPool.shutdown();

		/**
		 * 注销
		 */
		for (Tuple2<ScheduledFuture<?>, Registration> one : tasks.values()) {
			Registration registration = one.getT2();
			deregister(registration);
		}
		tasks.clear();
	}

}

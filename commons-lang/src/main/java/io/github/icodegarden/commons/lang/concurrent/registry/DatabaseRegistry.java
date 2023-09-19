package io.github.icodegarden.commons.lang.concurrent.registry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
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
public abstract class DatabaseRegistry implements Registry<Registration>, Discovery<Registration> {

	private ScheduledThreadPoolExecutor threadPool = ThreadUtils
			.newLightResourceScheduledThreadPool(this.getClass().getSimpleName());

	private long lockAcquireTimeoutMillis = 3000;
	private int maxIndexPerName = 1024;

	private final DatabaseRegistryRepository<Object> registryRepository;
	private final RegistryListener listener;

	public DatabaseRegistry(DatabaseRegistryRepository<Object> registryRepository, RegistryListener listener) {
		this.registryRepository = registryRepository;
		this.listener = listener;
	}

	public void setLockAcquireTimeoutMillis(long lockAcquireTimeoutMillis) {
		this.lockAcquireTimeoutMillis = lockAcquireTimeoutMillis;
	}

	public void setMaxIndexPerName(int maxIndexPerName) {
		this.maxIndexPerName = maxIndexPerName;
	}

	protected abstract DistributedLock getRegisterLock();

	@Override
	public void register(Registration registration) throws RegistryException {
		SimpleDO<Object> one = registryRepository.findByRegistration(registration);
		if (one != null) {
			/*
			 * 若已注册则更新
			 */
			registryRepository.updateRegistration(one.getId(), registration);
			return;
		}

		DistributedLock lock = getRegisterLock();
		if (lock.acquire(lockAcquireTimeoutMillis)) {
			try {
				one = registryRepository.findAnyAvailableByName(registration.getName());
				if (one != null) {
					/*
					 * 查询可以使用的票据，存在则使用
					 */
					registryRepository.updateOnRegister(one.getId(), registration);

					listener.onRegistered(registration, one.getIndex());

					scheduleUpdateLease(registration);
				} else {
					/*
					 * 否则查询最后一条，如果不存在或序号未满，则允许注册，否则满了
					 */
					one = registryRepository.findLastByName(registration.getName());
					if (one == null || one.getIndex() < maxIndexPerName) {
						int index = Optional.ofNullable(one != null ? one.getIndex() : 0).get() + 1/* 递增 */;
						registryRepository.createOnRegister(index, registration);

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
		ScheduledThreadPoolExecutor threadPool = ThreadUtils
				.newSingleScheduledThreadPool(this.getClass().getSimpleName());

		/**
		 * 过期时间的1/3作为调度频率
		 */
		long updateLeaseInterval = registration.getExpireSeconds() / 3;

		threadPool.scheduleWithFixedDelay(() -> {
			try {
				boolean b = registryRepository.updateLease(registration);
				if (!b) {
					/**
					 * 说明已有超时，需要重新注册
					 */

					listener.onLeaseExpired(registration);

					register(registration);

					/*
					 * 重新注册后会开启新的调度，旧的停止
					 */
					threadPool.shutdown();
				}
			} catch (Exception e) {
				log.error("ex on scheduleUpdateLease.", e);
			}
		}, updateLeaseInterval, updateLeaseInterval, TimeUnit.SECONDS);

	}

	@Override
	public void deregister(Registration registration) throws RegistryException {
		registryRepository.updateOnDeregister(registration);
	}

	@Override
	public List<Registration> listInstances(String name) {
		/**
		 * 只列出有效的注册
		 */
		return registryRepository.findAllRegistered(name);
	}

	@Override
	public void close() {
		threadPool.shutdown();

		DistributedLock lock = getRegisterLock();
		if (lock.isAcquired()) {
			lock.release();
		}
	}

}

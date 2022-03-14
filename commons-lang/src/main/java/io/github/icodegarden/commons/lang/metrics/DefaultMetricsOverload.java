package io.github.icodegarden.commons.lang.metrics;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.metrics.Metrics.Dimension;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;
import io.github.icodegarden.commons.lang.registry.InstanceRegistry;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultMetricsOverload implements MetricsOverload {
	private static final Logger log = LoggerFactory.getLogger(DefaultMetricsOverload.class);

	/**
	 * 每个对象独占1线程
	 */
	private final ScheduledThreadPoolExecutor scheduleFlushMetricsThreadPool = ThreadPoolUtils
			.newSingleScheduledThreadPool("DefaultMetricsOverload-scheduleFlushMetrics");

	private final InstanceRegistry<? extends RegisteredInstance> instanceRegistry;
	private final InstanceMetrics<? extends Metrics> instanceMetrics;
	private Metrics metrics;

	private AtomicLong localVersion = new AtomicLong();
	private AtomicLong flushedVersion = new AtomicLong();
	private boolean scheduleFlushMetrics;

	public DefaultMetricsOverload(InstanceRegistry<? extends RegisteredInstance> instanceRegistry,
			InstanceMetrics<? extends Metrics> instanceMetrics, Metrics metrics) {
		this.instanceRegistry = instanceRegistry;
		this.instanceMetrics = instanceMetrics;
		this.metrics = metrics;
	}

	public void resetMetrics(Metrics metrics) {
		this.metrics = metrics;
		localVersion.set(0);
	}

	public void enableScheduleFlushMetrics(int scheduleMillis) {
		synchronized (this) {
			if (!scheduleFlushMetrics) {
				scheduleFlushMetricsThreadPool.scheduleWithFixedDelay(() -> {
					try {
						flushMetricsIfNecessary();
					} catch (Throwable e) {
						if (log.isWarnEnabled()) {
							log.warn("ex on flushMetricsIfNecessary", e);
						}
					}
				}, 0, scheduleMillis, TimeUnit.MILLISECONDS);

				scheduleFlushMetrics = true;
			}
		}
	}

	@Override
	public Metrics getMetrics() {
		RegisteredInstance instance = instanceRegistry.getRegistered();
		if (instance == null) {
			instance = instanceRegistry.registerIfNot();
			flushMetrics();
		}
		return instanceMetrics.getMetrics(instance);
	}

	@Override
	public Metrics getLocalMetrics() {
		return metrics;
	}

	@Override
	public boolean willOverload(OverloadCalc calc) {
		Dimension dimension = metrics.getDimension(DimensionName.Jobs);
		return dimension.getUsed() + calc.ofOverload() > dimension.getMax();
	}

	@Override
	public boolean incrementOverload(OverloadCalc calc) {
		/**
		 * 并发
		 */
		synchronized (this) {
			if (willOverload(calc)) {
				return false;
			}
			boolean changed = metrics.incrementDimension(DimensionName.Jobs, calc.ofOverload());
			if (changed) {
				localVersion.incrementAndGet();
			}
			return true;
		}
	}

	@Override
	public void decrementOverload(OverloadCalc calc) {
		/**
		 * 方法内线程安全
		 */
		boolean changed = metrics.decrementDimension(DimensionName.Jobs, calc.ofOverload());
		if (changed) {
			localVersion.incrementAndGet();
		}
	}

	@Override
	public void flushMetrics() {
		metrics.refreshUsedValues();

		RegisteredInstance instance = instanceRegistry.registerIfNot();
		instanceMetrics.setMetrics(instance, metrics);

		flushedVersion.set(localVersion.get());
	}

	private void flushMetricsIfNecessary() {
		if (localVersion.get() != flushedVersion.get()) {
			flushMetrics();
		}
	}

	@Override
	public void close() throws IOException {
		scheduleFlushMetricsThreadPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		scheduleFlushMetricsThreadPool.shutdown();
	}
}
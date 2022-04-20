package io.github.icodegarden.commons.exchange.loadbalance;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface MetricsInstance {

	@Nullable
	RegisteredInstance getAvailable();

	@Nullable
	Metrics getMetrics();

	boolean isOverload();

}

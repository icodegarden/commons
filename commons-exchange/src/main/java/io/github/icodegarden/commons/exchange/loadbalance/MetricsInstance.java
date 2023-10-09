package io.github.icodegarden.commons.exchange.loadbalance;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;

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

package io.github.icodegarden.commons.exchange.loadbalance;

import java.util.LinkedList;
import java.util.Queue;

import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class Constants {

	static final Queue<MetricsInstance> EMPTY_METRICS_INSTANCE = new LinkedList<MetricsInstance>();
	static final Metrics IGNORE_METRICS = new Metrics(new Metrics.Dimension(new DimensionName("ignore"), 1, 0));
}

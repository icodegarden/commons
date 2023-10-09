package io.github.icodegarden.commons.lang.metricsregistry;

import java.io.Closeable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface MetricsOverload extends Closeable {

	/**
	 * 
	 * @return 实时数据
	 */
	Metrics getMetrics();
	
	Metrics getLocalMetrics();

	boolean willOverload(OverloadCalc obj);
	
	/**
	 * 
	 * @param job
	 * @return false表示会导致超载
	 */
	boolean incrementOverload(OverloadCalc obj);

	void decrementOverload(OverloadCalc obj);
	/**
	 * 立即刷入
	 */
	void flushMetrics();
}
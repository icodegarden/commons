package io.github.icodegarden.commons.lang.trace;

import java.util.concurrent.Callable;

import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SkywalkingTraceCtx extends TraceCtx {

	@Override
	public String traceId() {
		return TraceContext.traceId();
	}

	@Override
	public String spanId() {
		return Integer.toString(TraceContext.spanId());
	}

	@Override
	public String segmentId() {
		return TraceContext.segmentId();
	}

	@Override
	public void tag(String key, String value) {
		ActiveSpan.tag(key, value);
	}

	@Override
	public void putCorrelation(String key, String value) {
		TraceContext.putCorrelation(key, value);
	}

	@Override
	public Runnable traceRunnable(Runnable r) {
		return RunnableWrapper.of(r);
	}

	@Override
	public <V> Callable<V> traceCallable(Callable<V> c) {
		return CallableWrapper.of(c);
	}
}

package io.github.icodegarden.commons.lang.trace;

import java.util.concurrent.Callable;

import com.alibaba.arms.sdk.v1.async.TraceCallable;
import com.alibaba.arms.sdk.v1.async.TraceRunnable;
import com.alibaba.arms.tracing.Span;
import com.alibaba.arms.tracing.Tracer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ArmsTraceCtx extends TraceCtx {

	@Override
	public String traceId() {
		Span span = Tracer.builder().getSpan();
		return span.getTraceId();
	}

	@Override
	public String spanId() {
//		return "Arms-1";
		return null;
	}

	@Override
	public String segmentId() {
		Span span = Tracer.builder().getSpan();
		return span.getRpcId();
	}

	@Override
	public void tag(String key, String value) {
		Span span = Tracer.builder().getSpan();
		span.setTag(key, value);
	}

	@Override
	public void putCorrelation(String key, String value) {
	}

	@Override
	public Runnable traceRunnable(Runnable r) {
		return TraceRunnable.asyncEntry(r);
	}

	@Override
	public <V> Callable<V> traceCallable(Callable<V> c) {
		return TraceCallable.asyncEntry(c);
	}
}

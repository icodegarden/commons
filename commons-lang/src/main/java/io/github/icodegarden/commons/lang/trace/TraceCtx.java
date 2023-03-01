package io.github.icodegarden.commons.lang.trace;

import java.util.concurrent.Callable;

import org.springframework.util.ClassUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class TraceCtx {

	private static TraceCtx traceCtx;

	static {
		if (ClassUtils.isPresent("com.alibaba.arms.tracing.Tracer", null)) {
			traceCtx = new ArmsTraceCtx();
		} else if (ClassUtils.isPresent("org.apache.skywalking.apm.toolkit.trace.TraceContext", null)) {
			traceCtx = new SkywalkingTraceCtx();
		} else {
			traceCtx = new PseudoTraceCtx();
		}
	}

	public static TraceCtx get() {
		return traceCtx;
	}

	public abstract String traceId();

	public abstract String spanId();

	public abstract String segmentId();

	public abstract void tag(String key, String value);

	public abstract void putCorrelation(String key, String value);

	public abstract Runnable traceRunnable(Runnable r);

	public abstract <V> Callable<V> traceCallable(Callable<V> c);

	private static class PseudoTraceCtx extends TraceCtx {

		@Override
		public String traceId() {
			return "Pseudo-1";
		}

		@Override
		public String spanId() {
			return "Pseudo-1";
		}

		@Override
		public String segmentId() {
			return "Pseudo-1";
		}

		@Override
		public void tag(String key, String value) {
		}

		@Override
		public void putCorrelation(String key, String value) {
		}

		@Override
		public Runnable traceRunnable(Runnable r) {
			return r;
		}

		@Override
		public <V> Callable<V> traceCallable(Callable<V> c) {
			return c;
		}

	}
}

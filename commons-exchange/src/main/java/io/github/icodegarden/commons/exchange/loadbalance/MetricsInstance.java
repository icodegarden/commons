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

	public static class Default implements MetricsInstance {

		private RegisteredInstance registered;
		private Metrics metrics;

		/**
		 * @param registered
		 * @param metrics
		 */
		public Default(RegisteredInstance registered, @Nullable Metrics metrics) {
			this.registered = registered;
			this.metrics = metrics;
		}

		public RegisteredInstance getAvailable() {
			return isOverload() ? null : registered;
		}

		public Metrics getMetrics() {
			return metrics;
		}

		public boolean isOverload() {
			if (metrics != null) {
				return metrics.isOverload();
			}
			return false;
		}

		@Override
		public String toString() {
			return "[registered=" + registered + ", metrics=" + metrics + ", overload=" + isOverload() + "]";
		}

	}

}

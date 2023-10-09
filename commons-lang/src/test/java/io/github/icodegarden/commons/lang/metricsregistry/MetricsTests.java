package io.github.icodegarden.commons.lang.metricsregistry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.metricsregistry.Metrics;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics.Dimension;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MetricsTests {

	@Test
	void compareTo() {
		// 2/4 + 7/8
		Metrics m1 = new Metrics(new Dimension(Metrics.DimensionName.Memory, 4, 2, 1),
				new Dimension(Metrics.DimensionName.Jobs, 8, 1, 1));
		// 7/8 + 3/4
		Metrics m2 = new Metrics(new Dimension(Metrics.DimensionName.Memory, 8, 1, 1),
				new Dimension(Metrics.DimensionName.Jobs, 4, 1, 1));
		/**
		 * 权重都是1，两者的剩余率m2多，m2排前
		 */
		assertThat(m2.compareTo(m1)).isEqualTo(-1);

		m1 = new Metrics(new Dimension(Metrics.DimensionName.Memory, 4, 2, 1),
				new Dimension(Metrics.DimensionName.Jobs, 8, 1, 10));
		m2 = new Metrics(new Dimension(Metrics.DimensionName.Memory, 8, 1, 1),
				new Dimension(Metrics.DimensionName.Jobs, 4, 1, 1));
		/**
		 * 只调大m1的jobs权重，m1排前
		 */
		assertThat(m2.compareTo(m1)).isEqualTo(1);
	}

}

package io.github.icodegarden.commons.lang.endpoint;

import java.util.ArrayList;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown.Registry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class GracefullyShutdownTests {

	@Test
	void all() throws Exception {
		Registry registry = GracefullyShutdown.Registry.singleton();

		ArrayList<String> names = new ArrayList<String>();

		GracefullyShutdown g1 = new GracefullyShutdown() {
			@Override
			public String shutdownName() {
				return "g1";
			}

			@Override
			public void shutdown() {
				names.add(this.shutdownName());
			}

			@Override
			public int shutdownOrder() {
				return 1;
			}
		};
		GracefullyShutdown g2 = new GracefullyShutdown() {
			@Override
			public String shutdownName() {
				return "g2";
			}

			@Override
			public void shutdown() {
				names.add(this.shutdownName());
			}

			@Override
			public int shutdownOrder() {
				return -1;
			}
		};

		registry.register(g1);
		registry.register(g2);

		registry.shutdownRegistered();

		Assertions.assertThat(names.size()).isEqualTo(2);
		Assertions.assertThat(names.get(0)).isEqualTo(g2.shutdownName());// g2的order小
		Assertions.assertThat(names.get(1)).isEqualTo(g1.shutdownName());
	}

}

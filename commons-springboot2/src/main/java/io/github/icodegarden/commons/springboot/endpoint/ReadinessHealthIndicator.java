package io.github.icodegarden.commons.springboot.endpoint;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.availability.ReadinessState;

/**
 * readiness集成到health
 * 
 * @author Fangfang.Xu
 *
 */
public class ReadinessHealthIndicator extends AbstractHealthIndicator {

	private final ReadinessEndpoint readinessEndpoint;

	public ReadinessHealthIndicator(ReadinessEndpoint readinessEndpoint) {
		this.readinessEndpoint = readinessEndpoint;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			ReadinessState readiness = readinessEndpoint.readiness();
			if (readiness == ReadinessState.ACCEPTING_TRAFFIC) {
				builder.up();
			} else {
				builder.down();
			}
		} catch (Exception e) {
			builder.down();
		}
	}

}
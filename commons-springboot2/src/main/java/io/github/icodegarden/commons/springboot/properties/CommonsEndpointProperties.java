package io.github.icodegarden.commons.springboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.endpoint")
@Getter
@Setter
@ToString
public class CommonsEndpointProperties {

	private Readiness readiness = new Readiness();

	@Getter
	@Setter
	@ToString
	public static class Readiness {
		private Long shutdownWaitMs = 30000L;
	}
}
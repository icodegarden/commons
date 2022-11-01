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
@ConfigurationProperties(prefix = "commons.sentinel")
@Getter
@Setter
@ToString
public class CommonsSentinelProperties {

	private Cluster cluster = new Cluster();
	private Nacos nacos = new Nacos();

	@Getter
	@Setter
	@ToString
	public static class Cluster {
		private Boolean enabled = false;
		private String serverAddr;
		private Integer serverPort;
	}

	@Getter
	@Setter
	@ToString
	public static class Nacos {
		private String groupId = "Sentinel";
	}
}
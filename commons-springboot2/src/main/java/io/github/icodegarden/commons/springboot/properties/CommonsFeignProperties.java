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
@ConfigurationProperties(prefix = "commons.feign")
@Getter
@Setter
@ToString
public class CommonsFeignProperties {

	private Header header = new Header();

	@Getter
	@Setter
	@ToString
	public static class Header {
		private boolean transferAll = false;
		private String userIdIfNotPresent = "sys";
		private String usernameIfNotPresent = "sys";
	}
}
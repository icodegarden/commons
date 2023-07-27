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
@ConfigurationProperties(prefix = "commons.web")
@Getter
@Setter
@ToString
public class CommonsWebProperties {

	private ExceptionHandler exceptionHandler = new ExceptionHandler();

	@Getter
	@Setter
	@ToString
	public static class ExceptionHandler {
		private Boolean printErrorStackOnWarn = true;
	}
}
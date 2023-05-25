package io.github.icodegarden.commons.springboot.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.beecomb.client")
@Getter
@Setter
@ToString
public class CommonsBeeCombClientProperties {

	private BasicAuthentication basicAuth = new BasicAuthentication();
	
	private ZooKeeper zookeeper = new ZooKeeper();
	
	private Master master = new Master();

	@Getter
	@Setter
	@ToString
	public class BasicAuthentication {
		private String username;
		private String password;
	}

	@Getter
	@Setter
	@ToString
	public class Master {
		/**
		 * http://1.1.1.1:9898,http://1.1.1.2:9898 ...
		 */
		private String httpHosts;
	}
}
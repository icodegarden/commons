package io.github.icodegarden.commons.lang.concurrent.registry;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Instance {

	/**
	 * @return 名称 例如serviceName
	 */
	String getName();

	String getInstanceId();

	String getHost();

	int getPort();

	@Getter
	@ToString
	class Default implements Instance {
		private String name;
		private String instanceId;
		private String host;
		private int port;

		public Default(String name, String instanceId, String host, int port) {
			super();
			this.name = name;
			this.instanceId = instanceId;
			this.host = host;
			this.port = port;
		}
	}
}

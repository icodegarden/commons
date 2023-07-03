package io.github.icodegarden.commons.springboot.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.redis")
@Getter
@Setter
@ToString
public class CommonsRedisProperties {

	private Cluster cluster;
	private Pool pool;

	@Getter
	@Setter
	@ToString
	public static class Cluster extends JedisCommon {
		private List<Node> nodes;
		private int maxAttempts = 5;

		@Getter
		@Setter
		@ToString
		public static class Node {
			private String host;
			private int port;
		}
	}

	@Getter
	@Setter
	@ToString
	public static class Pool extends JedisCommon {
		private String host;
		private int port;
		private int database = 0;
	}

	@Getter
	@Setter
	@ToString
	public static class JedisCommon {
		private int connectionTimeout = 2000;
		private int soTimeout = 2000;
		private String user;
		private String password;
		private String clientName;
		private boolean ssl = false;

		private int maxIdle = 8;// 默认8
		private int maxTotal = 8;// 默认8
		private int maxWaitMillis = -1;// 默认-1
		private int minIdle = 0;// 默认0
		private long minEvictableIdleTimeMillis = 60000;// 默认60000
		private long timeBetweenEvictionRunsMillis = -1;// 默认-1

		private Lettuce lettuce;
	}

	@Getter
	@Setter
	@ToString
	public static class Lettuce {
		private Integer ioThreadPoolSize;
		private Integer computationThreadPoolSize;
		private Long reconnectDelayMs;
	}
}
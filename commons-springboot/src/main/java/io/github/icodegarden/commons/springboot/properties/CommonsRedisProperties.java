package io.github.icodegarden.commons.springboot.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import redis.clients.jedis.JedisPoolConfig;

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
	public static class Cluster extends JedisPoolConfig {
		private List<Node> nodes;
		private int connectionTimeout = 2000;
		private int soTimeout = 2000;
		private int maxAttempts = 5;
		private String user;
		private String password;
		private String clientName;
		private boolean ssl = false;

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
	public static class Pool extends JedisPoolConfig {
		private String host;
		private int port;
		private int connectionTimeout = 2000;
		private int soTimeout = 2000;
		private String user;
		private String password;
		private int database = 0;
		private String clientName;
		private boolean ssl = false;
	}

}
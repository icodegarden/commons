package io.github.icodegarden.commons.springboot.configuration;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.redis.ClusterRedisExecutor;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Cluster;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Pool;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(RedisExecutor.class)
@EnableConfigurationProperties({ CommonsRedisProperties.class })
@Configuration
public class CommonsRedisAutoConfiguration {

	@ConditionalOnProperty(value = "commons.redis.executor.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public RedisExecutor redisExecutor(CommonsRedisProperties redisProperties) {
		Cluster cluster = redisProperties.getCluster();
		if (cluster != null) {
			Set<HostAndPort> clusterNodes = cluster.getNodes().stream()
					.map(node -> new HostAndPort(node.getHost(), node.getPort())).collect(Collectors.toSet());

			JedisCluster jc = new JedisCluster(clusterNodes, cluster.getConnectionTimeout(), cluster.getSoTimeout(),
					cluster.getMaxAttempts(), cluster.getUser(), cluster.getPassword(), cluster.getClientName(),
					cluster, cluster.isSsl());

			return new ClusterRedisExecutor(jc);
		}

		Pool pool = redisProperties.getPool();
		if (pool != null) {
			JedisPool jp = new JedisPool(pool, pool.getHost(), pool.getPort(), pool.getConnectionTimeout(),
					pool.getSoTimeout(), pool.getUser(), pool.getPassword(), pool.getDatabase(), pool.getClientName(),
					pool.isSsl());
			return new PoolRedisExecutor(jp);
		}

		throw new IllegalStateException("CommonsRedisProperties config error, cluster or pool must not null");
	}

}

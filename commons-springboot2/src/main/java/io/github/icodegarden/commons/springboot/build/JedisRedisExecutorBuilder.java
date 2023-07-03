package io.github.icodegarden.commons.springboot.build;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisClusterRedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisPoolRedisExecutor;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Cluster;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Pool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class JedisRedisExecutorBuilder {

	public static RedisExecutor create(CommonsRedisProperties redisProperties) {
		Cluster cluster = redisProperties.getCluster();
		if (cluster != null) {
			log.info("create RedisExecutor by Cluster");

			Set<HostAndPort> clusterNodes = cluster.getNodes().stream()
					.map(node -> new HostAndPort(node.getHost(), node.getPort())).collect(Collectors.toSet());

			ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();// 使用默认值
			configGenericObjectPoolConfig(connectionPoolConfig, cluster);

			JedisCluster jc = new JedisCluster(clusterNodes, cluster.getConnectionTimeout(), cluster.getSoTimeout(),
					cluster.getMaxAttempts(), cluster.getUser(), cluster.getPassword(), cluster.getClientName(),
					connectionPoolConfig, cluster.isSsl());
			return new JedisClusterRedisExecutor(jc);
		}

		Pool pool = redisProperties.getPool();
		if (pool != null) {
			log.info("create RedisExecutor by Pool");

			JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();// 使用默认值
			configGenericObjectPoolConfig(jedisPoolConfig, pool);

			JedisPool jp = new JedisPool(jedisPoolConfig, pool.getHost(), pool.getPort(), pool.getConnectionTimeout(),
					pool.getSoTimeout(), pool.getUser(), pool.getPassword(), pool.getDatabase(), pool.getClientName(),
					pool.isSsl());
			return new JedisPoolRedisExecutor(jp);
		}

		return null;
	}

	private static void configGenericObjectPoolConfig(GenericObjectPoolConfig<?> genericObjectPoolConfig,
			CommonsRedisProperties.JedisCommon common) {
		genericObjectPoolConfig.setMaxIdle(common.getMaxIdle());
		genericObjectPoolConfig.setMaxTotal(common.getMaxTotal());
		genericObjectPoolConfig.setMaxWaitMillis(common.getMaxWaitMillis());
		genericObjectPoolConfig.setMinEvictableIdleTimeMillis(common.getMinEvictableIdleTimeMillis());
		genericObjectPoolConfig.setMinIdle(common.getMinIdle());
		genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(common.getTimeBetweenEvictionRunsMillis());
	}
}

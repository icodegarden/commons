package io.github.icodegarden.commons.springboot.build;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.lettuce.LettuceRedisClientRedisExecutor;
import io.github.icodegarden.commons.redis.lettuce.LettuceRedisClusterClientRedisExecutor;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Cluster;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Pool;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class LettuceRedisExecutorBuilder {

	public static RedisExecutor create(CommonsRedisProperties redisProperties) {
		Cluster cluster = redisProperties.getCluster();
		if (cluster != null) {
			log.info("create RedisExecutor by Cluster");

			Set<RedisURI> redisURIs = cluster.getNodes().stream().map(node -> {
				Builder builder = RedisURI.builder()//
						.withHost(node.getHost())//
						.withPort(node.getPort())//
						.withTimeout(Duration.ofMillis(cluster.getSoTimeout()))//
						.withPassword(cluster.getPassword() != null ? cluster.getPassword().toCharArray() : null)//
//							.withDatabase(0)//
						.withClientName(cluster.getClientName()).withSsl(cluster.isSsl());//

				if (cluster.getUser() != null) {
					builder.withAuthentication(cluster.getUser(), cluster.getPassword());
				}
				return builder.build();
			}).collect(Collectors.toSet());

			DefaultClientResources clientResources = DefaultClientResources.builder()
//						.ioThreadPoolSize()//使用默认值
//						.computationThreadPoolSize()//使用默认值
//						.reconnectDelay(reconnectDelay)//使用默认值
					.build();
			RedisClusterClient client = RedisClusterClient.create(clientResources, redisURIs);
			return new LettuceRedisClusterClientRedisExecutor(client);
		}

		Pool pool = redisProperties.getPool();
		if (pool != null) {
			log.info("create RedisExecutor by Pool");

			Builder builder = RedisURI.builder()//
					.withHost(pool.getHost())//
					.withPort(pool.getPort())//
					.withTimeout(Duration.ofMillis(pool.getSoTimeout()))//
					.withPassword(pool.getPassword() != null ? pool.getPassword().toCharArray() : null)//
//						.withDatabase(0)//
					.withClientName(pool.getClientName()).withSsl(pool.isSsl());//

			if (pool.getUser() != null) {
				builder.withAuthentication(pool.getUser(), pool.getPassword());
			}
			RedisURI redisURI = builder.build();

			DefaultClientResources clientResources = DefaultClientResources.builder()
//						.ioThreadPoolSize()//使用默认值
//						.computationThreadPoolSize()//使用默认值
//						.reconnectDelay(reconnectDelay)//使用默认值
					.build();
			RedisClient client = RedisClient.create(clientResources, redisURI);
			return new LettuceRedisClientRedisExecutor(client);
		}

		return null;
	}
}

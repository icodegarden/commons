package io.github.icodegarden.commons.springboot.build;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.lettuce.LettuceRedisClientRedisExecutor;
import io.github.icodegarden.commons.redis.lettuce.LettuceRedisClusterClientRedisExecutor;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Cluster;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Cluster.ClusterLettuce;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.LettuceCommon;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Pool;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Pool.PoolLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.Delay;
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
			ClusterLettuce lettuce = cluster.getLettuce();

			Set<RedisURI> redisURIs = lettuce.getNodes().stream().map(node -> {
				Builder builder = RedisURI.builder()//
						.withHost(node.getHost())//
						.withPort(node.getPort())//
						.withTimeout(Duration.ofMillis(lettuce.getSoTimeout()))//
						.withPassword(lettuce.getPassword() != null ? lettuce.getPassword().toCharArray() : null)//
//							.withDatabase(0)//
						.withClientName(lettuce.getClientName()).withSsl(lettuce.isSsl());//

				if (lettuce.getUser() != null) {
					builder.withAuthentication(lettuce.getUser(), lettuce.getPassword());
				}
				return builder.build();
			}).collect(Collectors.toSet());

			ClientResources clientResources = buildClientResources(lettuce);

			RedisClusterClient client = RedisClusterClient.create(clientResources, redisURIs);
			return new LettuceRedisClusterClientRedisExecutor(client);
		}

		Pool pool = redisProperties.getPool();
		if (pool != null) {
			log.info("create RedisExecutor by Pool");
			PoolLettuce lettuce = pool.getLettuce();

			Builder builder = RedisURI.builder()//
					.withHost(lettuce.getHost())//
					.withPort(lettuce.getPort())//
					.withTimeout(Duration.ofMillis(lettuce.getSoTimeout()))//
					.withPassword(lettuce.getPassword() != null ? lettuce.getPassword().toCharArray() : null)//
					.withDatabase(lettuce.getDatabase())//
					.withClientName(lettuce.getClientName()).withSsl(lettuce.isSsl());//

			if (lettuce.getUser() != null) {
				builder.withAuthentication(lettuce.getUser(), lettuce.getPassword());
			}
			RedisURI redisURI = builder.build();

			ClientResources clientResources = buildClientResources(lettuce);

			RedisClient client = RedisClient.create(clientResources, redisURI);
			return new LettuceRedisClientRedisExecutor(client);
		}

		return null;
	}

	private static ClientResources buildClientResources(LettuceCommon common) {
		DefaultClientResources.Builder builder = DefaultClientResources.builder();
		if (common != null) {
			if (common.getIoThreadPoolSize() != null) {
				builder.ioThreadPoolSize(common.getIoThreadPoolSize());
			}
			if (common.getComputationThreadPoolSize() != null) {
				builder.computationThreadPoolSize(common.getComputationThreadPoolSize());
			}
			if (common.getReconnectDelayMs() != null) {
				builder.reconnectDelay(Delay.constant(Duration.ofMillis(common.getReconnectDelayMs())));
			}
		}
		return builder.build();
	}
}

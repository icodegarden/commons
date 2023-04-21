package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisClusterRedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisPoolRedisExecutor;
import io.github.icodegarden.commons.redis.spring.RedisTemplateRedisExecutor;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Cluster;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties.Pool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CommonsRedisAutoConfiguration {

	/**
	 * 为了让创建RedisExecutor bean时能识别是否有RedisTemplate设立这个类，否则不引RedisTemplate包时报类找不到的
	 * @author Fangfang.Xu
	 *
	 */
	@ConditionalOnClass(RedisTemplate.class)
	@Configuration
	@Getter
	protected static class RedisTemplateWrap {
		@Autowired(required = false)
		private RedisTemplate redisTemplate;
	}

	@Autowired(required = false)
	RedisTemplateWrap redisTemplateWrap;

	@ConditionalOnProperty(value = "commons.redis.executor.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnMissingBean
	@Bean
	public RedisExecutor redisExecutor(CommonsRedisProperties redisProperties) {
		log.info("commons init bean of RedisExecutor");

		Cluster cluster = redisProperties.getCluster();
		if (cluster != null) {
			log.info("create RedisExecutor by Cluster");
			Set<HostAndPort> clusterNodes = cluster.getNodes().stream()
					.map(node -> new HostAndPort(node.getHost(), node.getPort())).collect(Collectors.toSet());

			JedisCluster jc = new JedisCluster(clusterNodes, cluster.getConnectionTimeout(), cluster.getSoTimeout(),
					cluster.getMaxAttempts(), cluster.getUser(), cluster.getPassword(), cluster.getClientName(),
					cluster, cluster.isSsl());

			return new JedisClusterRedisExecutor(jc);
		}

		Pool pool = redisProperties.getPool();
		if (pool != null) {
			log.info("create RedisExecutor by Pool");
			JedisPool jp = new JedisPool(pool, pool.getHost(), pool.getPort(), pool.getConnectionTimeout(),
					pool.getSoTimeout(), pool.getUser(), pool.getPassword(), pool.getDatabase(), pool.getClientName(),
					pool.isSsl());
			return new JedisPoolRedisExecutor(jp);
		}

		if (redisTemplateWrap != null && redisTemplateWrap.getRedisTemplate() != null) {
			log.info("create RedisExecutor by RedisTemplate");
			return new RedisTemplateRedisExecutor(redisTemplateWrap.getRedisTemplate());
		}

		throw new IllegalStateException("CommonsRedisProperties config error, cluster or pool must not null");
	}
}

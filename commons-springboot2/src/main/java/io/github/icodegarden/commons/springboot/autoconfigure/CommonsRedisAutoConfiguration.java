package io.github.icodegarden.commons.springboot.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.spring.RedisTemplateRedisExecutor;
import io.github.icodegarden.commons.springboot.build.JedisRedisExecutorBuilder;
import io.github.icodegarden.commons.springboot.build.LettuceRedisExecutorBuilder;
import io.github.icodegarden.commons.springboot.properties.CommonsRedisProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
	 * 
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

		/**
		 * 必须依赖项
		 */
		boolean lettucePresent = ClassUtils.isPresent("io.lettuce.core.cluster.RedisClusterClient", null);
		boolean jedisPresent = ClassUtils.isPresent("redis.clients.jedis.JedisCluster", null);
		Assert.isTrue(lettucePresent || jedisPresent, "lettuce or jedis dependency must present");

		if (lettucePresent) {
			RedisExecutor redisExecutor = LettuceRedisExecutorBuilder.create(redisProperties);
			if (redisExecutor != null) {
				return redisExecutor;
			}
		} else if (jedisPresent) {
			RedisExecutor redisExecutor = JedisRedisExecutorBuilder.create(redisProperties);
			if (redisExecutor != null) {
				return redisExecutor;
			}
		}

		if (redisTemplateWrap != null && redisTemplateWrap.getRedisTemplate() != null) {
			log.info("create RedisExecutor by RedisTemplate");
			return new RedisTemplateRedisExecutor(redisTemplateWrap.getRedisTemplate());
		}

		throw new IllegalStateException("CommonsRedisProperties config error, cluster or pool must not null");
	}

}

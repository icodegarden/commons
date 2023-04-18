package io.github.icodegarden.commons.redis;

import java.io.Serializable;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class TemplateRedisExecutorTests extends RedisExecutorTests {

	@Override
	protected RedisExecutor newInstance() {
		return new TemplateRedisExecutor(newRedisTemplate());
	}

	public static RedisTemplate<String, Serializable> newRedisTemplate() {
		RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<String, Serializable>();
//		redisTemplate.setConnectionFactory(jedisConnectionFactory());//二选一
		redisTemplate.setConnectionFactory(lettuceConnectionFactory());//二选一
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	static RedisConnectionFactory jedisConnectionFactory() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig)
				.build();

		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName("172.22.122.23");
//		redisConfig.setPassword(RedisPassword.of("8q9P&ZF5SQ@Fv49x"));
		redisConfig.setPort(6399);

		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisConfig, clientConfig);
		jedisConnectionFactory.afterPropertiesSet();// 需要调用一下，不然pool不会生效，内部总是创建一个新链接
		return jedisConnectionFactory;
	}
	
	static RedisConnectionFactory lettuceConnectionFactory() {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName("172.22.122.23");
//		redisConfig.setPassword(RedisPassword.of("8q9P&ZF5SQ@Fv49x"));
		redisConfig.setPort(6399);

		LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfig);
		lettuceConnectionFactory.afterPropertiesSet();// 需要调用一下，不然pool不会生效，内部总是创建一个新链接
		return lettuceConnectionFactory;
	}
}

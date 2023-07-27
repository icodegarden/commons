package io.github.icodegarden.commons.redis.filter;

import org.junit.jupiter.api.BeforeEach;

import io.github.icodegarden.commons.lang.filter.AbstractBloomFilter;
import io.github.icodegarden.commons.redis.JedisPoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisPoolRedisExecutor;
import io.github.icodegarden.commons.test.filter.AbstractBloomFilterTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class RedisBloomFilterTests extends AbstractBloomFilterTests {

	@BeforeEach
	void init() {
		RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
		redisExecutor.del("RedisBloomFilterTests".getBytes());
	}

	@Override
	protected AbstractBloomFilter newBloomFilter(int count) {
		return new RedisBloomFilter("RedisBloomFilterTests",
				new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool()));
	}

}

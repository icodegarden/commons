package io.github.icodegarden.commons.redis;

import java.time.Duration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class LettuceRedisClientExecutorTests extends RedisExecutorTests {

	@Override
	protected RedisExecutor newInstance() {
		return new LettuceRedisClientExecutor(newRedisClient());
	}

	public static RedisClient newRedisClient() {
		RedisURI redisURI = RedisURI.builder()//
				.withHost("172.22.122.23")//
				.withPort(6399)//
//				.withPassword("".toCharArray())//
				.withDatabase(0)//
				.withSsl(false)//
				.withTimeout(Duration.ofMillis(3000))//
				.build();

		RedisClient client = RedisClient.create(redisURI);
		return client;
	}
}

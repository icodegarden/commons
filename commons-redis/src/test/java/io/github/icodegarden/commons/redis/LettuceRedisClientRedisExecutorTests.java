package io.github.icodegarden.commons.redis;

import java.time.Duration;

import io.github.icodegarden.commons.redis.lettuce.LettuceRedisClientRedisExecutor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class LettuceRedisClientRedisExecutorTests extends RedisExecutorTests {

	@Override
	protected RedisExecutor newInstance() {
		return new LettuceRedisClientRedisExecutor(newRedisClient()).setShutdownClientOnClose(true);
	}

	public static RedisClient newRedisClient() {
		RedisURI redisURI = RedisURI.builder()//
				.withHost("192.168.80.130")//
				.withPort(6379)//
//				.withPassword("".toCharArray())//
				.withDatabase(0)//
				.withSsl(false)//
				.withTimeout(Duration.ofMillis(3000))//
				.build();

		RedisClient client = RedisClient.create(redisURI);
		return client;
	}

}

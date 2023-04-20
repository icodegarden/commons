package io.github.icodegarden.commons.redis;

import java.time.Duration;
import java.util.Arrays;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class LettuceRedisClusterClientExecutorTests extends RedisExecutorTests {

	@Override
	protected RedisExecutor newInstance() {
		return new LettuceRedisClusterClientExecutor(newRedisClusterClient());
	}

	public static RedisClusterClient newRedisClusterClient() {
		RedisURI redisURI = RedisURI.builder()//
				.withHost("172.22.122.23")//
				.withPort(6399)//
//				.withPassword("".toCharArray())//
				.withDatabase(0)//
				.withSsl(false)//
				.withTimeout(Duration.ofMillis(3000))//
				.build();

		RedisClusterClient client = RedisClusterClient.create(Arrays.asList(redisURI));
		return client;
	}
}

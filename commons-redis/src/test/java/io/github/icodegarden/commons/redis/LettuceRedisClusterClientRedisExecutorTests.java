package io.github.icodegarden.commons.redis;

import java.time.Duration;
import java.util.Arrays;

import io.github.icodegarden.commons.redis.lettuce.LettuceRedisClusterClientRedisExecutor;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class LettuceRedisClusterClientRedisExecutorTests extends RedisExecutorTests {

	@Override
	protected RedisExecutor newInstance() {
		return new LettuceRedisClusterClientRedisExecutor(newRedisClusterClient()).setShutdownClientOnClose(true);
	}

	public static RedisClusterClient newRedisClusterClient() {
		RedisURI redisURI = RedisURI.builder()//
				.withHost("192.168.184.129")//
				.withPort(6379)//
//				.withPassword("".toCharArray())//
				.withDatabase(0)//
				.withSsl(false)//
				.withTimeout(Duration.ofMillis(3000))//
				.build();

		RedisClusterClient client = RedisClusterClient.create(Arrays.asList(redisURI));
		return client;
	}
}

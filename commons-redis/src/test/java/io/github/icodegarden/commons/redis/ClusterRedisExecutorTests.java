package io.github.icodegarden.commons.redis;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class ClusterRedisExecutorTests extends RedisExecutorTests {

	@Override
	protected RedisExecutor newInstance() {
		return new ClusterRedisExecutor(newJedisCluster());
	}

	public static JedisCluster newJedisCluster() {
		Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>() {
			{
				add(new HostAndPort("172.22.122.23", 6399));
			}
		};
		int connectionTimeout = 3000;
		int soTimeout = 3000;
		int maxAttempts = 1;// 尝试请求redis server的次数，必须>=1，仅当内部出现JedisConnectionException时有效
		String password = null;
//		GenericObjectPoolConfig poolConfig = new ConnectionPoolConfig();
		GenericObjectPoolConfig poolConfig = new JedisPoolConfig();
		JedisCluster jc = new JedisCluster(jedisClusterNode, connectionTimeout, soTimeout, maxAttempts, password,
				poolConfig);
		return jc;
	}
}

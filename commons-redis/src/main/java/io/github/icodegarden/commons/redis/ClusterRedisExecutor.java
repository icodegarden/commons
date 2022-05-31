package io.github.icodegarden.commons.redis;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClusterRedisExecutor implements RedisExecutor {
	private static final Logger log = LoggerFactory.getLogger(ClusterRedisExecutor.class);

	private JedisCluster jc;

	public ClusterRedisExecutor(Set<HostAndPort> clusterNodes, int connectionTimeout, int soTimeout, int maxAttempts,
			String password, GenericObjectPoolConfig poolConfig) {
		this(new JedisCluster(clusterNodes, connectionTimeout, soTimeout, maxAttempts, password, poolConfig));
	}

	public ClusterRedisExecutor(JedisCluster jc) {
		this.jc = jc;
	}

	@Override
	public byte[] get(byte[] key) {
		return jc.get(key);
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		return jc.mget(keys);
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return jc.set(key, value);
	}

	@Override
	public String setex(byte[] key, long seconds, byte[] value) {
		return jc.setex(key, seconds, value);
	}

	@Override
	public Long setnx(byte[] key, byte[] value) {
		return jc.setnx(key, value);
	}

	@Override
	public Long expire(byte[] key, long seconds) {
		return jc.expire(key, (int) seconds);
	}

	@Override
	public Object eval(byte[] script, int keyCount, byte[]... params) {
		return jc.eval(script, keyCount, params);
	}

	@Override
	public Long del(byte[] key) {
		return jc.del(key);
	}

	@Override
	public Long del(byte[]... keys) {
		return jc.del(keys);
	}

	@Override
	public Long incr(byte[] key) {
		return jc.incr(key);
	}

	@Override
	public Long incrBy(byte[] key, long value) {
		return jc.incrBy(key, value);
	}

	@Override
	public Double incrByFloat(byte[] key, double value) {
		return jc.incrByFloat(key, value);
	}

	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return jc.hincrBy(key, field, value);
	}

	@Override
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return jc.hincrByFloat(key, field, value);
	}
	
	@Override
	public Long decr(byte[] key) {
		return jc.decr(key);
	}
	
	@Override
	public Long decrBy(byte[] key, long value) {
		return jc.decrBy(key, value);
	}

	@Override
	public void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver) {
		unsubscribeReceiver.accept(new Unsubscribe() {
			@Override
			public boolean isSubscribed() {
				return jedisPubSub.isSubscribed();
			}

			@Override
			public void unsubscribe(byte[]... channels) {
				jedisPubSub.unsubscribe(channels);
			}

			@Override
			public void unsubscribe() {
				jedisPubSub.unsubscribe();
				if (log.isInfoEnabled()) {
					log.info(this.getClass().getSimpleName() + " unsubscribe channel:{}",
							new String(channel, Charset.forName("utf-8")));
				}
			}
		});
		jc.subscribe(jedisPubSub, channel);
	}

	@Override
	public void publish(byte[] channel, byte[] message) {
		jc.publish(channel, message);
	}
}

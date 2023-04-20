package io.github.icodegarden.commons.redis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import redis.clients.jedis.BinaryJedisPubSub;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LettuceRedisClusterClientExecutor implements RedisExecutor {
	private static final Logger log = LoggerFactory.getLogger(LettuceRedisClusterClientExecutor.class);

	private RedisClusterClient  client;
	private StatefulRedisClusterConnection<byte[], byte[]> connection;
	private RedisAdvancedClusterCommands<byte[], byte[]> syncRedisCommands;

	public LettuceRedisClusterClientExecutor(RedisClusterClient  client) {
		this.client = client;
		this.connection = client.connect(new ByteArrayCodec());
		this.syncRedisCommands = this.connection.sync();
	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {
		List<byte[]> list = syncRedisCommands.keys(pattern);
		return new HashSet<byte[]>(list);
	}

	@Override
	public byte[] get(byte[] key) {
		return syncRedisCommands.get(key);
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		List<KeyValue<byte[], byte[]>> list = syncRedisCommands.mget(keys);
		return list.stream().map(i -> i.getValue()).collect(Collectors.toList());
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return syncRedisCommands.set(key, value);
	}

	@Override
	public String setex(byte[] key, long seconds, byte[] value) {
		return syncRedisCommands.setex(key, seconds, value);
	}

	@Override
	public Long setnx(byte[] key, byte[] value) {
		return syncRedisCommands.setnx(key, value) ? 1L : 0L;
	}

	@Override
	public Long expire(byte[] key, long seconds) {
		return syncRedisCommands.expire(key, seconds) ? 1L : 0L;
	}

	@Override
	public Object eval(byte[] script, int keyCount, byte[]... params) {
		/**
		 * 每组有几个参数
		 */
		int groupSize = params.length / keyCount;// keyCount即共有几组

		List<byte[]> keys = new LinkedList<byte[]>();
		List<byte[]> values = new LinkedList<byte[]>();
		for (int g = 0; g < keyCount; g++) {
			/**
			 * 取每组
			 */
			for (int i = 0; i < groupSize; i++) {
				byte[] param = params[groupSize * g + i];
				if (i == 0) {
					keys.add(param);/* 每组的第一个是key */
				} else {
					values.add(param);
				}
			}
		}

		byte[][] keysArr = keys.toArray(new byte[keys.size()][]);
		byte[][] valuesArr = values.toArray(new byte[values.size()][]);

		return syncRedisCommands.eval(script, ScriptOutputType.VALUE, keysArr, valuesArr);
	}

	@Override
	public Long del(byte[] key) {
		return syncRedisCommands.del(key);
	}

	@Override
	public Long del(byte[]... keys) {
		return syncRedisCommands.del(keys);
	}

	@Override
	public Long incr(byte[] key) {
		return syncRedisCommands.incr(key);
	}

	@Override
	public Long incrBy(byte[] key, long value) {
		return syncRedisCommands.incrby(key, value);
	}

	@Override
	public Double incrByFloat(byte[] key, double value) {
		return syncRedisCommands.incrbyfloat(key, value);
	}

	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return syncRedisCommands.hincrby(key, field, value);
	}

	@Override
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return syncRedisCommands.hincrbyfloat(key, field, value);
	}

	@Override
	public Long decr(byte[] key) {
		return syncRedisCommands.decr(key);
	}

	@Override
	public Long decrBy(byte[] key, long value) {
		return syncRedisCommands.decrby(key, value);
	}

	@Override
	public void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver) {
		// TODO
	}

	@Override
	public void publish(byte[] channel, byte[] message) {
		syncRedisCommands.publish(channel, message);
	}
}

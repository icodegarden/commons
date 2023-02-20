package io.github.icodegarden.commons.redis;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import redis.clients.jedis.BinaryJedisPubSub;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RedisExecutor {
	
	Set<byte[]> keys(byte[] pattern);

	byte[] get(final byte[] key);

	List<byte[]> mget(final byte[]... keys);

	String set(final byte[] key, final byte[] value);

	String setex(final byte[] key, final long seconds, final byte[] value);

	Long setnx(final byte[] key, final byte[] value);

	Long expire(final byte[] key, final long seconds);

	Object eval(final byte[] script, final int keyCount, final byte[]... params);

	Long del(final byte[] key);

	Long del(final byte[]... keys);
	
	Long incr(byte[] key);

	Long incrBy(byte[] key, long value);

	Double incrByFloat(byte[] key, double value);

	Long hincrBy(byte[] key, byte[] field, long value);

	Double hincrByFloat(byte[] key, byte[] field, double value);
	
	Long decr(byte[] key);

	Long decrBy(byte[] key, long value);

	/**
	 * 该动作是一直阻塞的，直到unsubscribe
	 * 
	 * @param channel
	 * @param jedisPubSub
	 * @param unsubscribeReceiver
	 */
	void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver);

	void publish(byte[] channel, byte[] message);

}

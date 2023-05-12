package io.github.icodegarden.commons.redis;

import java.io.IOException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RedisExecutor
		extends KeyBinaryCommands, StringBinaryCommands,HashBinaryCommands, ScriptingKeyBinaryCommands {

	void close() throws IOException;

//	Set<byte[]> keys(byte[] pattern);
//
//	byte[] get(final byte[] key);
//
//	List<byte[]> mget(final byte[]... keys);
//
//	String set(final byte[] key, final byte[] value);
//
//	String setex(final byte[] key, final long seconds, final byte[] value);
//
//	Long setnx(final byte[] key, final byte[] value);
//
//	Long expire(final byte[] key, final long seconds);
//
//	Long del(final byte[] key);
//
//	Long del(final byte[]... keys);
//
//	Long incr(byte[] key);
//
//	Long incrBy(byte[] key, long value);
//
//	Double incrByFloat(byte[] key, double value);
//
//	Long hincrBy(byte[] key, byte[] field, long value);
//
//	Double hincrByFloat(byte[] key, byte[] field, double value);
//
//	Long decr(byte[] key);
//
//	Long decrBy(byte[] key, long value);

	/**
	 * 该动作是一直阻塞的，直到unsubscribe
	 * 
	 * @param channel
	 * @param jedisPubSub
	 * @param unsubscribeReceiver
	 */
//	void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver);

	/**
	 * 使用Jedis时，该动作是一直阻塞的，直到unsubscribe，因此会开启新线程<br>
	 * lettuce由于是异步的，不阻塞
	 * 
	 * @param channel
	 * @param listener 首次必须，后续可以null(jedis则不能)
	 */
	void subscribe(byte[] channel, RedisPubSubListener<byte[], byte[]> listener);

	void unsubscribe(byte[] channel);

	void publish(byte[] channel, byte[] message);

}

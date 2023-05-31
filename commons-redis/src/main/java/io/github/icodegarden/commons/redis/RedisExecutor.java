package io.github.icodegarden.commons.redis;

import java.io.IOException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RedisExecutor
		extends KeyBinaryCommands, StringBinaryCommands, HashBinaryCommands, ListBinaryCommands, SetBinaryCommands,
		SortedSetBinaryCommands, BitmapBinaryCommands, GeoBinaryCommands, ScriptingKeyBinaryCommands {

	void close() throws IOException;

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

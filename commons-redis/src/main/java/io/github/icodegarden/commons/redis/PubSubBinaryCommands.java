package io.github.icodegarden.commons.redis;

import java.util.List;
import java.util.Map;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface PubSubBinaryCommands {

	/**
	 * 该动作是一直阻塞的，直到unsubscribe
	 * 
	 * @param channel
	 * @param jedisPubSub
	 * @param unsubscribeReceiver
	 */
//	void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver);

	void psubscribe(List<byte[]> patterns, RedisPubSubListener<byte[], byte[]> listener);

	void publish(byte[] channel, byte[] message);

	@Nullable
	List<byte[]> pubsubChannels();

	@Nullable
	List<byte[]> pubsubChannels(byte[] pattern);

	long pubsubNumpat();

	Map<byte[], Long> pubsubNumsub(byte[]... channels);

	@Nullable
	List<byte[]> pubsubShardChannels();

	@Nullable
	List<byte[]> pubsubShardChannels(byte[] pattern);

	Map<byte[], Long> pubsubShardNumsub(byte[]... shardchannels);

	void punsubscribe(List<byte[]> patterns);

	void spublish(byte[] shardchannel, byte[] message);

	void ssubscribe(byte[] shardchannel, RedisPubSubListener<byte[], byte[]> listener);

	/**
	 * 使用Jedis时，该动作是一直阻塞的，直到unsubscribe，因此会开启新线程<br>
	 * lettuce由于是异步的，不阻塞
	 * 
	 * @param channel
	 * @param listener 首次必须，后续可以null(jedis则不能)
	 */
	void subscribe(byte[] channel, RedisPubSubListener<byte[], byte[]> listener);

	void sunsubscribe(byte[] shardchannel);

	void unsubscribe(byte[] channel);

}

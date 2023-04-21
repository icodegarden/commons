package io.github.icodegarden.commons.redis.lettuce;

import java.io.IOException;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LettuceRedisClientRedisExecutor extends AbstractLettuceRedisExecutor {

	private RedisClient client;
	private StatefulRedisConnection<byte[], byte[]> connection;
	private RedisCommands<byte[], byte[]> syncRedisCommands;

	public LettuceRedisClientRedisExecutor(RedisClient client) {
		this.client = client;
		this.connection = client.connect(new ByteArrayCodec());
		this.syncRedisCommands = this.connection.sync();

		super.setRedisClusterCommands(syncRedisCommands);
	}

	@Override
	protected StatefulRedisPubSubConnection<byte[], byte[]> connectPubSub() {
		return client.connectPubSub(new ByteArrayCodec());
	}

	@Override
	public void publish(byte[] channel, byte[] message) {
		syncRedisCommands.publish(channel, message);
	}
	
	@Override
	public void close() throws IOException {
		/**
		 * 只关闭connection，client是外面给进来的，不关闭
		 */
		super.close();
		connection.close();	
	}
}

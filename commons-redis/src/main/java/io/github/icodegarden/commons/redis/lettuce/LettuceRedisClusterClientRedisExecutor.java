package io.github.icodegarden.commons.redis.lettuce;

import java.io.IOException;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LettuceRedisClusterClientRedisExecutor extends AbstractLettuceRedisExecutor {

	private RedisClusterClient client;
	private StatefulRedisClusterConnection<byte[], byte[]> connection;
	private RedisAdvancedClusterCommands<byte[], byte[]> syncRedisCommands;
	
	private boolean shutdownClientOnClose = false;

	public LettuceRedisClusterClientRedisExecutor(RedisClusterClient client) {
		this.client = client;
		this.connection = client.connect(new ByteArrayCodec());
		this.syncRedisCommands = this.connection.sync();

		super.setRedisClusterCommands(syncRedisCommands);
	}
	
	public LettuceRedisClusterClientRedisExecutor setShutdownClientOnClose(boolean shutdownClientOnClose) {
		this.shutdownClientOnClose = shutdownClientOnClose;
		return this;
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
		super.close();
		connection.close();
		
		/**
		 * client是外面给进来的，默认不关闭
		 */
		if(shutdownClientOnClose) {
			client.close();
			client.shutdown();	
		}
	}
}

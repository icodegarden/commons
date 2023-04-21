package io.github.icodegarden.commons.redis.spring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.ValueEncoding;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;

import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.RedisPubSubListener;
import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.MigrateParams;
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.util.EvalUtils;
import io.github.icodegarden.commons.redis.util.RedisTemplateUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SuppressWarnings("all")
public class RedisTemplateRedisExecutor implements RedisExecutor {

	private RedisTemplate redisTemplate;

	private Map<byte[], RedisConnection> subMap = new ConcurrentHashMap<byte[], RedisConnection>();

	public RedisTemplateRedisExecutor(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void close() throws IOException {
		RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {
		return (Set<byte[]>) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keys(pattern);
		});
	}

	@Override
	public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
		return (boolean) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().copy(srcKey, dstKey, replace);
		});
	}

	@Override
	public long del(byte[] key) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().del(key);
		});
	}

	@Override
	public long del(byte[]... keys) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().del(keys);
		});
	}

	@Override
	public byte[] dump(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().dump(key);
		});
	}

	@Override
	public boolean exists(byte[] key) {
		return (boolean) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().exists(key);
		});
	}

	@Override
	public long exists(byte[]... keys) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().exists(keys);
		});
	}

	@Override
	public long expire(byte[] key, long seconds) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().expire(key, seconds) ? 1 : 0;
		});
	}

	@Override
	public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().expire(key, seconds);
		});
	}

	@Override
	public long expireAt(byte[] key, long unixTime) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().expireAt(key, unixTime);
		});
	}

	@Override
	public long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().expireAt(key, unixTime);
		});
	}

	@Override
	public long expireTime(byte[] key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String migrate(String host, int port, byte[] key, int timeout) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			RedisNode redisNode = new RedisNode(host, port);
			connection.migrate(key, redisNode, 0, null, timeout);
			return null;
		});
	}

	@Override
	public String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
		for (byte[] key : keys) {
			migrate(host, port, key, timeout);
		}
		return null;
	}

	@Override
	public byte[] objectEncoding(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			ValueEncoding valueEncoding = connection.keyCommands().encodingOf(key);

			String raw = valueEncoding.raw();
			if (raw != null) {
				return raw.getBytes(StandardCharsets.UTF_8);
			}
			return null;
		});
	}

	@Override
	public Long objectFreq(byte[] key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long objectIdletime(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			Duration duration = connection.keyCommands().idletime(key);
			if (duration != null) {
				return duration.toMillis();
			}
			return null;
		});
	}

	@Override
	public Long objectRefcount(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().refcount(key);
		});
	}

	@Override
	public long persist(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().persist(key) ? 1 : 0;
		});
	}

	@Override
	public long pexpire(byte[] key, long milliseconds) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpire(key, milliseconds) ? 1 : 0;
		});
	}

	@Override
	public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpire(key, milliseconds) ? 1 : 0;
		});
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpireAt(key, millisecondsTimestamp) ? 1 : 0;
		});
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpireAt(key, millisecondsTimestamp) ? 1 : 0;
		});
	}

	@Override
	public long pexpireTime(byte[] key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long pttl(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pTtl(key);
		});
	}

	@Override
	public byte[] randomBinaryKey() {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().randomKey();
		});
	}

	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			connection.keyCommands().rename(oldkey, newkey);
			return null;
		});
	}

	@Override
	public long renamenx(byte[] oldkey, byte[] newkey) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().renameNX(oldkey, newkey) ? 1 : 0;
		});
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			connection.keyCommands().restore(key, ttl, serializedValue);
			return null;
		});
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			connection.keyCommands().restore(key, ttl, serializedValue, params.isReplace());
			return null;
		});
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params, byte[] type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<byte[]> sort(byte[] key) {
		return (List<byte[]>) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().sort(key, new DefaultSortParameters());
		});
	}

	@Override
	public List<byte[]> sort(byte[] key, SortArgs params) {
		return (List<byte[]>) redisTemplate.execute((RedisCallback) connection -> {
			SortParameters sortParameters = RedisTemplateUtils.convertSortParameters(params);
			return connection.keyCommands().sort(key, sortParameters);
		});
	}

	@Override
	public long sort(byte[] key, byte[] dstkey) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().sort(key, new DefaultSortParameters(), dstkey);
		});
	}

	@Override
	public long sort(byte[] key, SortArgs params, byte[] dstkey) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			SortParameters sortParameters = RedisTemplateUtils.convertSortParameters(params);
			return connection.keyCommands().sort(key, sortParameters, dstkey);
		});
	}

	@Override
	public List<byte[]> sortReadonly(byte[] key, SortArgs params) {
		return (List<byte[]>) redisTemplate.execute((RedisCallback) connection -> {
			SortParameters sortParameters = RedisTemplateUtils.convertSortParameters(params);
			return connection.keyCommands().sort(key, sortParameters);
		});
	}

	@Override
	public long touch(byte[] key) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().touch(key);
		});
	}

	@Override
	public long touch(byte[]... keys) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().touch(keys);
		});
	}

	@Override
	public long ttl(byte[] key) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().ttl(key);
		});
	}

	@Override
	public String type(byte[] key) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().type(key).code();
		});
	}

	@Override
	public long unlink(byte[] key) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().unlink(key);
		});
	}

	@Override
	public long unlink(byte[]... keys) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().unlink(keys);
		});
	}

	@Override
	public Long memoryUsage(byte[] key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long memoryUsage(byte[] key, int samples) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Object> eval(byte[] script) {
		return (List<Object>) redisTemplate.execute((RedisCallback) connection -> {
			Object obj = connection.eval(script, ReturnType.MULTI, 0, new byte[0]);
			return EvalUtils.ofMultiReturnType(obj);
		});
	}

	@Override
	public List<Object> eval(byte[] script, int keyCount, byte[]... params) {
		return (List<Object>) redisTemplate.execute((RedisCallback) connection -> {
			Object obj = connection.eval(script, ReturnType.MULTI, keyCount, params);
			return EvalUtils.ofMultiReturnType(obj);
		});
	}

	@Override
	public List<Object> eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		List<byte[]> keysAndArgs = CollectionUtils.mergeByKeyGroup(keys, args);

		return (List<Object>) redisTemplate.execute((RedisCallback) connection -> {
			Object obj = connection.eval(script, ReturnType.MULTI, keys.size(),
					keysAndArgs.toArray(new byte[keysAndArgs.size()][]));
			return EvalUtils.ofMultiReturnType(obj);
		});
	}

	@Override
	public List<Object> evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
		/**
		 * 语句自己控制只读
		 */
		return eval(script, keys, args);
	}

//	@Override
//	public void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver) {
//		redisTemplate.execute((RedisCallback) connection -> {
//			unsubscribeReceiver.accept(new Unsubscribe() {
//				@Override
//				public boolean isSubscribed() {
//					return connection.isSubscribed();
//				}
//
//				@Override
//				public void unsubscribe(byte[]... channels) {
//					Subscription subscription = connection.getSubscription();
//					if (subscription != null) {
//						subscription.unsubscribe(channels);
//					}
//				}
//
//				@Override
//				public void unsubscribe() {
//					Subscription subscription = connection.getSubscription();
//					if (subscription != null) {
//						subscription.unsubscribe();
//					}
//					if (log.isInfoEnabled()) {
//						log.info(this.getClass().getSimpleName() + " unsubscribe channel:{}",
//								new String(channel, Charset.forName("utf-8")));
//					}
//				}
//			});
//
//			connection.subscribe((message, pattern) -> {
//				jedisPubSub.onMessage(message.getChannel(), message.getBody());
//			}, channel);
//			return null;
//		});
//	}

	@Override
	public void subscribe(byte[] channel, RedisPubSubListener<byte[], byte[]> listener) {
		Thread thread = new Thread("Jedis-Sub-" + new String(channel, StandardCharsets.UTF_8)) {
			@Override
			public void run() {
				redisTemplate.execute((RedisCallback) connection -> {
					subMap.put(channel, connection);

					connection.subscribe((message, pattern) -> {
						listener.message(message.getChannel(), message.getBody());
					}, channel);
					return null;
				});
			}
		};

		RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
		if (connectionFactory instanceof JedisConnectionFactory) {
			thread.start();
		} else {
			thread.run();
		}
	}

	@Override
	public void unsubscribe(byte[] channel) {
//		redisTemplate.execute((RedisCallback) connection -> {
//			connection.getSubscription().unsubscribe(channel);
//			return null;
//		});

		/**
		 * 如果使用以上代码，则对应的connection可能不是sub时的，getSubscription将会是null
		 */
		RedisConnection connection = subMap.get(channel);
		if (connection != null) {
			connection.getSubscription().unsubscribe(channel);
		}
	}

	@Override
	public void publish(byte[] channel, byte[] message) {
		redisTemplate.execute((RedisCallback) connection -> {
			connection.publish(channel, message);
			return null;
		});
	}
}

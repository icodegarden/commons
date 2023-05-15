package io.github.icodegarden.commons.redis.spring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.ValueEncoding;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ScanOptions.ScanOptionsBuilder;
import org.springframework.data.redis.core.types.Expiration;

import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.RedisPubSubListener;
import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.LCSMatchResult;
import io.github.icodegarden.commons.redis.args.LCSParams;
import io.github.icodegarden.commons.redis.args.MapScanCursor;
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
			return connection.keyCommands().expire(key, seconds) ? 1L : 0L;
		});
	}

	@Override
	public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().expire(key, seconds) ? 1L : 0L;
		});
	}

	@Override
	public long expireAt(byte[] key, long unixTime) {
		return (long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().expireAt(key, unixTime) ? 1L : 0L;
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
			return connection.keyCommands().persist(key) ? 1L : 0L;
		});
	}

	@Override
	public long pexpire(byte[] key, long milliseconds) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpire(key, milliseconds) ? 1L : 0L;
		});
	}

	@Override
	public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpire(key, milliseconds) ? 1L : 0L;
		});
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpireAt(key, millisecondsTimestamp) ? 1L : 0L;
		});
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.keyCommands().pExpireAt(key, millisecondsTimestamp) ? 1L : 0L;
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
			return connection.keyCommands().renameNX(oldkey, newkey) ? 1L : 0L;
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
		return scan(cursor, null);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params) {
		return scan(cursor, params, null);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params, byte[] type) {
		return (KeyScanCursor<byte[]>) redisTemplate.execute((RedisCallback) connection -> {

			ScanOptionsBuilder builder = ScanOptions.scanOptions();
			if (params != null) {
				params.match(params.getMatch());
				if (params.getCount() != null) {
					builder.count(params.getCount());
				}
			}

			if (type != null) {
				builder.type(new String(type, StandardCharsets.UTF_8));
			}
			ScanOptions scanOptions = builder.build();

			try (Cursor<byte[]> scan = connection.scan(scanOptions);) {
				List<byte[]> keys = new LinkedList<byte[]>();

				while (scan.hasNext()) {
					keys.add(scan.next());
				}

				String cursorId = Long.toString(scan.getCursorId());

				return new KeyScanCursor<byte[]>(cursorId, "0".equals(cursorId), keys);
			}
		});
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
	public Long hdel(byte[] key, byte[]... fields) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hDel(key, fields);
		});
	}

	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		return (Boolean) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hExists(key, field);
		});
	}

	@Override
	public byte[] hget(byte[] key, byte[] field) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hGet(key, field);
		});
	}

	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return (Map) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hGetAll(key);
		});
	}

	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hIncrBy(key, field, value);
		});
	}

	@Override
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return (Double) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hIncrBy(key, field, value);
		});
	}

	@Override
	public Set<byte[]> hkeys(byte[] key) {
		return (Set) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hKeys(key);
		});
	}

	@Override
	public Long hlen(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hLen(key);
		});
	}

	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hMGet(key, fields);
		});
	}

	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			connection.hashCommands().hMSet(key, hash);
			return "OK";
		});
	}

	@Override
	public byte[] hrandfield(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hRandField(key);
		});
	}

	@Override
	public List<byte[]> hrandfield(byte[] key, long count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hRandField(key, count);
		});
	}

	@Override
	public Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
		return (Map) redisTemplate.execute((RedisCallback) connection -> {
			List<Entry<byte[], byte[]>> list = connection.hashCommands().hRandFieldWithValues(key, count);
			return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a));
		});
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor) {
		return hscan(key, cursor, null);
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor, ScanArgs params) {
		return (MapScanCursor<byte[], byte[]>) redisTemplate.execute((RedisCallback) connection -> {

			ScanOptionsBuilder builder = ScanOptions.scanOptions();
			if (params != null) {
				params.match(params.getMatch());
				if (params.getCount() != null) {
					builder.count(params.getCount());
				}
			}

			ScanOptions scanOptions = builder.build();

			try (Cursor<Map.Entry<byte[], byte[]>> scan = connection.hScan(key, scanOptions);) {
				Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();

				while (scan.hasNext()) {
					Entry<byte[], byte[]> entry = scan.next();
					map.put(entry.getKey(), entry.getValue());
				}

				String cursorId = Long.toString(scan.getCursorId());
				return new MapScanCursor<byte[], byte[]>(cursorId, "0".equals(cursorId), map);
			}
		});
	}

	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hSet(key, field, value) ? 1L : 0L;
		});
	}

	@Override
	public Long hset(byte[] key, Map<byte[], byte[]> hash) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			connection.hashCommands().hMSet(key, hash);
			return null;
		});
	}

	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hSetNX(key, field, value) ? 1L : 0L;
		});
	}

	@Override
	public Long hstrlen(byte[] key, byte[] field) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hStrLen(key, field);
		});
	}

	@Override
	public List<byte[]> hvals(byte[] key) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.hashCommands().hVals(key);
		});
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
	public Long append(byte[] key, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.append(key, value);
		});
	}

	@Override
	public Long decr(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.decr(key);
		});
	}

	@Override
	public Long decrBy(byte[] key, long value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.decrBy(key, value);
		});
	}

	@Override
	public byte[] get(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.get(key);
		});
	}

	@Override
	public byte[] getDel(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.getDel(key);
		});
	}

	@Override
	public byte[] getEx(byte[] key, GetExArgs params) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			Expiration expiration = RedisTemplateUtils.convertExpiration(params);
			return connection.getEx(key, expiration);
		});
	}

	@Override
	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.getRange(key, startOffset, endOffset);
		});
	}

	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.getSet(key, value);
		});
	}

	@Override
	public Long incr(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.incr(key);
		});
	}

	@Override
	public Long incrBy(byte[] key, long increment) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.incrBy(key, increment);
		});
	}

	@Override
	public Double incrByFloat(byte[] key, double increment) {
		return (Double) redisTemplate.execute((RedisCallback) connection -> {
			return connection.incrBy(key, increment);
		});
	}

	@Override
	public LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		return (List<byte[]>) redisTemplate.execute((RedisCallback) connection -> {
			return connection.mGet(keys);
		});
	}

	@Override
	public String mset(byte[]... keysvalues) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			Map<byte[], byte[]> map = CollectionUtils.keysValuesToMap(keysvalues);
			return connection.mSet(map) ? "OK" : null;
		});
	}

	@Override
	public Long msetnx(byte[]... keysvalues) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			Map<byte[], byte[]> map = CollectionUtils.keysValuesToMap(keysvalues);
			return connection.mSetNX(map) ? 1L : 0L;
		});
	}

	@Override
	public String psetex(byte[] key, long milliseconds, byte[] value) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			return connection.pSetEx(key, milliseconds, value) ? "OK" : null;
		});
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			return connection.set(key, value) ? "OK" : null;
		});
	}

	@Override
	public String setex(byte[] key, long seconds, byte[] value) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setEx(key, seconds, value) ? "OK" : null;
		});
	}

	@Override
	public Long setnx(byte[] key, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setNX(key, value) ? 1L : 0L;
		});
	}

	@Override
	public Long setrange(byte[] key, long offset, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			connection.setRange(key, value, offset);
			return null;// 返回值不兼容redis api
		});
	}

	@Override
	public Long strlen(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.strLen(key);
		});
	}

	@Override
	public byte[] substr(byte[] key, int start, int end) {
		throw new UnsupportedOperationException();
	}

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
			thread.start();// jedis是阻塞的
		} else {
			thread.run();// lettuce不阻塞
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

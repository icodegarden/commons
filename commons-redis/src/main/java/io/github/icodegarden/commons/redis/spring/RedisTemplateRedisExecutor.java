package io.github.icodegarden.commons.redis.spring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.Range.Bound;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisListCommands.Direction;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStringCommands.BitOperation;
import org.springframework.data.redis.connection.RedisZSetCommands.Aggregate;
import org.springframework.data.redis.connection.RedisZSetCommands.Limit;
import org.springframework.data.redis.connection.RedisZSetCommands.Weights;
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

import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.RedisPubSubListener;
import io.github.icodegarden.commons.redis.args.BitCountOption;
import io.github.icodegarden.commons.redis.args.BitFieldArgs;
import io.github.icodegarden.commons.redis.args.BitOP;
import io.github.icodegarden.commons.redis.args.BitPosParams;
import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GeoAddArgs;
import io.github.icodegarden.commons.redis.args.GeoArgs;
import io.github.icodegarden.commons.redis.args.GeoCoordinate;
import io.github.icodegarden.commons.redis.args.GeoRadiusStoreArgs;
import io.github.icodegarden.commons.redis.args.GeoSearch.GeoPredicate;
import io.github.icodegarden.commons.redis.args.GeoSearch.GeoRef;
import io.github.icodegarden.commons.redis.args.GeoUnit;
import io.github.icodegarden.commons.redis.args.GeoValue;
import io.github.icodegarden.commons.redis.args.GeoWithin;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.KeyValue;
import io.github.icodegarden.commons.redis.args.LCSMatchResult;
import io.github.icodegarden.commons.redis.args.LCSParams;
import io.github.icodegarden.commons.redis.args.LPosParams;
import io.github.icodegarden.commons.redis.args.ListDirection;
import io.github.icodegarden.commons.redis.args.ListPosition;
import io.github.icodegarden.commons.redis.args.MapScanCursor;
import io.github.icodegarden.commons.redis.args.MigrateParams;
import io.github.icodegarden.commons.redis.args.Range;
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.ScoredValue;
import io.github.icodegarden.commons.redis.args.ScoredValueScanCursor;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.SortedSetOption;
import io.github.icodegarden.commons.redis.args.ValueScanCursor;
import io.github.icodegarden.commons.redis.args.ZAddArgs;
import io.github.icodegarden.commons.redis.args.ZAggregateArgs;
import io.github.icodegarden.commons.redis.util.EvalUtils;
import io.github.icodegarden.commons.redis.util.JedisUtils;
import io.github.icodegarden.commons.redis.util.RedisTemplateUtils;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;

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

	@Override
	public long bitcount(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.bitCount(key);
		});
	}

	@Override
	public long bitcount(byte[] key, long start, long end) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.bitCount(key, start, end);
		});
	}

	@Override
	public long bitcount(byte[] key, long start, long end, BitCountOption option) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Long> bitfield(byte[] key, BitFieldArgs args) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			BitFieldSubCommands commands = RedisTemplateUtils.convertBitFieldSubCommands(args);
			return connection.bitField(key, commands);
		});
	}

	@Override
	public List<Long> bitfieldReadonly(byte[] key, BitFieldArgs args) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			BitOperation valueOf = org.springframework.data.redis.connection.RedisStringCommands.BitOperation
					.valueOf(op.name());
			return connection.bitOp(valueOf, destKey, srcKeys);
		});
	}

	@Override
	public long bitpos(byte[] key, boolean value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.bitPos(key, value);
		});
	}

	@Override
	public long bitpos(byte[] key, boolean value, BitPosParams params) {
		org.springframework.data.domain.Range<Long> range;
		if (params.getStart() == null) {
			range = org.springframework.data.domain.Range.unbounded();
		} else if (params.getEnd() == null) {
			Bound<Long> bound = org.springframework.data.domain.Range.Bound.inclusive(params.getStart());
			range = org.springframework.data.domain.Range.rightUnbounded(bound);
		} else {
			range = org.springframework.data.domain.Range.closed(params.getStart(), params.getEnd());
		}

		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.bitPos(key, value, range);
		});
	}

	@Override
	public boolean getbit(byte[] key, long offset) {
		return (Boolean) redisTemplate.execute((RedisCallback) connection -> {
			return connection.getBit(key, offset);
		});
	}

	@Override
	public boolean setbit(byte[] key, long offset, boolean value) {
		return (Boolean) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setBit(key, offset, value);
		});
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
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, long timeout) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			Direction f = Direction.valueOf(from.name());
			Direction t = Direction.valueOf(to.name());
			return connection.listCommands().bLMove(srcKey, dstKey, f, t, timeout);
		});
	}

	@Override
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			Direction f = Direction.valueOf(from.name());
			Direction t = Direction.valueOf(to.name());
			return connection.listCommands().bLMove(srcKey, dstKey, f, t, timeout);
		});
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, long count, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(long timeout, byte[]... keys) {
		return (KeyValue) redisTemplate.execute((RedisCallback) connection -> {
			List<byte[]> list = connection.listCommands().bLPop((int) timeout, keys);
			if (org.springframework.util.CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(double timeout, byte[]... keys) {
		return (KeyValue) redisTemplate.execute((RedisCallback) connection -> {
			List<byte[]> list = connection.listCommands().bLPop((int) timeout, keys);
			if (org.springframework.util.CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(long timeout, byte[]... keys) {
		return (KeyValue) redisTemplate.execute((RedisCallback) connection -> {
			List<byte[]> list = connection.listCommands().bRPop((int) timeout, keys);
			if (org.springframework.util.CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(double timeout, byte[]... keys) {
		return (KeyValue) redisTemplate.execute((RedisCallback) connection -> {
			List<byte[]> list = connection.listCommands().bRPop((int) timeout, keys);
			if (org.springframework.util.CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, long timeout) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().bRPopLPush((int) timeout, source, destination);
		});
	}

	@Override
	public byte[] lindex(byte[] key, long index) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lIndex(key, index);
		});
	}

	@Override
	public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisListCommands.Position w = org.springframework.data.redis.connection.RedisListCommands.Position
					.valueOf(where.name());
			return connection.listCommands().lInsert(key, w, pivot, value);
		});
	}

	@Override
	public Long llen(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lLen(key);
		});
	}

	@Override
	public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			Direction f = Direction.valueOf(from.name());
			Direction t = Direction.valueOf(to.name());
			return connection.listCommands().lMove(srcKey, dstKey, f, t);
		});
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, long count, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] lpop(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lPop(key);
		});
	}

	@Override
	public List<byte[]> lpop(byte[] key, long count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lPop(key, count);
		});
	}

	@Override
	public Long lpos(byte[] key, byte[] element) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lPos(key, element);
		});
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, long count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lPos(key, element, null, (int) count);
		});
	}

	@Override
	public Long lpos(byte[] key, byte[] element, LPosParams params) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			List<Long> list = connection.listCommands().lPos(key, element, params.getRank(), null);
			if (org.springframework.util.CollectionUtils.isEmpty(list)) {
				return null;
			}
			return list.get(0);
		});
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			int c = (int) count;
			if (params.getMaxLen() != null && count == 0) {
				c = params.getMaxLen();// 因为redisTemplate不支持maxlen，所以这样算
			}
			return connection.listCommands().lPos(key, element, params.getRank(), c);
		});
	}

	@Override
	public Long lpush(byte[] key, byte[]... values) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lPush(key, values);
		});
	}

	@Override
	public Long lpushx(byte[] key, byte[]... values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<byte[]> lrange(byte[] key, long start, long stop) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lRange(key, start, stop);
		});
	}

	@Override
	public Long lrem(byte[] key, long count, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().lRem(key, count, value);
		});
	}

	@Override
	public String lset(byte[] key, long index, byte[] value) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			connection.listCommands().lSet(key, index, value);
			return "OK";
		});
	}

	@Override
	public String ltrim(byte[] key, long start, long stop) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			connection.listCommands().lTrim(key, start, stop);
			return "OK";
		});
	}

	@Override
	public byte[] rpop(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().rPop(key);
		});
	}

	@Override
	public List<byte[]> rpop(byte[] key, long count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().rPop(key, count);
		});
	}

	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().rPopLPush(srckey, dstkey);
		});
	}

	@Override
	public Long rpush(byte[] key, byte[]... values) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.listCommands().rPush(key, values);
		});
	}

	@Override
	public Long rpushx(byte[] key, byte[]... values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long sadd(byte[] key, byte[]... members) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sAdd(key, members);
		});
	}

	@Override
	public Long scard(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sCard(key);
		});
	}

	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		return (Set) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sDiff(keys);
		});
	}

	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sDiffStore(dstkey, keys);
		});
	}

	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		return (Set) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sInter(keys);
		});
	}

	@Override
	public long sintercard(byte[]... keys) {
		Set<byte[]> set = sinter(keys);// 不支持直接命令
		return set.size();
	}

	@Override
	public long sintercard(int limit, byte[]... keys) {
		Set<byte[]> set = sinter(keys);// 不支持直接命令
		if (limit == 0) {
			return set.size();
		}
		return set.size() > limit ? limit : set.size();
	}

	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sInterStore(dstkey, keys);
		});
	}

	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		return (Boolean) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sIsMember(key, member);
		});
	}

	@Override
	public Set<byte[]> smembers(byte[] key) {
		return (Set) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sMembers(key);
		});
	}

	@Override
	public List<Boolean> smismember(byte[] key, byte[]... members) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sMIsMember(key, members);
		});
	}

	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sMove(srckey, dstkey, member) ? 1L : 0L;
		});
	}

	@Override
	public byte[] spop(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sPop(key);
		});
	}

	@Override
	public Set<byte[]> spop(byte[] key, long count) {
		return (Set) redisTemplate.execute((RedisCallback) connection -> {
			List<byte[]> list = connection.setCommands().sPop(key, count);
			return new HashSet<>(list);
		});
	}

	@Override
	public byte[] srandmember(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sRandMember(key);
		});
	}

	@Override
	public List<byte[]> srandmember(byte[] key, int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sRandMember(key, count);
		});
	}

	@Override
	public Long srem(byte[] key, byte[]... members) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sRem(key, members);
		});
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor) {
		return sscan(key, cursor, null);
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor, ScanArgs params) {
		return (ValueScanCursor<byte[]>) redisTemplate.execute((RedisCallback) connection -> {

			ScanOptionsBuilder builder = ScanOptions.scanOptions();
			if (params != null) {
				params.match(params.getMatch());
				if (params.getCount() != null) {
					builder.count(params.getCount());
				}
			}

			ScanOptions scanOptions = builder.build();

			try (Cursor<byte[]> scan = connection.sScan(key, scanOptions);) {

				List<byte[]> list = new LinkedList<>();

				while (scan.hasNext()) {
					byte[] bs = scan.next();
					list.add(bs);
				}

				String cursorId = Long.toString(scan.getCursorId());
				return new ValueScanCursor<byte[]>(cursorId, "0".equals(cursorId), list);
			}
		});
	}

	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		return (Set) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sUnion(keys);
		});
	}

	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setCommands().sUnionStore(dstkey, keys);
		});
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzmpop(long timeout, SortedSetOption option, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], List<ScoredValue<byte[]>>> bzmpop(long timeout, SortedSetOption option, int count,
			byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzpopmax(double timeout, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzpopmin(double timeout, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long zadd(byte[] key, double score, byte[] member) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zAdd(key, score, member) ? 1L : 0L;
		});
	}

	@Override
	public long zadd(byte[] key, double score, byte[] member, ZAddArgs params) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.ZAddArgs zAddArgs = RedisTemplateUtils
					.convertZAddArgs(params);
			return connection.zSetCommands().zAdd(key, score, member, zAddArgs) ? 1L : 0L;
		});
	}

	@Override
	public long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> tuples = scoredValues.stream()
					.map(one -> {
						return new org.springframework.data.redis.connection.DefaultTuple(one.getValue(),
								one.getScore());
					}).collect(Collectors.toSet());

			return connection.zSetCommands().zAdd(key, tuples);
		});
	}

	@Override
	public long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues, ZAddArgs params) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> tuples = scoredValues.stream()
					.map(one -> {
						return new org.springframework.data.redis.connection.DefaultTuple(one.getValue(),
								one.getScore());
					}).collect(Collectors.toSet());

			org.springframework.data.redis.connection.RedisZSetCommands.ZAddArgs zAddArgs = RedisTemplateUtils
					.convertZAddArgs(params);

			return connection.zSetCommands().zAdd(key, tuples, zAddArgs);
		});
	}

	@Override
	public long zcard(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zCard(key);
		});
	}

	@Override
	public long zcount(byte[] key, Range<? extends Number> range) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			return connection.zSetCommands().zCount(key, r);
		});
	}

	@Override
	public List<byte[]> zdiff(byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return new ArrayList(connection.zSetCommands().zDiff(keys));
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zdiffWithScores(byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zDiffWithScores(keys);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public long zdiffStore(byte[] dstkey, byte[]... keys) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zDiffStore(dstkey, keys);
		});
	}

	@Override
	public double zincrby(byte[] key, double increment, byte[] member) {
		return (Double) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zIncrBy(key, increment, member);
		});
	}

	@Override
	public List<byte[]> zinter(byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return new ArrayList(connection.zSetCommands().zInter(keys));
		});
	}

	@Override
	public List<byte[]> zinter(ZAggregateArgs params, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ScoredValue<byte[]>> zinterWithScores(byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zInterWithScores(keys);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zinterWithScores(ZAggregateArgs params, byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			NullableTuple2<Aggregate, Weights> tuple2 = RedisTemplateUtils.convertAggregateWeights(params);

			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zInterWithScores(tuple2.getT1(), tuple2.getT2(), keys);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public long zinterstore(byte[] dstkey, byte[]... sets) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zInterStore(dstkey, sets);
		});
	}

	@Override
	public long zinterstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			NullableTuple2<Aggregate, Weights> tuple2 = RedisTemplateUtils.convertAggregateWeights(params);
			return connection.zSetCommands().zInterStore(dstkey, tuple2.getT1(), tuple2.getT2(), sets);
		});
	}

	@Override
	public long zintercard(byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long zintercard(long limit, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long zlexcount(byte[] key, byte[] min, byte[] max) {
//		return (Long) redisTemplate.execute((RedisCallback) connection -> {
//			org.springframework.data.redis.connection.RedisZSetCommands.Range range = new org.springframework.data.redis.connection.RedisZSetCommands.Range();
//			range.gte(min);
//			range.lte(max);
//			return connection.zSetCommands().zLexCount(key, range);
//		});
		/**
		 * FIXME 上面不准
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> zmpop(SortedSetOption option, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], List<ScoredValue<byte[]>>> zmpop(SortedSetOption option, int count, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Double> zmscore(byte[] key, byte[]... members) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zMScore(key, members);
		});
	}

	@Override
	public ScoredValue<byte[]> zpopmax(byte[] key) {
		return (ScoredValue) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Tuple tuple = connection.zSetCommands()
					.zPopMax(key);
			return new ScoredValue<>(tuple.getScore(), tuple.getValue());
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zpopmax(byte[] key, int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zPopMax(key, count);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public ScoredValue<byte[]> zpopmin(byte[] key) {
		return (ScoredValue) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Tuple tuple = connection.zSetCommands()
					.zPopMin(key);
			return new ScoredValue<>(tuple.getScore(), tuple.getValue());
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zpopmin(byte[] key, int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zPopMin(key, count);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public byte[] zrandmember(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zRandMember(key);
		});
	}

	@Override
	public List<byte[]> zrandmember(byte[] key, long count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zRandMember(key, count);
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zrandmemberWithScores(byte[] key, long count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			List<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> list = connection.zSetCommands()
					.zRandMemberWithScore(key, count);
			return list.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public List<byte[]> zrange(byte[] key, long start, long stop) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return new ArrayList(connection.zSetCommands().zRange(key, start, stop));
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeWithScores(byte[] key, long start, long stop) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zRangeWithScores(key, start, stop);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range) {
//		return (List) redisTemplate.execute((RedisCallback) connection -> {
//			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
//					.convertRange(range);
//			return new ArrayList(connection.zSetCommands().zRangeByLex(key, r));
//		});
		/**
		 * FIXME 上面不准
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range, int offset, int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);

			Limit limit = new org.springframework.data.redis.connection.RedisZSetCommands.Limit();
			limit.offset(offset);
			limit.count(count);
			return new ArrayList(connection.zSetCommands().zRangeByLex(key, r, limit));
		});
	}

	@Override
	public List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			return new ArrayList(connection.zSetCommands().zRangeByScore(key, r));
		});
	}

	@Override
	public List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			Limit limit = new org.springframework.data.redis.connection.RedisZSetCommands.Limit();
			limit.offset(offset);
			limit.count(count);
			return new ArrayList(connection.zSetCommands().zRangeByScore(key, r, limit));
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zRangeByScoreWithScores(key, r);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset,
			int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			Limit limit = new org.springframework.data.redis.connection.RedisZSetCommands.Limit();
			limit.offset(offset);
			limit.count(count);
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zRangeByScoreWithScores(key, r, limit);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public long zrangestore(byte[] dest, byte[] src, Range<Long> range) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long zrangestoreByLex(byte[] dest, byte[] src, Range<byte[]> range, int offset, int count) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long zrangestoreByScore(byte[] dest, byte[] src, Range<? extends Number> range, int offset, int count) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long zrank(byte[] key, byte[] member) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zRank(key, member);
		});
	}

	@Override
	public long zrem(byte[] key, byte[]... members) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zRem(key, members);
		});
	}

	@Override
	public long zremrangeByLex(byte[] key, Range<byte[]> range) {
//		return (Long) redisTemplate.execute((RedisCallback) connection -> {
//			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
//					.convertRange(range);
//			return connection.zSetCommands().zRemRangeByLex(key, r);
//		});
		/**
		 * FIXME 上面不准
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public long zremrangeByRank(byte[] key, long start, long stop) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long zremrangeByScore(byte[] key, Range<? extends Number> range) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			return connection.zSetCommands().zRemRangeByScore(key, r);
		});
	}

	@Override
	public List<byte[]> zrevrange(byte[] key, long start, long stop) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return new ArrayList(connection.zSetCommands().zRevRange(key, start, stop));
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeWithScores(byte[] key, long start, long stop) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zRevRangeWithScores(key, start, stop);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range) {
//		return (List) redisTemplate.execute((RedisCallback) connection -> {
//			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
//					.convertRange(range);
//			return new ArrayList(connection.zSetCommands().zRevRangeByLex(key, r));
//		});
		/**
		 * FIXME 上面不准
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range, int offset, int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			Limit limit = new org.springframework.data.redis.connection.RedisZSetCommands.Limit();
			limit.offset(offset);
			limit.count(count);

			return new ArrayList(connection.zSetCommands().zRevRangeByLex(key, r, limit));
		});
	}

	@Override
	public List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			return new ArrayList(connection.zSetCommands().zRevRangeByScore(key, r));
		});
	}

	@Override
	public List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			Limit limit = new org.springframework.data.redis.connection.RedisZSetCommands.Limit();
			limit.offset(offset);
			limit.count(count);
			return new ArrayList(connection.zSetCommands().zRevRangeByScore(key, r, limit));
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zRevRangeByScoreWithScores(key, r);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset,
			int count) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			org.springframework.data.redis.connection.RedisZSetCommands.Range r = RedisTemplateUtils
					.convertRange(range);
			Limit limit = new org.springframework.data.redis.connection.RedisZSetCommands.Limit();
			limit.offset(offset);
			limit.count(count);

			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zRevRangeByScoreWithScores(key, r, limit);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public Long zrevrank(byte[] key, byte[] member) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zRevRank(key, member);
		});
	}

	@Override
	public ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor) {
		return zscan(key, cursor, null);
	}

	@Override
	public ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor, ScanArgs params) {
		return (ScoredValueScanCursor<byte[]>) redisTemplate.execute((RedisCallback) connection -> {

			ScanOptionsBuilder builder = ScanOptions.scanOptions();
			if (params != null) {
				params.match(params.getMatch());
				if (params.getCount() != null) {
					builder.count(params.getCount());
				}
			}

			ScanOptions scanOptions = builder.build();

			try (Cursor<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> scan = connection.zScan(key,
					scanOptions);) {

				List<ScoredValue<byte[]>> list = new LinkedList<>();

				while (scan.hasNext()) {
					org.springframework.data.redis.connection.RedisZSetCommands.Tuple tuple = scan.next();
					list.add(new ScoredValue<byte[]>(tuple.getScore(), tuple.getValue()));
				}

				String cursorId = Long.toString(scan.getCursorId());
				return new ScoredValueScanCursor<byte[]>(cursorId, "0".equals(cursorId), list);
			}
		});
	}

	@Override
	public Double zscore(byte[] key, byte[] member) {
		return (Double) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zScore(key, member);
		});
	}

	@Override
	public List<byte[]> zunion(byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return new ArrayList(connection.zSetCommands().zUnion(keys));
		});
	}

	@Override
	public List<byte[]> zunion(ZAggregateArgs params, byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ScoredValue<byte[]>> zunionWithScores(byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zUnionWithScores(keys);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public List<ScoredValue<byte[]>> zunionWithScores(ZAggregateArgs params, byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			NullableTuple2<Aggregate, Weights> tuple2 = RedisTemplateUtils.convertAggregateWeights(params);
			Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> set = connection.zSetCommands()
					.zUnionWithScores(tuple2.getT1(), tuple2.getT2(), keys);
			return set.stream().map(one -> {
				return new ScoredValue<byte[]>(one.getScore(), one.getValue());
			}).collect(Collectors.toList());
		});
	}

	@Override
	public long zunionstore(byte[] dstkey, byte[]... sets) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.zSetCommands().zUnionStore(dstkey, sets);
		});
	}

	@Override
	public long zunionstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			NullableTuple2<Aggregate, Weights> tuple2 = RedisTemplateUtils.convertAggregateWeights(params);
			return connection.zSetCommands().zUnionStore(dstkey, tuple2.getT1(), tuple2.getT2(), sets);
		});
	}

	@Override
	public long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			Point point = new Point(longitude, latitude);
			return connection.geoCommands().geoAdd(key, point, member);
		});
	}

	@Override
	public long geoadd(byte[] key, double longitude, double latitude, byte[] member, GeoAddArgs args) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long geoadd(byte[] key, GeoValue<byte[]>... geoValues) {
		Map<byte[], Point> map = new HashMap<>(geoValues.length, 1);
		for (GeoValue<byte[]> one : geoValues) {
			Point point = new Point(one.getLongitude(), one.getLatitude());
			map.put(one.getValue(), point);
		}

		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.geoCommands().geoAdd(key, map);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public long geoadd(byte[] key, GeoAddArgs args, GeoValue<byte[]>... geoValues) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2) {
		return (Double) redisTemplate.execute((RedisCallback) connection -> {
			Distance distance = connection.geoCommands().geoDist(key, member1, member2);
			return distance != null ? distance.getValue() : null;
		});
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
		return (Double) redisTemplate.execute((RedisCallback) connection -> {
			DistanceUnit du = null;
			if (unit.equals(GeoUnit.M)) {
				du = DistanceUnit.METERS;
			} else if (unit.equals(GeoUnit.MI)) {
				du = DistanceUnit.MILES;
			} else if (unit.equals(GeoUnit.KM)) {
				du = DistanceUnit.KILOMETERS;
			} else if (unit.equals(GeoUnit.FT)) {
				du = DistanceUnit.FEET;
			}

			Distance distance = connection.geoCommands().geoDist(key, member1, member2, du);
			return distance != null ? distance.getValue() : null;
		});
	}

	@Override
	public List<String> geohash(byte[] key, byte[]... members) {
		List<byte[]> list = jc.geohash(key, members);
		return list.stream().map(one -> new String(one, StandardCharsets.UTF_8)).collect(Collectors.toList());
	}

	@Override
	public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
		List<redis.clients.jedis.GeoCoordinate> list = jc.geopos(key, members);
		return list.stream().map(one -> {
			return new GeoCoordinate(one.getLongitude(), one.getLatitude());
		}).collect(Collectors.toList());
	}

	@Override
	public List<byte[]> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
		List<GeoRadiusResponse> list = jc.georadius(key, longitude, latitude, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()));
		return list.stream().map(GeoRadiusResponse::getMember).collect(Collectors.toList());
	}

	@Override
	public List<GeoWithin<byte[]>> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoArgs args) {
		GeoRadiusParam geoRadiusParam = JedisUtils.convertGeoRadiusParam(args);
		List<GeoRadiusResponse> list = jc.georadius(key, longitude, latitude, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()), geoRadiusParam);

		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list.stream().map(one -> {
			GeoCoordinate geoCoordinate = null;

			if (one.getCoordinate() != null) {
				geoCoordinate = new GeoCoordinate(one.getCoordinate().getLongitude(),
						one.getCoordinate().getLatitude());
			}
			return new GeoWithin<>(one.getMember(), one.getDistance(), one.getRawScore(), geoCoordinate);
		}).collect(Collectors.toList());
	}

	@Override
	public long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusStoreArgs<byte[]> storeArgs) {
		Tuple2<GeoRadiusParam, GeoRadiusStoreParam> tuple2 = JedisUtils.convertTuple(storeArgs);
		GeoRadiusParam geoRadiusParam = tuple2.getT1();
		GeoRadiusStoreParam geoRadiusStoreParam = tuple2.getT2();
		return jc.georadiusStore(key, longitude, latitude, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()), geoRadiusParam, geoRadiusStoreParam);
	}

	@Override
	public List<byte[]> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
		List<GeoRadiusResponse> list = jc.georadiusReadonly(key, longitude, latitude, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()));
		return list.stream().map(GeoRadiusResponse::getMember).collect(Collectors.toList());
	}

	@Override
	public List<GeoWithin<byte[]>> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
			GeoUnit unit, GeoArgs args) {
		GeoRadiusParam geoRadiusParam = JedisUtils.convertGeoRadiusParam(args);
		List<GeoRadiusResponse> list = jc.georadiusReadonly(key, longitude, latitude, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()), geoRadiusParam);

		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list.stream().map(one -> {
			GeoCoordinate geoCoordinate = null;

			if (one.getCoordinate() != null) {
				geoCoordinate = new GeoCoordinate(one.getCoordinate().getLongitude(),
						one.getCoordinate().getLatitude());
			}
			return new GeoWithin<>(one.getMember(), one.getDistance(), one.getRawScore(), geoCoordinate);
		}).collect(Collectors.toList());
	}

	@Override
	public List<byte[]> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
		List<GeoRadiusResponse> list = jc.georadiusByMember(key, member, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()));
		return list.stream().map(GeoRadiusResponse::getMember).collect(Collectors.toList());
	}

	@Override
	public List<GeoWithin<byte[]>> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoArgs args) {
		GeoRadiusParam geoRadiusParam = JedisUtils.convertGeoRadiusParam(args);

		List<GeoRadiusResponse> list = jc.georadiusByMember(key, member, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()), geoRadiusParam);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list.stream().map(one -> {
			GeoCoordinate geoCoordinate = null;

			if (one.getCoordinate() != null) {
				geoCoordinate = new GeoCoordinate(one.getCoordinate().getLongitude(),
						one.getCoordinate().getLatitude());
			}
			return new GeoWithin<>(one.getMember(), one.getDistance(), one.getRawScore(), geoCoordinate);
		}).collect(Collectors.toList());
	}

	@Override
	public long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoRadiusStoreArgs<byte[]> storeArgs) {
		Tuple2<GeoRadiusParam, GeoRadiusStoreParam> tuple2 = JedisUtils.convertTuple(storeArgs);
		GeoRadiusParam geoRadiusParam = tuple2.getT1();
		GeoRadiusStoreParam geoRadiusStoreParam = tuple2.getT2();
		return jc.georadiusByMemberStore(key, member, radius, redis.clients.jedis.args.GeoUnit.valueOf(unit.name()),
				geoRadiusParam, geoRadiusStoreParam);
	}

	@Override
	public List<byte[]> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
		List<GeoRadiusResponse> list = jc.georadiusByMemberReadonly(key, member, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()));
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list.stream().map(GeoRadiusResponse::getMember).collect(Collectors.toList());
	}

	@Override
	public List<GeoWithin<byte[]>> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoArgs args) {
		GeoRadiusParam geoRadiusParam = JedisUtils.convertGeoRadiusParam(args);
		List<GeoRadiusResponse> list = jc.georadiusByMemberReadonly(key, member, radius,
				redis.clients.jedis.args.GeoUnit.valueOf(unit.name()), geoRadiusParam);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list.stream().map(one -> {
			GeoCoordinate geoCoordinate = null;

			if (one.getCoordinate() != null) {
				geoCoordinate = new GeoCoordinate(one.getCoordinate().getLongitude(),
						one.getCoordinate().getLatitude());
			}
			return new GeoWithin<>(one.getMember(), one.getDistance(), one.getRawScore(), geoCoordinate);
		}).collect(Collectors.toList());
	}

	@Override
	public List<byte[]> geosearch(byte[] key, GeoRef<byte[]> reference, GeoPredicate predicate) {
		GeoSearchParam geoSearchParam = JedisUtils.convertGeoSearchParam(reference, predicate);
		List<GeoRadiusResponse> list = jc.geosearch(key, geoSearchParam);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list.stream().map(GeoRadiusResponse::getMember).collect(Collectors.toList());
	}

	@Override
	public List<GeoWithin<byte[]>> geosearch(byte[] key, GeoRef<byte[]> reference, GeoPredicate predicate,
			GeoArgs args) {
		GeoSearchParam geoSearchParam = JedisUtils.convertGeoSearchParam(reference, predicate, args);
		List<GeoRadiusResponse> list = jc.geosearch(key, geoSearchParam);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list.stream().map(one -> {
			GeoCoordinate geoCoordinate = null;

			if (one.getCoordinate() != null) {
				geoCoordinate = new GeoCoordinate(one.getCoordinate().getLongitude(),
						one.getCoordinate().getLatitude());
			}
			return new GeoWithin<>(one.getMember(), one.getDistance(), one.getRawScore(), geoCoordinate);
		}).collect(Collectors.toList());
	}

	@Override
	public long geosearchStore(byte[] destination, byte[] key, GeoRef<byte[]> reference, GeoPredicate predicate,
			GeoArgs args) {
		GeoSearchParam geoSearchParam = JedisUtils.convertGeoSearchParam(reference, predicate, args);
		return jc.geosearchStore(destination, key, geoSearchParam);
	}

	@Override
	public long geosearchStoreStoreDist(byte[] destination, byte[] key, GeoRef<byte[]> reference,
			GeoPredicate predicate, GeoArgs args) {
		GeoSearchParam geoSearchParam = JedisUtils.convertGeoSearchParam(reference, predicate, args);
		return jc.geosearchStoreStoreDist(destination, key, geoSearchParam);
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

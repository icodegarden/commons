package io.github.icodegarden.commons.redis.lettuce;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.RedisPubSubListener;
import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.LCSMatchResult;
import io.github.icodegarden.commons.redis.args.LCSParams;
import io.github.icodegarden.commons.redis.args.MigrateParams;
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.util.EvalUtils;
import io.github.icodegarden.commons.redis.util.LettuceUtils;
import io.lettuce.core.CopyArgs;
import io.lettuce.core.ExpireArgs;
import io.lettuce.core.KeyValue;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractLettuceRedisExecutor implements RedisExecutor {

	protected RedisClusterCommands<byte[], byte[]> syncRedisCommands;

	private StatefulRedisPubSubConnection<byte[], byte[]> connectPubSub;

	public void setRedisClusterCommands(RedisClusterCommands<byte[], byte[]> syncRedisCommands) {
		this.syncRedisCommands = syncRedisCommands;
	}

	@Override
	public void close() throws IOException {
		if (connectPubSub != null) {
			connectPubSub.close();
		}
	}

	@Override
	public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
		CopyArgs copyArgs = CopyArgs.Builder.replace(replace);
		return syncRedisCommands.copy(srcKey, dstKey, copyArgs);
	}

	@Override
	public long del(byte[] key) {
		return syncRedisCommands.del(key);
	}

	@Override
	public long del(byte[]... keys) {
		return syncRedisCommands.del(keys);
	}

	@Override
	public byte[] dump(byte[] key) {
		return syncRedisCommands.dump(key);
	}

	@Override
	public boolean exists(byte[] key) {
		return syncRedisCommands.exists(key) == 1;
	}

	@Override
	public long exists(byte[]... keys) {
		return syncRedisCommands.exists(keys);
	}

	@Override
	public long expire(byte[] key, long seconds) {
		return syncRedisCommands.expire(key, seconds) ? 1 : 0;
	}

	@Override
	public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
		ExpireArgs expireArgs = LettuceUtils.convertExpireArgs(expiryOption);
		return syncRedisCommands.expire(key, seconds, expireArgs) ? 1 : 0;
	}

	@Override
	public long expireAt(byte[] key, long unixTime) {
		return syncRedisCommands.expireat(key, unixTime) ? 1 : 0;
	}

	@Override
	public long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
		ExpireArgs expireArgs = LettuceUtils.convertExpireArgs(expiryOption);
		return syncRedisCommands.expire(key, unixTime, expireArgs) ? 1 : 0;
	}

	@Override
	public long expireTime(byte[] key) {
		return syncRedisCommands.expiretime(key);
	}

	@Override
	public String migrate(String host, int port, byte[] key, int timeout) {
		return syncRedisCommands.migrate(host, port, key, 0, timeout);
	}

	@Override
	public String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
		MigrateArgs<byte[]> migrateArgs = LettuceUtils.convertMigrateArgs(params);
		return syncRedisCommands.migrate(host, port, 0, timeout, migrateArgs);
	}

	@Override
	public byte[] objectEncoding(byte[] key) {
		return syncRedisCommands.objectEncoding(key).getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public Long objectFreq(byte[] key) {
		return syncRedisCommands.objectFreq(key);
	}

	@Override
	public Long objectIdletime(byte[] key) {
		return syncRedisCommands.objectIdletime(key);
	}

	@Override
	public Long objectRefcount(byte[] key) {
		return syncRedisCommands.objectRefcount(key);
	}

	@Override
	public long persist(byte[] key) {
		return syncRedisCommands.persist(key) ? 1 : 0;
	}

	@Override
	public long pexpire(byte[] key, long milliseconds) {
		return syncRedisCommands.pexpire(key, milliseconds) ? 1 : 0;
	}

	@Override
	public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
		ExpireArgs expireArgs = LettuceUtils.convertExpireArgs(expiryOption);
		return syncRedisCommands.pexpire(key, milliseconds, expireArgs) ? 1 : 0;
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return syncRedisCommands.pexpireat(key, millisecondsTimestamp) ? 1 : 0;
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
		ExpireArgs expireArgs = LettuceUtils.convertExpireArgs(expiryOption);
		return syncRedisCommands.pexpire(key, millisecondsTimestamp, expireArgs) ? 1 : 0;
	}

	@Override
	public long pexpireTime(byte[] key) {
		return syncRedisCommands.pexpiretime(key);
	}

	@Override
	public long pttl(byte[] key) {
		return syncRedisCommands.pttl(key);
	}

	@Override
	public byte[] randomBinaryKey() {
		return syncRedisCommands.randomkey();
	}

	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		return syncRedisCommands.rename(oldkey, newkey);
	}

	@Override
	public long renamenx(byte[] oldkey, byte[] newkey) {
		return syncRedisCommands.renamenx(oldkey, newkey) ? 1 : 0;
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue) {
		return syncRedisCommands.restore(key, ttl, serializedValue);
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
		RestoreArgs restoreArgs = LettuceUtils.convertRestoreArgs(params);
		restoreArgs.ttl(ttl);

		return syncRedisCommands.restore(key, serializedValue, restoreArgs);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor) {
		ScanCursor scanCursor = new ScanCursor();
		scanCursor.setCursor(new String(cursor, StandardCharsets.UTF_8));

		io.lettuce.core.KeyScanCursor<byte[]> scanResult = syncRedisCommands.scan(scanCursor);
		return LettuceUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params) {
		ScanCursor scanCursor = new ScanCursor();
		scanCursor.setCursor(new String(cursor, StandardCharsets.UTF_8));

		io.lettuce.core.ScanArgs scanArgs = LettuceUtils.convertScanArgs(params);

		io.lettuce.core.KeyScanCursor<byte[]> scanResult = syncRedisCommands.scan(scanCursor, scanArgs);
		return LettuceUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params, byte[] type) {
		ScanCursor scanCursor = new ScanCursor();
		scanCursor.setCursor(new String(cursor, StandardCharsets.UTF_8));

		io.lettuce.core.KeyScanArgs scanArgs = LettuceUtils.convertScanArgs(params);
		scanArgs.type(new String(type, StandardCharsets.UTF_8));

		io.lettuce.core.KeyScanCursor<byte[]> scanResult = syncRedisCommands.scan(scanCursor, scanArgs);
		return LettuceUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public List<byte[]> sort(byte[] key) {
		return syncRedisCommands.sort(key);
	}

	@Override
	public List<byte[]> sort(byte[] key, SortArgs params) {
		io.lettuce.core.SortArgs sortArgs = LettuceUtils.convertSortArgs(params);
		return syncRedisCommands.sort(key, sortArgs);
	}

	@Override
	public long sort(byte[] key, byte[] dstkey) {
		io.lettuce.core.SortArgs sortArgs = new io.lettuce.core.SortArgs();
		return syncRedisCommands.sortStore(key, sortArgs, dstkey);
	}

	@Override
	public long sort(byte[] key, SortArgs params, byte[] dstkey) {
		io.lettuce.core.SortArgs sortArgs = LettuceUtils.convertSortArgs(params);
		return syncRedisCommands.sortStore(key, sortArgs, dstkey);
	}

	@Override
	public List<byte[]> sortReadonly(byte[] key, SortArgs params) {
		io.lettuce.core.SortArgs sortArgs = LettuceUtils.convertSortArgs(params);
		return syncRedisCommands.sortReadOnly(key, sortArgs);
	}

	@Override
	public long touch(byte[] key) {
		return syncRedisCommands.touch(key);
	}

	@Override
	public long touch(byte[]... keys) {
		return syncRedisCommands.touch(keys);
	}

	@Override
	public long ttl(byte[] key) {
		return syncRedisCommands.ttl(key);
	}

	@Override
	public String type(byte[] key) {
		return syncRedisCommands.type(key);
	}

	@Override
	public long unlink(byte[] key) {
		return syncRedisCommands.unlink(key);
	}

	@Override
	public long unlink(byte[]... keys) {
		return syncRedisCommands.unlink(keys);
	}

	@Override
	public Long memoryUsage(byte[] key) {
		return syncRedisCommands.memoryUsage(key);
	}

	@Override
	public Long memoryUsage(byte[] key, int samples) {
		return syncRedisCommands.memoryUsage(key);
	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {
		List<byte[]> list = syncRedisCommands.keys(pattern);
		return new HashSet<byte[]>(list);
	}

	@Override
	public List<Object> eval(byte[] script) {
		Object obj = syncRedisCommands.eval(script, ScriptOutputType.MULTI, new byte[0]);
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public List<Object> eval(byte[] script, int keyCount, byte[]... params) {
		Tuple2<List<byte[]>, List<byte[]>> tuple2 = CollectionUtils.splitByKeyGroup(params, keyCount);
		List<byte[]> keys = tuple2.getT1();
		List<byte[]> args = tuple2.getT2();

		Object obj = syncRedisCommands.eval(script, ScriptOutputType.MULTI, keys.toArray(new byte[keys.size()][]),
				args.toArray(new byte[args.size()][]));
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public List<Object> eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		Object obj = syncRedisCommands.eval(script, ScriptOutputType.MULTI, keys.toArray(new byte[keys.size()][]),
				args.toArray(new byte[args.size()][]));
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public List<Object> evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
		Object obj = syncRedisCommands.evalReadOnly(script, ScriptOutputType.MULTI,
				keys.toArray(new byte[keys.size()][]), args.toArray(new byte[args.size()][]));
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public Long append(byte[] key, byte[] value) {
		return syncRedisCommands.append(key, value);
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
	public byte[] get(byte[] key) {
		return syncRedisCommands.get(key);
	}

	@Override
	public byte[] getDel(byte[] key) {
		return syncRedisCommands.getdel(key);
	}

	@Override
	public byte[] getEx(byte[] key, GetExArgs params) {
		io.lettuce.core.GetExArgs getExArgs = LettuceUtils.convertGetExArgs(params);
		return syncRedisCommands.getex(key, getExArgs);
	}

	@Override
	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return syncRedisCommands.getrange(key, startOffset, endOffset);
	}

	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		return syncRedisCommands.getset(key, value);
	}

	@Override
	public Long incr(byte[] key) {
		return syncRedisCommands.incr(key);
	}

	@Override
	public Long incrBy(byte[] key, long increment) {
		return syncRedisCommands.incrby(key, increment);
	}

	@Override
	public Double incrByFloat(byte[] key, double increment) {
		return syncRedisCommands.incrbyfloat(key, increment);
	}

	@Override
	public LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		List<KeyValue<byte[], byte[]>> list = syncRedisCommands.mget(keys);
		return list.stream().map(KeyValue::getValue).collect(Collectors.toList());
	}

	@Override
	public String mset(byte[]... keysvalues) {
		Map<byte[], byte[]> map = CollectionUtils.keysValuesToMap(keysvalues);
		return syncRedisCommands.mset(map);
	}

	@Override
	public Long msetnx(byte[]... keysvalues) {
		Map<byte[], byte[]> map = CollectionUtils.keysValuesToMap(keysvalues);
		return syncRedisCommands.msetnx(map) ? 1L : 0L;
	}

	@Override
	public String psetex(byte[] key, long milliseconds, byte[] value) {
		return syncRedisCommands.psetex(key, milliseconds, value);
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
	public Long setrange(byte[] key, long offset, byte[] value) {
		return syncRedisCommands.setrange(key, offset, value);
	}

	@Override
	public Long strlen(byte[] key) {
		return syncRedisCommands.strlen(key);
	}

	@Override
	public byte[] substr(byte[] key, int start, int end) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void subscribe(byte[] channel, RedisPubSubListener<byte[], byte[]> listener) {
		/**
		 * lettuce
		 * 同一个1个Connection，可以订阅很多个不同channel，并可以addListener很多个，有消息时所有Listener都会遍历触发<br>
		 * 所以无论订阅多少个channel，都使用1个Connection
		 */
		if (connectPubSub == null) {
			synchronized (this) {
				if (connectPubSub == null) {
					connectPubSub = connectPubSub();
				}
			}
		}

		if (listener != null) {
			connectPubSub.addListener(new io.lettuce.core.pubsub.RedisPubSubListener<byte[], byte[]>() {

				@Override
				public void message(byte[] channel, byte[] message) {
					listener.message(channel, message);
				}

				@Override
				public void message(byte[] pattern, byte[] channel, byte[] message) {
					listener.message(pattern, channel, message);
				}

				@Override
				public void subscribed(byte[] channel, long count) {
					listener.subscribed(channel, count);
				}

				@Override
				public void psubscribed(byte[] pattern, long count) {
					listener.psubscribed(pattern, count);
				}

				@Override
				public void unsubscribed(byte[] channel, long count) {
					listener.unsubscribed(channel, count);
				}

				@Override
				public void punsubscribed(byte[] pattern, long count) {
					listener.punsubscribed(channel, count);
				}
			});
		}

		RedisPubSubCommands<byte[], byte[]> sync = connectPubSub.sync();
		sync.subscribe(channel);
	}

	@Override
	public void unsubscribe(byte[] channel) {
		RedisPubSubCommands<byte[], byte[]> sync = connectPubSub.sync();
		sync.unsubscribe(channel);
	}

	protected abstract StatefulRedisPubSubConnection<byte[], byte[]> connectPubSub();
}

package io.github.icodegarden.commons.redis.lettuce;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
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
import io.github.icodegarden.commons.redis.util.LettuceUtils;
import io.lettuce.core.CopyArgs;
import io.lettuce.core.ExpireArgs;
import io.lettuce.core.LMPopArgs;
import io.lettuce.core.LMoveArgs;
import io.lettuce.core.LPosArgs;
import io.lettuce.core.Limit;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.ZStoreArgs;
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
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.KeyScanCursor<byte[]> scanResult = syncRedisCommands.scan(scanCursor);
		return LettuceUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.ScanArgs scanArgs = LettuceUtils.convertScanArgs(params);

		io.lettuce.core.KeyScanCursor<byte[]> scanResult = syncRedisCommands.scan(scanCursor, scanArgs);
		return LettuceUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params, byte[] type) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

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
	public Long hdel(byte[] key, byte[]... fields) {
		return syncRedisCommands.hdel(key, fields);
	}

	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		return syncRedisCommands.hexists(key, field);
	}

	@Override
	public byte[] hget(byte[] key, byte[] field) {
		return syncRedisCommands.hget(key, field);
	}

	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return syncRedisCommands.hgetall(key);
	}

	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return syncRedisCommands.hincrby(key, field, value);
	}

	@Override
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return syncRedisCommands.hincrbyfloat(key, field, value);
	}

	@Override
	public Set<byte[]> hkeys(byte[] key) {
		List<byte[]> list = syncRedisCommands.hkeys(key);
		return new HashSet<>(list);
	}

	@Override
	public Long hlen(byte[] key) {
		return syncRedisCommands.hlen(key);
	}

	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		List<io.lettuce.core.KeyValue<byte[], byte[]>> list = syncRedisCommands.hmget(key, fields);
		return list.stream().map(io.lettuce.core.KeyValue::getValue).collect(Collectors.toList());
	}

	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return syncRedisCommands.hmset(key, hash);
	}

	@Override
	public byte[] hrandfield(byte[] key) {
		return syncRedisCommands.hrandfield(key);
	}

	@Override
	public List<byte[]> hrandfield(byte[] key, long count) {
		return syncRedisCommands.hrandfield(key, count);
	}

	@Override
	public Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
		List<io.lettuce.core.KeyValue<byte[], byte[]>> list = syncRedisCommands.hrandfieldWithvalues(key, count);
		return list.stream().collect(
				Collectors.toMap(io.lettuce.core.KeyValue::getKey, io.lettuce.core.KeyValue::getValue, (a, b) -> a));
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.MapScanCursor<byte[], byte[]> scanResult = syncRedisCommands.hscan(key, scanCursor);
		return LettuceUtils.convertMapScanCursor(scanResult);
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor, ScanArgs params) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.ScanArgs scanArgs = LettuceUtils.convertScanArgs(params);

		io.lettuce.core.MapScanCursor<byte[], byte[]> scanResult = syncRedisCommands.hscan(key, scanCursor, scanArgs);
		return LettuceUtils.convertMapScanCursor(scanResult);
	}

	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		return syncRedisCommands.hset(key, field, value) ? 1L : 0;
	}

	@Override
	public Long hset(byte[] key, Map<byte[], byte[]> hash) {
		return syncRedisCommands.hset(key, hash);
	}

	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return syncRedisCommands.hsetnx(key, field, value) ? 1L : 0;
	}

	@Override
	public Long hstrlen(byte[] key, byte[] field) {
		return syncRedisCommands.hstrlen(key, field);
	}

	@Override
	public List<byte[]> hvals(byte[] key) {
		return syncRedisCommands.hvals(key);
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
		List<io.lettuce.core.KeyValue<byte[], byte[]>> list = syncRedisCommands.mget(keys);
		return list.stream().map(io.lettuce.core.KeyValue::getValue).collect(Collectors.toList());
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
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, long timeout) {
		LMoveArgs lMoveArgs = convertLMoveArgs(from, to);
		return syncRedisCommands.blmove(srcKey, dstKey, lMoveArgs, timeout);
	}

	@Override
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
		LMoveArgs lMoveArgs = convertLMoveArgs(from, to);
		return syncRedisCommands.blmove(srcKey, dstKey, lMoveArgs, timeout);
	}

	private LMoveArgs convertLMoveArgs(ListDirection from, ListDirection to) {
		LMoveArgs lMoveArgs = null;
		if (from.equals(ListDirection.LEFT) && to.equals(ListDirection.LEFT)) {
			lMoveArgs = LMoveArgs.Builder.leftLeft();
		} else if (from.equals(ListDirection.LEFT) && to.equals(ListDirection.RIGHT)) {
			lMoveArgs = LMoveArgs.Builder.leftRight();
		} else if (from.equals(ListDirection.RIGHT) && to.equals(ListDirection.LEFT)) {
			lMoveArgs = LMoveArgs.Builder.rightLeft();
		} else if (from.equals(ListDirection.RIGHT) && to.equals(ListDirection.RIGHT)) {
			lMoveArgs = LMoveArgs.Builder.rightRight();
		}
		return lMoveArgs;
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, byte[]... keys) {
		LMPopArgs lmPopArgs = convertLMPopArgs(direction, null);
		io.lettuce.core.KeyValue<byte[], List<byte[]>> kv = syncRedisCommands.blmpop(timeout, lmPopArgs, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, long count, byte[]... keys) {
		LMPopArgs lmPopArgs = convertLMPopArgs(direction, count);
		io.lettuce.core.KeyValue<byte[], List<byte[]>> kv = syncRedisCommands.blmpop(timeout, lmPopArgs, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	private LMPopArgs convertLMPopArgs(ListDirection direction, Long count) {
		LMPopArgs lmPopArgs = null;
		if (direction.equals(ListDirection.LEFT)) {
			lmPopArgs = LMPopArgs.Builder.left();
		} else if (direction.equals(ListDirection.RIGHT)) {
			lmPopArgs = LMPopArgs.Builder.right();
		}
		if (count != null) {
			lmPopArgs.count(count);
		}
		return lmPopArgs;
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(long timeout, byte[]... keys) {
		io.lettuce.core.KeyValue<byte[], byte[]> kv = syncRedisCommands.blpop(timeout, keys);
		return new KeyValue<byte[], byte[]>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(double timeout, byte[]... keys) {
		io.lettuce.core.KeyValue<byte[], byte[]> kv = syncRedisCommands.blpop(timeout, keys);
		return new KeyValue<byte[], byte[]>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(long timeout, byte[]... keys) {
		io.lettuce.core.KeyValue<byte[], byte[]> kv = syncRedisCommands.brpop(timeout, keys);
		return new KeyValue<byte[], byte[]>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(double timeout, byte[]... keys) {
		io.lettuce.core.KeyValue<byte[], byte[]> kv = syncRedisCommands.brpop(timeout, keys);
		return new KeyValue<byte[], byte[]>(kv.getKey(), kv.getValue());
	}

	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, long timeout) {
		return syncRedisCommands.brpoplpush(timeout, source, destination);
	}

	@Override
	public byte[] lindex(byte[] key, long index) {
		return syncRedisCommands.lindex(key, index);
	}

	@Override
	public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
		return syncRedisCommands.linsert(key, where.equals(ListPosition.BEFORE), pivot, value);
	}

	@Override
	public Long llen(byte[] key) {
		return syncRedisCommands.llen(key);
	}

	@Override
	public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
		LMoveArgs lMoveArgs = convertLMoveArgs(from, to);
		return syncRedisCommands.lmove(srcKey, dstKey, lMoveArgs);
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys) {
		LMPopArgs lmPopArgs = convertLMPopArgs(direction, null);
		io.lettuce.core.KeyValue<byte[], List<byte[]>> kv = syncRedisCommands.lmpop(lmPopArgs, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, long count, byte[]... keys) {
		LMPopArgs lmPopArgs = convertLMPopArgs(direction, count);
		io.lettuce.core.KeyValue<byte[], List<byte[]>> kv = syncRedisCommands.lmpop(lmPopArgs, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	@Override
	public byte[] lpop(byte[] key) {
		return syncRedisCommands.lpop(key);
	}

	@Override
	public List<byte[]> lpop(byte[] key, long count) {
		return syncRedisCommands.lpop(key, count);
	}

	@Override
	public Long lpos(byte[] key, byte[] element) {
		return syncRedisCommands.lpos(key, element);
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, long count) {
		return syncRedisCommands.lpos(key, element, (int) count);
	}

	@Override
	public Long lpos(byte[] key, byte[] element, LPosParams params) {
		LPosArgs lPosArgs = convertLPosArgs(params);
		return syncRedisCommands.lpos(key, element, lPosArgs);
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
		LPosArgs lPosArgs = convertLPosArgs(params);
		return syncRedisCommands.lpos(key, element, (int) count, lPosArgs);
	}

	private LPosArgs convertLPosArgs(LPosParams params) {
		LPosArgs lPosArgs = new LPosArgs();
		if (params.getRank() != null) {
			lPosArgs.rank(params.getRank());
		}
		if (params.getMaxLen() != null && params.getMaxLen() != 0/* lettuce要求不能是0 */) {
			lPosArgs.maxlen(params.getMaxLen());
		}
		return lPosArgs;
	}

	@Override
	public Long lpush(byte[] key, byte[]... values) {
		return syncRedisCommands.lpush(key, values);
	}

	@Override
	public Long lpushx(byte[] key, byte[]... values) {
		return syncRedisCommands.lpushx(key, values);
	}

	@Override
	public List<byte[]> lrange(byte[] key, long start, long stop) {
		return syncRedisCommands.lrange(key, start, stop);
	}

	@Override
	public Long lrem(byte[] key, long count, byte[] value) {
		return syncRedisCommands.lrem(key, count, value);
	}

	@Override
	public String lset(byte[] key, long index, byte[] value) {
		return syncRedisCommands.lset(key, index, value);
	}

	@Override
	public String ltrim(byte[] key, long start, long stop) {
		return syncRedisCommands.ltrim(key, start, stop);
	}

	@Override
	public byte[] rpop(byte[] key) {
		return syncRedisCommands.rpop(key);
	}

	@Override
	public List<byte[]> rpop(byte[] key, long count) {
		return syncRedisCommands.rpop(key, count);
	}

	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		return syncRedisCommands.rpoplpush(srckey, dstkey);
	}

	@Override
	public Long rpush(byte[] key, byte[]... values) {
		return syncRedisCommands.rpush(key, values);
	}

	@Override
	public Long rpushx(byte[] key, byte[]... values) {
		return syncRedisCommands.rpushx(key, values);
	}

	@Override
	public Long sadd(byte[] key, byte[]... members) {
		return syncRedisCommands.sadd(key, members);
	}

	@Override
	public Long scard(byte[] key) {
		return syncRedisCommands.scard(key);
	}

	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		return syncRedisCommands.sdiff(keys);
	}

	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		return syncRedisCommands.sdiffstore(dstkey, keys);
	}

	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		return syncRedisCommands.sinter(keys);
	}

	@Override
	public long sintercard(byte[]... keys) {
		return syncRedisCommands.sintercard(keys);
	}

	@Override
	public long sintercard(int limit, byte[]... keys) {
		return syncRedisCommands.sintercard(limit, keys);
	}

	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		return syncRedisCommands.sinterstore(dstkey, keys);
	}

	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		return syncRedisCommands.sismember(key, member);
	}

	@Override
	public Set<byte[]> smembers(byte[] key) {
		return syncRedisCommands.smembers(key);
	}

	@Override
	public List<Boolean> smismember(byte[] key, byte[]... members) {
		return syncRedisCommands.smismember(key, members);
	}

	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return syncRedisCommands.smove(srckey, dstkey, member) ? 1L : 0L;
	}

	@Override
	public byte[] spop(byte[] key) {
		return syncRedisCommands.spop(key);
	}

	@Override
	public Set<byte[]> spop(byte[] key, long count) {
		return syncRedisCommands.spop(key, count);
	}

	@Override
	public byte[] srandmember(byte[] key) {
		return syncRedisCommands.srandmember(key);
	}

	@Override
	public List<byte[]> srandmember(byte[] key, int count) {
		return syncRedisCommands.srandmember(key, count);
	}

	@Override
	public Long srem(byte[] key, byte[]... members) {
		return syncRedisCommands.srem(key, members);
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.ValueScanCursor<byte[]> scanResult = syncRedisCommands.sscan(key, scanCursor);
		return LettuceUtils.convertValueScanCursor(scanResult);
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor, ScanArgs params) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.ScanArgs scanArgs = LettuceUtils.convertScanArgs(params);

		io.lettuce.core.ValueScanCursor<byte[]> scanResult = syncRedisCommands.sscan(key, scanCursor, scanArgs);
		return LettuceUtils.convertValueScanCursor(scanResult);
	}

	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		return syncRedisCommands.sunion(keys);
	}

	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		return syncRedisCommands.sunionstore(dstkey, keys);
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzmpop(long timeout, SortedSetOption option, byte[]... keys) {
		if (option.equals(SortedSetOption.MIN)) {
			return bzpopmin(timeout, keys);
		} else if (option.equals(SortedSetOption.MAX)) {
			return bzpopmax(timeout, keys);
		}
		return null;
	}

	@Override
	public KeyValue<byte[], List<ScoredValue<byte[]>>> bzmpop(long timeout, SortedSetOption option, int count,
			byte[]... keys) {
		throw new UnsupportedOperationException();
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzpopmax(double timeout, byte[]... keys) {
		io.lettuce.core.KeyValue<byte[], io.lettuce.core.ScoredValue<byte[]>> kv = syncRedisCommands.bzpopmax(timeout,
				keys);
		if (kv == null) {
			return null;
		}

		io.lettuce.core.ScoredValue<byte[]> v = kv.getValue();
		ScoredValue<byte[]> scoredValue = new ScoredValue<byte[]>(v.getScore(), v.getValue());
		return new KeyValue<byte[], ScoredValue<byte[]>>(kv.getKey(), scoredValue);
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzpopmin(double timeout, byte[]... keys) {
		io.lettuce.core.KeyValue<byte[], io.lettuce.core.ScoredValue<byte[]>> kv = syncRedisCommands.bzpopmin(timeout,
				keys);
		if (kv == null) {
			return null;
		}

		io.lettuce.core.ScoredValue<byte[]> v = kv.getValue();
		ScoredValue<byte[]> scoredValue = new ScoredValue<byte[]>(v.getScore(), v.getValue());
		return new KeyValue<byte[], ScoredValue<byte[]>>(kv.getKey(), scoredValue);
	}

	@Override
	public long zadd(byte[] key, double score, byte[] member) {
		return syncRedisCommands.zadd(key, score, member);
	}

	@Override
	public long zadd(byte[] key, double score, byte[] member, ZAddArgs params) {
		io.lettuce.core.ZAddArgs zAddArgs = LettuceUtils.convertZAddArgs(params);
		return syncRedisCommands.zadd(key, zAddArgs, score, member);
	}

	@Override
	public long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = scoredValues.stream()
				.map(one -> io.lettuce.core.ScoredValue.just(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
		io.lettuce.core.ScoredValue<byte[]>[] svs = list.toArray(new io.lettuce.core.ScoredValue[scoredValues.size()]);

		return syncRedisCommands.zadd(key, svs);
	}

	@Override
	public long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues, ZAddArgs params) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = scoredValues.stream()
				.map(one -> io.lettuce.core.ScoredValue.just(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
		io.lettuce.core.ScoredValue<byte[]>[] svs = list.toArray(new io.lettuce.core.ScoredValue[scoredValues.size()]);

		io.lettuce.core.ZAddArgs zAddArgs = LettuceUtils.convertZAddArgs(params);

		return syncRedisCommands.zadd(key, zAddArgs, svs);
	}

	@Override
	public long zcard(byte[] key) {
		return syncRedisCommands.zcard(key);
	}

	@Override
	public long zcount(byte[] key, Range<? extends Number> range) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zcount(key, r);
	}

	@Override
	public List<byte[]> zdiff(byte[]... keys) {
		return syncRedisCommands.zdiff(keys);
	}

	@Override
	public List<ScoredValue<byte[]>> zdiffWithScores(byte[]... keys) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zdiffWithScores(keys);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public long zdiffStore(byte[] dstkey, byte[]... keys) {
		return syncRedisCommands.zdiffstore(dstkey, keys);
	}

	@Override
	public double zincrby(byte[] key, double increment, byte[] member) {
		return syncRedisCommands.zincrby(key, increment, member);
	}

	@Override
	public List<byte[]> zinter(byte[]... keys) {
		return syncRedisCommands.zinter(keys);
	}

	@Override
	public List<byte[]> zinter(ZAggregateArgs params, byte[]... keys) {
		io.lettuce.core.ZAggregateArgs zAggregateArgs = LettuceUtils.convertZAggregateArgs(params);
		return syncRedisCommands.zinter(zAggregateArgs, keys);
	}

	@Override
	public List<ScoredValue<byte[]>> zinterWithScores(byte[]... keys) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zinterWithScores(keys);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zinterWithScores(ZAggregateArgs params, byte[]... keys) {
		io.lettuce.core.ZAggregateArgs zAggregateArgs = LettuceUtils.convertZAggregateArgs(params);
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zinterWithScores(zAggregateArgs, keys);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public long zinterstore(byte[] dstkey, byte[]... sets) {
		return syncRedisCommands.zinterstore(dstkey, sets);
	}

	@Override
	public long zinterstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets) {
		ZStoreArgs storeArgs = LettuceUtils.convertZStoreArgs(params);
		return syncRedisCommands.zinterstore(dstkey, storeArgs, sets);
	}

	@Override
	public long zintercard(byte[]... keys) {
		return syncRedisCommands.zintercard(keys);
	}

	@Override
	public long zintercard(long limit, byte[]... keys) {
		return syncRedisCommands.zintercard(limit, keys);
	}

	@Override
	public long zlexcount(byte[] key, byte[] min, byte[] max) {
		return syncRedisCommands.zlexcount(key, new String(min, StandardCharsets.UTF_8),
				new String(max, StandardCharsets.UTF_8));
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
		return syncRedisCommands.zmscore(key, members);
	}

	@Override
	public ScoredValue<byte[]> zpopmax(byte[] key) {
		io.lettuce.core.ScoredValue<byte[]> one = syncRedisCommands.zpopmax(key);
		return new ScoredValue<byte[]>(one.getScore(), one.getValue());
	}

	@Override
	public List<ScoredValue<byte[]>> zpopmax(byte[] key, int count) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zpopmax(key, count);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public ScoredValue<byte[]> zpopmin(byte[] key) {
		io.lettuce.core.ScoredValue<byte[]> one = syncRedisCommands.zpopmin(key);
		return new ScoredValue<byte[]>(one.getScore(), one.getValue());
	}

	@Override
	public List<ScoredValue<byte[]>> zpopmin(byte[] key, int count) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zpopmin(key, count);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public byte[] zrandmember(byte[] key) {
		return syncRedisCommands.zrandmember(key);
	}

	@Override
	public List<byte[]> zrandmember(byte[] key, long count) {
		return syncRedisCommands.zrandmember(key, count);
	}

	@Override
	public List<ScoredValue<byte[]>> zrandmemberWithScores(byte[] key, long count) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zrandmemberWithScores(key, count);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public List<byte[]> zrange(byte[] key, long start, long stop) {

		return syncRedisCommands.zrange(key, start, stop);
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeWithScores(byte[] key, long start, long stop) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zrangeWithScores(key, start, stop);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range) {
		io.lettuce.core.Range<byte[]> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zrangebylex(key, r);
	}

	@Override
	public List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range, int offset, int count) {
		io.lettuce.core.Range<byte[]> r = LettuceUtils.convertRange(range);
		Limit limit = io.lettuce.core.Limit.create(offset, count);
		return syncRedisCommands.zrangebylex(key, r, limit);
	}

	@Override
	public List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zrangebyscore(key, r);
	}

	@Override
	public List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		Limit limit = io.lettuce.core.Limit.create(offset, count);
		return syncRedisCommands.zrangebyscore(key, r, limit);
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zrangebyscoreWithScores(key, r);

		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset,
			int count) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		Limit limit = io.lettuce.core.Limit.create(offset, count);
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zrangebyscoreWithScores(key, r, limit);

		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public long zrangestore(byte[] dest, byte[] src, Range<Long> range) {
		io.lettuce.core.Range<Long> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zrangestore(dest, src, r);
	}

	@Override
	public Long zrank(byte[] key, byte[] member) {
		return syncRedisCommands.zrank(key, member);
	}

	@Override
	public long zrem(byte[] key, byte[]... members) {
		return syncRedisCommands.zrem(key, members);
	}

	@Override
	public long zremrangeByLex(byte[] key, Range<byte[]> range) {
		io.lettuce.core.Range<byte[]> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zremrangebylex(key, r);
	}

	@Override
	public long zremrangeByRank(byte[] key, long start, long stop) {
		return syncRedisCommands.zremrangebyrank(key, start, stop);
	}

	@Override
	public long zremrangeByScore(byte[] key, Range<? extends Number> range) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zremrangebyscore(key, r);
	}

	@Override
	public List<byte[]> zrevrange(byte[] key, long start, long stop) {
		return syncRedisCommands.zrevrange(key, start, stop);
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeWithScores(byte[] key, long start, long stop) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zrevrangeWithScores(key, start, stop);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range) {
		io.lettuce.core.Range<byte[]> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zrevrangebylex(key, r);
	}

	@Override
	public List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range, int offset, int count) {
		io.lettuce.core.Range<byte[]> r = LettuceUtils.convertRange(range);
		Limit limit = io.lettuce.core.Limit.create(offset, count);
		return syncRedisCommands.zrevrangebylex(key, r, limit);
	}

	@Override
	public List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		return syncRedisCommands.zrevrangebyscore(key, r);
	}

	@Override
	public List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		Limit limit = io.lettuce.core.Limit.create(offset, count);
		return syncRedisCommands.zrevrangebyscore(key, r, limit);
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zrevrangebyscoreWithScores(key, r);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset,
			int count) {
		io.lettuce.core.Range<? extends Number> r = LettuceUtils.convertRange(range);
		Limit limit = io.lettuce.core.Limit.create(offset, count);
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zrevrangebyscoreWithScores(key, r, limit);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public Long zrevrank(byte[] key, byte[] member) {
		return syncRedisCommands.zrevrank(key, member);
	}

	@Override
	public ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.ScoredValueScanCursor<byte[]> scanResult = syncRedisCommands.zscan(key, scanCursor);
		return LettuceUtils.convertScoredValueScanCursor(scanResult);
	}

	@Override
	public ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor, ScanArgs params) {
		ScanCursor scanCursor = LettuceUtils.convertScanCursor(cursor);

		io.lettuce.core.ScanArgs scanArgs = LettuceUtils.convertScanArgs(params);

		io.lettuce.core.ScoredValueScanCursor<byte[]> scanResult = syncRedisCommands.zscan(key, scanCursor, scanArgs);
		return LettuceUtils.convertScoredValueScanCursor(scanResult);
	}

	@Override
	public Double zscore(byte[] key, byte[] member) {
		return syncRedisCommands.zscore(key, member);
	}

	@Override
	public List<byte[]> zunion(byte[]... keys) {
		return syncRedisCommands.zunion(keys);
	}

	@Override
	public List<byte[]> zunion(ZAggregateArgs params, byte[]... keys) {
		io.lettuce.core.ZAggregateArgs zAggregateArgs = LettuceUtils.convertZAggregateArgs(params);
		return syncRedisCommands.zunion(zAggregateArgs, keys);
	}

	@Override
	public List<ScoredValue<byte[]>> zunionWithScores(byte[]... keys) {
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zunionWithScores(keys);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zunionWithScores(ZAggregateArgs params, byte[]... keys) {
		io.lettuce.core.ZAggregateArgs zAggregateArgs = LettuceUtils.convertZAggregateArgs(params);
		List<io.lettuce.core.ScoredValue<byte[]>> list = syncRedisCommands.zunionWithScores(zAggregateArgs, keys);
		return list.stream().map(one -> new ScoredValue<byte[]>(one.getScore(), one.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public long zunionstore(byte[] dstkey, byte[]... sets) {
		return syncRedisCommands.zunionstore(dstkey, sets);
	}

	@Override
	public long zunionstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets) {
		ZStoreArgs storeArgs = LettuceUtils.convertZStoreArgs(params);
		return syncRedisCommands.zunionstore(dstkey, storeArgs, sets);
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

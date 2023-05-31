package io.github.icodegarden.commons.redis.jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.lang.tuple.Tuple2;
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
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZParams;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisClusterRedisExecutor implements RedisExecutor {

	private Map<byte[], BinaryJedisPubSub> subMap = new ConcurrentHashMap<byte[], BinaryJedisPubSub>();

	private JedisCluster jc;

	public JedisClusterRedisExecutor(JedisCluster jc) {
		this.jc = jc;
	}

	public JedisClusterRedisExecutor(Set<HostAndPort> clusterNodes, int connectionTimeout, int soTimeout,
			int maxAttempts, String password, GenericObjectPoolConfig poolConfig) {
		this(new JedisCluster(clusterNodes, connectionTimeout, soTimeout, maxAttempts, password, poolConfig));
	}

	@Override
	public void close() throws IOException {
		jc.close();
	}

	@Override
	public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
		return jc.copy(srcKey, dstKey, replace);
	}

	@Override
	public long del(byte[] key) {
		return jc.del(key);
	}

	@Override
	public long del(byte[]... keys) {
		return jc.del(keys);
	}

	@Override
	public byte[] dump(byte[] key) {
		return jc.dump(key);
	}

	@Override
	public boolean exists(byte[] key) {
		return jc.exists(key);
	}

	@Override
	public long exists(byte[]... keys) {
		return jc.exists(keys);
	}

	@Override
	public long expire(byte[] key, long seconds) {
		return jc.expire(key, seconds);
	}

	@Override
	public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
		redis.clients.jedis.args.ExpiryOption valueOf = redis.clients.jedis.args.ExpiryOption
				.valueOf(expiryOption.name());
		return jc.expire(key, seconds, valueOf);
	}

	@Override
	public long expireAt(byte[] key, long unixTime) {
		return jc.expireAt(key, unixTime);
	}

	@Override
	public long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
		redis.clients.jedis.args.ExpiryOption valueOf = redis.clients.jedis.args.ExpiryOption
				.valueOf(expiryOption.name());
		return jc.expireAt(key, unixTime, valueOf);
	}

	@Override
	public long expireTime(byte[] key) {
		return jc.expireTime(key);
	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {
		return jc.keys(pattern);
	}

	@Override
	public String migrate(String host, int port, byte[] key, int timeout) {
		return jc.migrate(host, port, key, timeout);
	}

	@Override
	public String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
		redis.clients.jedis.params.MigrateParams migrateParams = JedisUtils.convertMigrateParams(params);
		return jc.migrate(host, port, timeout, migrateParams, keys);
	}

	@Override
	public byte[] objectEncoding(byte[] key) {
		return jc.objectEncoding(key);
	}

	@Override
	public Long objectFreq(byte[] key) {
		return jc.objectFreq(key);
	}

	@Override
	public Long objectIdletime(byte[] key) {
		return jc.objectIdletime(key);
	}

	@Override
	public Long objectRefcount(byte[] key) {
		return jc.objectRefcount(key);
	}

	@Override
	public long persist(byte[] key) {
		return jc.persist(key);
	}

	@Override
	public long pexpire(byte[] key, long milliseconds) {
		return jc.pexpire(key, milliseconds);
	}

	@Override
	public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
		redis.clients.jedis.args.ExpiryOption valueOf = JedisUtils.convertExpiryOption(expiryOption);
		return jc.pexpire(key, milliseconds, valueOf);
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return jc.pexpireAt(key, millisecondsTimestamp);
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
		redis.clients.jedis.args.ExpiryOption valueOf = JedisUtils.convertExpiryOption(expiryOption);
		return jc.pexpireAt(key, millisecondsTimestamp, valueOf);
	}

	@Override
	public long pexpireTime(byte[] key) {
		return jc.pexpireTime(key);
	}

	@Override
	public long pttl(byte[] key) {
		return jc.pttl(key);
	}

	@Override
	public byte[] randomBinaryKey() {
		return jc.randomBinaryKey();
	}

	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		return jc.rename(oldkey, newkey);
	}

	@Override
	public long renamenx(byte[] oldkey, byte[] newkey) {
		return jc.renamenx(oldkey, newkey);
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue) {
		return jc.restore(key, ttl, serializedValue);
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
		redis.clients.jedis.params.RestoreParams restoreParams = JedisUtils.convertRestoreParams(params);
		return jc.restore(key, ttl, serializedValue, restoreParams);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor) {
		ScanResult<byte[]> scanResult = jc.scan(cursor);
		return JedisUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params) {
		ScanResult<byte[]> scanResult = jc.scan(cursor, JedisUtils.convertScanParams(params));
		return JedisUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params, byte[] type) {
		ScanResult<byte[]> scanResult = jc.scan(cursor, JedisUtils.convertScanParams(params), type);
		return JedisUtils.convertKeyScanCursor(scanResult);
	}

	@Override
	public List<byte[]> sort(byte[] key) {
		return jc.sort(key);
	}

	@Override
	public List<byte[]> sort(byte[] key, SortArgs params) {
		return jc.sort(key, JedisUtils.convertSortingParams(params));
	}

	@Override
	public long sort(byte[] key, byte[] dstkey) {
		return jc.sort(key, dstkey);
	}

	@Override
	public long sort(byte[] key, SortArgs params, byte[] dstkey) {
		return jc.sort(key, JedisUtils.convertSortingParams(params), dstkey);
	}

	@Override
	public List<byte[]> sortReadonly(byte[] key, SortArgs params) {
		return jc.sortReadonly(key, JedisUtils.convertSortingParams(params));
	}

	@Override
	public long touch(byte[] key) {
		return jc.touch(key);
	}

	@Override
	public long touch(byte[]... keys) {
		return jc.touch(keys);
	}

	@Override
	public long ttl(byte[] key) {
		return jc.ttl(key);
	}

	@Override
	public String type(byte[] key) {
		return jc.type(key);
	}

	@Override
	public long unlink(byte[] key) {
		return jc.unlink(key);
	}

	@Override
	public long unlink(byte[]... keys) {
		return jc.unlink(keys);
	}

	@Override
	public Long memoryUsage(byte[] key) {
		return jc.memoryUsage(key);
	}

	@Override
	public Long memoryUsage(byte[] key, int samples) {
		return jc.memoryUsage(key, samples);
	}

	@Override
	public Long append(byte[] key, byte[] value) {
		return jc.append(key, value);
	}

	@Override
	public Long decr(byte[] key) {
		return jc.decr(key);
	}

	@Override
	public Long decrBy(byte[] key, long value) {
		return jc.decrBy(key, value);
	}

	@Override
	public byte[] get(byte[] key) {
		return jc.get(key);
	}

	@Override
	public byte[] getDel(byte[] key) {
		return jc.getDel(key);
	}

	@Override
	public byte[] getEx(byte[] key, GetExArgs params) {
		GetExParams getExParams = JedisUtils.convertGetExParams(params);
		return jc.getEx(key, getExParams);
	}

	@Override
	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return jc.getrange(key, startOffset, endOffset);
	}

	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		return jc.getSet(key, value);
	}

	@Override
	public Long incr(byte[] key) {
		return jc.incr(key);
	}

	@Override
	public Long incrBy(byte[] key, long increment) {
		return jc.incrBy(key, increment);
	}

	@Override
	public Double incrByFloat(byte[] key, double increment) {
		return jc.incrByFloat(key, increment);
	}

	@Override
	public LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params) {
		redis.clients.jedis.params.LCSParams lcsParams = JedisUtils.convertLCSParams(params);

		redis.clients.jedis.resps.LCSMatchResult lcsMatchResult = jc.lcs(keyA, keyB, lcsParams);

		return JedisUtils.convertLCSMatchResult(lcsMatchResult);
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		return jc.mget(keys);
	}

	@Override
	public String mset(byte[]... keysvalues) {
		return jc.mset(keysvalues);
	}

	@Override
	public Long msetnx(byte[]... keysvalues) {
		return jc.msetnx(keysvalues);
	}

	@Override
	public String psetex(byte[] key, long milliseconds, byte[] value) {
		return jc.psetex(key, milliseconds, value);
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return jc.set(key, value);
	}

	@Override
	public String setex(byte[] key, long seconds, byte[] value) {
		return jc.setex(key, seconds, value);
	}

	@Override
	public Long setnx(byte[] key, byte[] value) {
		return jc.setnx(key, value);
	}

	@Override
	public Long setrange(byte[] key, long offset, byte[] value) {
		return jc.setrange(key, offset, value);
	}

	@Override
	public Long strlen(byte[] key) {
		return jc.strlen(key);
	}

	@Override
	public byte[] substr(byte[] key, int start, int end) {
		return jc.substr(key, start, end);
	}

	@Override
	public Long hdel(byte[] key, byte[]... fields) {
		return jc.hdel(key, fields);
	}

	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		return jc.hexists(key, field);
	}

	@Override
	public byte[] hget(byte[] key, byte[] field) {
		return jc.hget(key, field);
	}

	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return jc.hgetAll(key);
	}

	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return jc.hincrBy(key, field, value);
	}

	@Override
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return jc.hincrByFloat(key, field, value);
	}

	@Override
	public Set<byte[]> hkeys(byte[] key) {
		return jc.hkeys(key);
	}

	@Override
	public Long hlen(byte[] key) {
		return jc.hlen(key);
	}

	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return jc.hmget(key, fields);
	}

	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return jc.hmset(key, hash);
	}

	@Override
	public byte[] hrandfield(byte[] key) {
		return jc.hrandfield(key);
	}

	@Override
	public List<byte[]> hrandfield(byte[] key, long count) {
		return jc.hrandfield(key, count);
	}

	@Override
	public Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
		return jc.hrandfieldWithValues(key, count);
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor) {
		ScanResult<Entry<byte[], byte[]>> scanResult = jc.hscan(key, cursor);
		return JedisUtils.convertMapScanCursor(scanResult);
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor, ScanArgs params) {
		ScanResult<Entry<byte[], byte[]>> scanResult = jc.hscan(key, cursor, JedisUtils.convertScanParams(params));
		return JedisUtils.convertMapScanCursor(scanResult);
	}

	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		return jc.hset(key, field, value);
	}

	@Override
	public Long hset(byte[] key, Map<byte[], byte[]> hash) {
		return jc.hset(key, hash);
	}

	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return jc.hsetnx(key, field, value);
	}

	@Override
	public Long hstrlen(byte[] key, byte[] field) {
		return jc.hstrlen(key, field);
	}

	@Override
	public List<byte[]> hvals(byte[] key) {
		return jc.hvals(key);
	}

	@Override
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, long timeout) {
		redis.clients.jedis.args.ListDirection f = redis.clients.jedis.args.ListDirection.valueOf(from.name());
		redis.clients.jedis.args.ListDirection t = redis.clients.jedis.args.ListDirection.valueOf(to.name());
		return jc.blmove(srcKey, dstKey, f, t, timeout);
	}

	@Override
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
		redis.clients.jedis.args.ListDirection f = redis.clients.jedis.args.ListDirection.valueOf(from.name());
		redis.clients.jedis.args.ListDirection t = redis.clients.jedis.args.ListDirection.valueOf(to.name());
		return jc.blmove(srcKey, dstKey, f, t, timeout);
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, byte[]... keys) {
		redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
		redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jc.blmpop(timeout, d, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, long count, byte[]... keys) {
		redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
		redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jc.blmpop(timeout, d, (int) count, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(long timeout, byte[]... keys) {
		List<byte[]> list = jc.blpop(timeout, keys);
		if (CollectionUtils.isEmpty(list)) {
			return new KeyValue<byte[], byte[]>(null, null);
		}
		return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(double timeout, byte[]... keys) {
		List<byte[]> list = jc.blpop(timeout, keys);
		if (CollectionUtils.isEmpty(list)) {
			return new KeyValue<byte[], byte[]>(null, null);
		}
		return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(long timeout, byte[]... keys) {
		List<byte[]> list = jc.brpop(timeout, keys);
		if (CollectionUtils.isEmpty(list)) {
			return new KeyValue<byte[], byte[]>(null, null);
		}
		return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(double timeout, byte[]... keys) {
		List<byte[]> list = jc.brpop(timeout, keys);
		if (CollectionUtils.isEmpty(list)) {
			return new KeyValue<byte[], byte[]>(null, null);
		}
		return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
	}

	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, long timeout) {
		return jc.brpoplpush(source, destination, (int) timeout);
	}

	@Override
	public byte[] lindex(byte[] key, long index) {
		return jc.lindex(key, index);
	}

	@Override
	public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
		redis.clients.jedis.args.ListPosition w = redis.clients.jedis.args.ListPosition.valueOf(where.name());
		return jc.linsert(key, w, pivot, value);
	}

	@Override
	public Long llen(byte[] key) {
		return jc.llen(key);
	}

	@Override
	public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
		redis.clients.jedis.args.ListDirection f = redis.clients.jedis.args.ListDirection.valueOf(from.name());
		redis.clients.jedis.args.ListDirection t = redis.clients.jedis.args.ListDirection.valueOf(to.name());
		return jc.lmove(srcKey, dstKey, f, t);
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys) {
		redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
		redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jc.lmpop(d, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, long count, byte[]... keys) {
		redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
		redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jc.lmpop(d, (int) count, keys);
		return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
	}

	@Override
	public byte[] lpop(byte[] key) {
		return jc.lpop(key);
	}

	@Override
	public List<byte[]> lpop(byte[] key, long count) {
		return jc.lpop(key, (int) count);
	}

	@Override
	public Long lpos(byte[] key, byte[] element) {
		return jc.lpos(key, element);
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, long count) {
		redis.clients.jedis.params.LPosParams lPosParams = redis.clients.jedis.params.LPosParams.lPosParams();
		return jc.lpos(key, element, lPosParams, count);
	}

	@Override
	public Long lpos(byte[] key, byte[] element, LPosParams params) {
		redis.clients.jedis.params.LPosParams lPosParams = JedisUtils.convertLPosParams(params);
		return jc.lpos(key, element, lPosParams);
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
		redis.clients.jedis.params.LPosParams lPosParams = JedisUtils.convertLPosParams(params);
		return jc.lpos(key, element, lPosParams, count);
	}

	@Override
	public Long lpush(byte[] key, byte[]... values) {
		return jc.lpush(key, values);
	}

	@Override
	public Long lpushx(byte[] key, byte[]... values) {
		return jc.lpushx(key, values);
	}

	@Override
	public List<byte[]> lrange(byte[] key, long start, long stop) {
		return jc.lrange(key, start, stop);
	}

	@Override
	public Long lrem(byte[] key, long count, byte[] value) {
		return jc.lrem(key, count, value);
	}

	@Override
	public String lset(byte[] key, long index, byte[] value) {
		return jc.lset(key, index, value);
	}

	@Override
	public String ltrim(byte[] key, long start, long stop) {
		return jc.ltrim(key, start, stop);
	}

	@Override
	public byte[] rpop(byte[] key) {
		return jc.rpop(key);
	}

	@Override
	public List<byte[]> rpop(byte[] key, long count) {
		return jc.rpop(key, (int) count);
	}

	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		return jc.rpoplpush(srckey, dstkey);
	}

	@Override
	public Long rpush(byte[] key, byte[]... values) {
		return jc.rpush(key, values);
	}

	@Override
	public Long rpushx(byte[] key, byte[]... values) {
		return jc.rpushx(key, values);
	}

	@Override
	public Long sadd(byte[] key, byte[]... members) {
		return jc.sadd(key, members);
	}

	@Override
	public Long scard(byte[] key) {
		return jc.scard(key);
	}

	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		return jc.sdiff(keys);
	}

	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		return jc.sdiffstore(dstkey, keys);
	}

	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		return jc.sinter(keys);
	}

	@Override
	public long sintercard(byte[]... keys) {
		return jc.sintercard(keys);
	}

	@Override
	public long sintercard(int limit, byte[]... keys) {
		return jc.sintercard(limit, keys);
	}

	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		return jc.sinterstore(dstkey, keys);
	}

	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		return jc.sismember(key, member);
	}

	@Override
	public Set<byte[]> smembers(byte[] key) {
		return jc.smembers(key);
	}

	@Override
	public List<Boolean> smismember(byte[] key, byte[]... members) {
		return jc.smismember(key, members);
	}

	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return jc.smove(srckey, dstkey, member);
	}

	@Override
	public byte[] spop(byte[] key) {
		return jc.spop(key);
	}

	@Override
	public Set<byte[]> spop(byte[] key, long count) {
		return jc.spop(key, count);
	}

	@Override
	public byte[] srandmember(byte[] key) {
		return jc.srandmember(key);
	}

	@Override
	public List<byte[]> srandmember(byte[] key, int count) {
		return jc.srandmember(key, count);
	}

	@Override
	public Long srem(byte[] key, byte[]... members) {
		return jc.srem(key, members);
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor) {
		ScanResult<byte[]> scanResult = jc.sscan(key, cursor);
		return JedisUtils.convertValueScanCursor(scanResult);
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor, ScanArgs params) {
		ScanResult<byte[]> scanResult = jc.sscan(key, cursor, JedisUtils.convertScanParams(params));
		return JedisUtils.convertValueScanCursor(scanResult);
	}

	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		return jc.sunion(keys);
	}

	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		return jc.sunionstore(dstkey, keys);
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzmpop(long timeout, SortedSetOption option, byte[]... keys) {
		redis.clients.jedis.util.KeyValue<byte[], List<Tuple>> kv = jc.bzmpop(timeout,
				redis.clients.jedis.args.SortedSetOption.valueOf(option.name()), keys);
		if (kv == null) {
			return null;
		}

		Assert.isTrue(kv.getValue().size() == 1, "bzmpop result size must eq 1");

		Tuple tuple = kv.getValue().get(0);
		return new KeyValue<byte[], ScoredValue<byte[]>>(kv.getKey(),
				new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement()));
	}

	@Override
	public KeyValue<byte[], List<ScoredValue<byte[]>>> bzmpop(long timeout, SortedSetOption option, int count,
			byte[]... keys) {
		redis.clients.jedis.util.KeyValue<byte[], List<Tuple>> kv = jc.bzmpop(timeout,
				redis.clients.jedis.args.SortedSetOption.valueOf(option.name()), count, keys);
		if (kv == null) {
			return null;
		}
		List<ScoredValue<byte[]>> list = kv.getValue().stream()
				.map(tuple -> new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement()))
				.collect(Collectors.toList());
		return new KeyValue<byte[], List<ScoredValue<byte[]>>>(kv.getKey(), list);
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzpopmax(double timeout, byte[]... keys) {
		List<byte[]> list = jc.bzpopmax(timeout, keys);
		if (org.springframework.util.CollectionUtils.isEmpty(list)) {
			return null;
		}

		Iterator<byte[]> iterator = list.iterator();
		byte[] key = iterator.next();
		byte[] value = iterator.next();
		byte[] score = iterator.next();
		return new KeyValue<byte[], ScoredValue<byte[]>>(key,
				new ScoredValue<byte[]>(Double.parseDouble(new String(score, StandardCharsets.UTF_8)), value));
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> bzpopmin(double timeout, byte[]... keys) {
		List<byte[]> list = jc.bzpopmin(timeout, keys);
		if (org.springframework.util.CollectionUtils.isEmpty(list)) {
			return null;
		}

		Iterator<byte[]> iterator = list.iterator();
		byte[] key = iterator.next();
		byte[] value = iterator.next();
		byte[] score = iterator.next();
		return new KeyValue<byte[], ScoredValue<byte[]>>(key,
				new ScoredValue<byte[]>(Double.parseDouble(new String(score, StandardCharsets.UTF_8)), value));
	}

	@Override
	public long zadd(byte[] key, double score, byte[] member) {
		return jc.zadd(key, score, member);
	}

	@Override
	public long zadd(byte[] key, double score, byte[] member, ZAddArgs params) {
		ZAddParams zAddParams = JedisUtils.convertZAddParams(params);
		return jc.zadd(key, score, member, zAddParams);
	}

	@Override
	public long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues) {
		Map<byte[], Double> scoreMembers = scoredValues.stream()
				.collect(Collectors.toMap(ScoredValue::getValue, ScoredValue::getScore, (a, b) -> a));
		return jc.zadd(key, scoreMembers);
	}

	@Override
	public long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues, ZAddArgs params) {
		Map<byte[], Double> scoreMembers = scoredValues.stream()
				.collect(Collectors.toMap(ScoredValue::getValue, ScoredValue::getScore, (a, b) -> a));
		ZAddParams zAddParams = JedisUtils.convertZAddParams(params);
		return jc.zadd(key, scoreMembers, zAddParams);
	}

	@Override
	public long zcard(byte[] key) {
		return jc.zcard(key);
	}

	@Override
	public long zcount(byte[] key, Range<? extends Number> range) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		return jc.zcount(key, min, max);
	}

	@Override
	public List<byte[]> zdiff(byte[]... keys) {
		Set<byte[]> set = jc.zdiff(keys);
		return new ArrayList<byte[]>(set);
	}

	@Override
	public List<ScoredValue<byte[]>> zdiffWithScores(byte[]... keys) {
		Set<Tuple> set = jc.zdiffWithScores(keys);
		return set.stream().map(one -> {
			return new ScoredValue<byte[]>(one.getScore(), one.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public long zdiffStore(byte[] dstkey, byte[]... keys) {
		return jc.zdiffStore(dstkey, keys);
	}

	@Override
	public double zincrby(byte[] key, double increment, byte[] member) {
		return jc.zincrby(key, increment, member);
	}

	@Override
	public List<byte[]> zinter(byte[]... keys) {
		ZParams zParams = new ZParams();
		Set<byte[]> set = jc.zinter(zParams, keys);
		return new ArrayList<byte[]>(set);
	}

	@Override
	public List<byte[]> zinter(ZAggregateArgs params, byte[]... keys) {
		ZParams zParams = JedisUtils.convertZParams(params);
		Set<byte[]> set = jc.zinter(zParams, keys);
		return new ArrayList<byte[]>(set);
	}

	@Override
	public List<ScoredValue<byte[]>> zinterWithScores(byte[]... keys) {
		ZParams zParams = new ZParams();
		Set<Tuple> set = jc.zinterWithScores(zParams, keys);
		return set.stream().map(one -> {
			return new ScoredValue<byte[]>(one.getScore(), one.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zinterWithScores(ZAggregateArgs params, byte[]... keys) {
		ZParams zParams = JedisUtils.convertZParams(params);
		Set<Tuple> set = jc.zinterWithScores(zParams, keys);
		return set.stream().map(one -> {
			return new ScoredValue<byte[]>(one.getScore(), one.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public long zinterstore(byte[] dstkey, byte[]... sets) {
		return jc.zinterstore(dstkey, sets);
	}

	@Override
	public long zinterstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets) {
		ZParams zParams = JedisUtils.convertZParams(params);
		return jc.zinterstore(dstkey, zParams, sets);
	}

	@Override
	public long zintercard(byte[]... keys) {
		return jc.zintercard(keys);
	}

	@Override
	public long zintercard(long limit, byte[]... keys) {
		return jc.zintercard(limit, keys);
	}

	@Override
	public long zlexcount(byte[] key, byte[] min, byte[] max) {
		return jc.zlexcount(key, min, max);
	}

	@Override
	public KeyValue<byte[], ScoredValue<byte[]>> zmpop(SortedSetOption option, byte[]... keys) {
		redis.clients.jedis.util.KeyValue<byte[], List<Tuple>> kv = jc
				.zmpop(redis.clients.jedis.args.SortedSetOption.valueOf(option.name()), keys);
		if (kv == null) {
			return null;
		}

		Tuple tuple = kv.getValue().get(0);

		ScoredValue<byte[]> value = new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		return new KeyValue<byte[], ScoredValue<byte[]>>(kv.getKey(), value);
	}

	@Override
	public KeyValue<byte[], List<ScoredValue<byte[]>>> zmpop(SortedSetOption option, int count, byte[]... keys) {
		redis.clients.jedis.util.KeyValue<byte[], List<Tuple>> kv = jc
				.zmpop(redis.clients.jedis.args.SortedSetOption.valueOf(option.name()), count, keys);
		if (kv == null) {
			return null;
		}

		List<ScoredValue<byte[]>> list = kv.getValue().stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());

		return new KeyValue<byte[], List<ScoredValue<byte[]>>>(kv.getKey(), list);
	}

	@Override
	public List<Double> zmscore(byte[] key, byte[]... members) {
		return jc.zmscore(key, members);
	}

	@Override
	public ScoredValue<byte[]> zpopmax(byte[] key) {
		Tuple tuple = jc.zpopmax(key);
		return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
	}

	@Override
	public List<ScoredValue<byte[]>> zpopmax(byte[] key, int count) {
		List<Tuple> list = jc.zpopmax(key, count);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public ScoredValue<byte[]> zpopmin(byte[] key) {
		Tuple tuple = jc.zpopmin(key);
		return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
	}

	@Override
	public List<ScoredValue<byte[]>> zpopmin(byte[] key, int count) {
		List<Tuple> list = jc.zpopmin(key, count);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public byte[] zrandmember(byte[] key) {
		return jc.zrandmember(key);
	}

	@Override
	public List<byte[]> zrandmember(byte[] key, long count) {
		return jc.zrandmember(key, count);
	}

	@Override
	public List<ScoredValue<byte[]>> zrandmemberWithScores(byte[] key, long count) {
		List<Tuple> list = jc.zrandmemberWithScores(key, count);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public List<byte[]> zrange(byte[] key, long start, long stop) {
		return jc.zrange(key, start, stop);
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeWithScores(byte[] key, long start, long stop) {
		List<Tuple> list = jc.zrangeWithScores(key, start, stop);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range) {
		return jc.zrangeByLex(key, range.getLower().getValue(), range.getUpper().getValue());
	}

	@Override
	public List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range, int offset, int count) {
		return jc.zrangeByLex(key, range.getLower().getValue(), range.getUpper().getValue(), offset, count);
	}

	@Override
	public List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		return jc.zrangeByScore(key, min, max);
	}

	@Override
	public List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		return jc.zrangeByScore(key, min, max, offset, count);
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		List<Tuple> list = jc.zrangeByScoreWithScores(key, min, max);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset,
			int count) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		List<Tuple> list = jc.zrangeByScoreWithScores(key, min, max, offset, count);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public long zrangestore(byte[] dest, byte[] src, Range<Long> range) {
		redis.clients.jedis.params.ZRangeParams zRangeParams = new redis.clients.jedis.params.ZRangeParams(
				range.getLower().getValue().intValue(), range.getUpper().getValue().intValue());
		return jc.zrangestore(dest, src, zRangeParams);
	}

	@Override
	public long zrangestoreByLex(byte[] dest, byte[] src, Range<byte[]> range, int offset, int count) {
		byte[] min = range.getLower().getValue();
		byte[] max = range.getUpper().getValue();

		redis.clients.jedis.params.ZRangeParams zRangeParams = new redis.clients.jedis.params.ZRangeParams(
				redis.clients.jedis.Protocol.Keyword.BYLEX, min, max);
		zRangeParams.limit(offset, count);
		return jc.zrangestore(dest, src, zRangeParams);
	}

	@Override
	public long zrangestoreByScore(byte[] dest, byte[] src, Range<? extends Number> range, int offset, int count) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();

		redis.clients.jedis.params.ZRangeParams zRangeParams = new redis.clients.jedis.params.ZRangeParams(
				redis.clients.jedis.Protocol.Keyword.BYSCORE, min, max);
		zRangeParams.limit(offset, count);
		return jc.zrangestore(dest, src, zRangeParams);
	}

	@Override
	public Long zrank(byte[] key, byte[] member) {
		return jc.zrank(key, member);
	}

	@Override
	public long zrem(byte[] key, byte[]... members) {
		return jc.zrem(key, members);
	}

	@Override
	public long zremrangeByLex(byte[] key, Range<byte[]> range) {
		return jc.zremrangeByLex(key, range.getLower().getValue(), range.getUpper().getValue());
	}

	@Override
	public long zremrangeByRank(byte[] key, long start, long stop) {
		return jc.zremrangeByRank(key, start, stop);
	}

	@Override
	public long zremrangeByScore(byte[] key, Range<? extends Number> range) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		return jc.zremrangeByScore(key, min, max);
	}

	@Override
	public List<byte[]> zrevrange(byte[] key, long start, long stop) {
		return jc.zrevrange(key, start, stop);
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeWithScores(byte[] key, long start, long stop) {
		List<Tuple> list = jc.zrevrangeWithScores(key, start, stop);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range) {
		return jc.zrevrangeByLex(key, range.getUpper().getValue(), range.getLower().getValue());
	}

	@Override
	public List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range, int offset, int count) {
		return jc.zrevrangeByLex(key, range.getUpper().getValue(), range.getLower().getValue(), offset, count);
	}

	@Override
	public List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		return jc.zrevrangeByScore(key, max, min);
	}

	@Override
	public List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		return jc.zrevrangeByScore(key, max, min, offset, count);
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		List<Tuple> list = jc.zrevrangeByScoreWithScores(key, max, min);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset,
			int count) {
		Tuple2<byte[], byte[]> tuple2 = JedisUtils.convertMinMax(range);
		byte[] min = tuple2.getT1();
		byte[] max = tuple2.getT2();
		List<Tuple> list = jc.zrevrangeByScoreWithScores(key, max, min, offset, count);
		return list.stream().map(tuple -> {
			return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
		}).collect(Collectors.toList());
	}

	@Override
	public Long zrevrank(byte[] key, byte[] member) {
		return jc.zrevrank(key, member);
	}

	@Override
	public ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor) {
		ScanResult<Tuple> scanResult = jc.zscan(key, cursor);
		return JedisUtils.convertScoredValueScanCursor(scanResult);
	}

	@Override
	public ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor, ScanArgs params) {
		ScanResult<Tuple> scanResult = jc.zscan(key, cursor, JedisUtils.convertScanParams(params));
		return JedisUtils.convertScoredValueScanCursor(scanResult);
	}

	@Override
	public Double zscore(byte[] key, byte[] member) {
		return jc.zscore(key, member);
	}

	@Override
	public List<byte[]> zunion(byte[]... keys) {
		ZParams zParams = new ZParams();
		return new ArrayList<>(jc.zunion(zParams, keys));
	}

	@Override
	public List<byte[]> zunion(ZAggregateArgs params, byte[]... keys) {
		ZParams zParams = JedisUtils.convertZParams(params);
		return new ArrayList<>(jc.zunion(zParams, keys));
	}

	@Override
	public List<ScoredValue<byte[]>> zunionWithScores(byte[]... keys) {
		ZParams zParams = new ZParams();
		Set<Tuple> set = jc.zunionWithScores(zParams, keys);
		return set.stream().map(one -> new ScoredValue<>(one.getScore(), one.getBinaryElement()))
				.collect(Collectors.toList());
	}

	@Override
	public List<ScoredValue<byte[]>> zunionWithScores(ZAggregateArgs params, byte[]... keys) {
		ZParams zParams = JedisUtils.convertZParams(params);
		Set<Tuple> set = jc.zunionWithScores(zParams, keys);
		return set.stream().map(one -> new ScoredValue<>(one.getScore(), one.getBinaryElement()))
				.collect(Collectors.toList());
	}

	@Override
	public long zunionstore(byte[] dstkey, byte[]... sets) {
		return jc.zunionstore(dstkey, sets);
	}

	@Override
	public long zunionstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets) {
		ZParams zParams = JedisUtils.convertZParams(params);
		return jc.zunionstore(dstkey, zParams, sets);
	}

	@Override
	public List<Object> eval(byte[] script) {
		Object obj = jc.eval(script);
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public List<Object> eval(byte[] script, int keyCount, byte[]... params) {
		Object obj = jc.eval(script, keyCount, params);
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public List<Object> eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		Object obj = jc.eval(script, keys, args);
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public List<Object> evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
		Object obj = jc.eval(script, keys, args);
		return EvalUtils.ofMultiReturnType(obj);
	}

	@Override
	public long bitcount(byte[] key) {
		return jc.bitcount(key);
	}

	@Override
	public long bitcount(byte[] key, long start, long end) {
		return jc.bitcount(key, start, end);
	}

	@Override
	public long bitcount(byte[] key, long start, long end, BitCountOption option) {
		redis.clients.jedis.args.BitCountOption valueOf = redis.clients.jedis.args.BitCountOption
				.valueOf(option.name());
		return jc.bitcount(key, start, end, valueOf);
	}

	@Override
	public List<Long> bitfield(byte[] key, BitFieldArgs args) {
		byte[][] bitFieldArgs = JedisUtils.convertBitFieldArgs(args);
		return jc.bitfield(key, bitFieldArgs);
	}

	@Override
	public List<Long> bitfieldReadonly(byte[] key, BitFieldArgs args) {
		byte[][] bitFieldArgs = JedisUtils.convertBitFieldArgs(args);
		return jc.bitfieldReadonly(key, bitFieldArgs);
	}

	@Override
	public long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
		redis.clients.jedis.args.BitOP valueOf = redis.clients.jedis.args.BitOP.valueOf(op.name());
		return jc.bitop(valueOf, destKey, srcKeys);
	}

	@Override
	public long bitpos(byte[] key, boolean value) {
		return jc.bitpos(key, value);
	}

	@Override
	public long bitpos(byte[] key, boolean value, BitPosParams params) {
		redis.clients.jedis.params.BitPosParams bitPosParams = JedisUtils.convertBitPosParams(params);
		return jc.bitpos(key, value, bitPosParams);
	}

	@Override
	public boolean getbit(byte[] key, long offset) {
		return jc.getbit(key, offset);
	}

	@Override
	public boolean setbit(byte[] key, long offset, boolean value) {
		return jc.setbit(key, offset, value);
	}

	@Override
	public long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
		return jc.geoadd(key, longitude, latitude, member);
	}

	@Override
	public long geoadd(byte[] key, double longitude, double latitude, byte[] member, GeoAddArgs args) {
		GeoAddParams geoAddParams = JedisUtils.convertGeoAddParams(args);
		redis.clients.jedis.GeoCoordinate geoCoordinate = new redis.clients.jedis.GeoCoordinate(longitude, latitude);
		Map<byte[], redis.clients.jedis.GeoCoordinate> map = new HashMap<>();
		map.put(member, geoCoordinate);
		return jc.geoadd(key, geoAddParams, map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long geoadd(byte[] key, GeoValue<byte[]>... geoValues) {
		Map<byte[], redis.clients.jedis.GeoCoordinate> map = new HashMap<>(geoValues.length, 1);
		for (GeoValue<byte[]> one : geoValues) {
			redis.clients.jedis.GeoCoordinate geoCoordinate = new redis.clients.jedis.GeoCoordinate(one.getLongitude(),
					one.getLatitude());
			map.put(one.getValue(), geoCoordinate);
		}
		return jc.geoadd(key, map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long geoadd(byte[] key, GeoAddArgs args, GeoValue<byte[]>... geoValues) {
		GeoAddParams geoAddParams = JedisUtils.convertGeoAddParams(args);

		Map<byte[], redis.clients.jedis.GeoCoordinate> map = new HashMap<>(geoValues.length, 1);
		for (GeoValue<byte[]> one : geoValues) {
			redis.clients.jedis.GeoCoordinate geoCoordinate = new redis.clients.jedis.GeoCoordinate(one.getLongitude(),
					one.getLatitude());
			map.put(one.getValue(), geoCoordinate);
		}
		return jc.geoadd(key, geoAddParams, map);
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2) {
		return jc.geodist(key, member1, member2);
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
		return jc.geodist(key, member1, member2, redis.clients.jedis.args.GeoUnit.valueOf(unit.name()));
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

//	@Override
//	public void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver) {
//		unsubscribeReceiver.accept(new Unsubscribe() {
//			@Override
//			public boolean isSubscribed() {
//				return jedisPubSub.isSubscribed();
//			}
//
//			@Override
//			public void unsubscribe(byte[]... channels) {
//				jedisPubSub.unsubscribe(channels);
//			}
//
//			@Override
//			public void unsubscribe() {
//				jedisPubSub.unsubscribe();
//				if (log.isInfoEnabled()) {
//					log.info(this.getClass().getSimpleName() + " unsubscribe channel:{}",
//							new String(channel, Charset.forName("utf-8")));
//				}
//			}
//		});
//		jc.subscribe(jedisPubSub, channel);
//	}

	@Override
	public void subscribe(byte[] channel, RedisPubSubListener<byte[], byte[]> listener) {
		new Thread("Jedis-Sub-" + new String(channel, StandardCharsets.UTF_8)) {
			@Override
			public void run() {
				BinaryJedisPubSub jedisPubSub = new BinaryJedisPubSub() {
					@Override
					public void onSubscribe(byte[] channel, int subscribedChannels) {
						listener.subscribed(channel, subscribedChannels);
					}

					@Override
					public void onUnsubscribe(byte[] channel, int subscribedChannels) {
						listener.unsubscribed(channel, subscribedChannels);
					}

					@Override
					public void onMessage(byte[] channel, byte[] message) {
						listener.message(channel, message);
					}
				};

				subMap.put(channel, jedisPubSub);

				jc.subscribe(jedisPubSub, channel);
			}
		}.start();
	}

	@Override
	public void unsubscribe(byte[] channel) {
		BinaryJedisPubSub jedisPubSub = subMap.get(channel);
		if (jedisPubSub != null) {
			jedisPubSub.unsubscribe();
		}
	}

	@Override
	public void publish(byte[] channel, byte[] message) {
		jc.publish(channel, message);
	}
}

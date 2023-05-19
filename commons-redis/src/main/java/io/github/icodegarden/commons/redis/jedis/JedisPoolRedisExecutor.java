package io.github.icodegarden.commons.redis.jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.CollectionUtils;

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
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.ValueScanCursor;
import io.github.icodegarden.commons.redis.util.EvalUtils;
import io.github.icodegarden.commons.redis.util.JedisUtils;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisPoolRedisExecutor implements RedisExecutor {

	private Map<byte[], BinaryJedisPubSub> subMap = new ConcurrentHashMap<byte[], BinaryJedisPubSub>();

	private JedisPool jedisPool;

	public JedisPoolRedisExecutor(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public JedisPoolRedisExecutor(JedisPoolConfig poolConfig, String host, int port, int timeout, String password,
			boolean ssl) {
		this.jedisPool = new JedisPool(poolConfig, host, port, timeout, password, ssl);
	}

	@Override
	public void close() throws IOException {
		jedisPool.close();
	}

	private <T> T execCommand(Command<T> co) {
		Jedis jedis = jedisPool.getResource();
		try {
			return co.exec(jedis);
		} finally {
			jedis.close();
		}
	}

	@FunctionalInterface
	private interface Command<T> {
		T exec(Jedis jedis);
	}

	@Override
	public Long append(byte[] key, byte[] value) {
		return execCommand(jedis -> jedis.append(key, value));
	}

	@Override
	public Long decr(byte[] key) {
		return execCommand(jedis -> jedis.decr(key));
	}

	@Override
	public Long decrBy(byte[] key, long value) {
		return execCommand(jedis -> jedis.decrBy(key, value));
	}

	@Override
	public byte[] get(byte[] key) {
		return execCommand(jedis -> jedis.get(key));
	}

	@Override
	public byte[] getDel(byte[] key) {
		return execCommand(jedis -> jedis.getDel(key));
	}

	@Override
	public byte[] getEx(byte[] key, GetExArgs params) {
		GetExParams getExParams = JedisUtils.convertGetExParams(params);
		return execCommand(jedis -> jedis.getEx(key, getExParams));
	}

	@Override
	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return execCommand(jedis -> jedis.getrange(key, startOffset, endOffset));
	}

	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		return execCommand(jedis -> jedis.getSet(key, value));
	}

	@Override
	public Long incr(byte[] key) {
		return execCommand(jedis -> jedis.incr(key));
	}

	@Override
	public Long incrBy(byte[] key, long increment) {
		return execCommand(jedis -> jedis.incrBy(key, increment));
	}

	@Override
	public Double incrByFloat(byte[] key, double increment) {
		return execCommand(jedis -> jedis.incrByFloat(key, increment));
	}

	@Override
	public LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params) {
		redis.clients.jedis.params.LCSParams lcsParams = JedisUtils.convertLCSParams(params);
		return execCommand(jedis -> {
			redis.clients.jedis.resps.LCSMatchResult lcsMatchResult = jedis.lcs(keyA, keyB, lcsParams);
			return JedisUtils.convertLCSMatchResult(lcsMatchResult);
		});
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		return execCommand(jedis -> jedis.mget(keys));
	}

	@Override
	public String mset(byte[]... keysvalues) {
		return execCommand(jedis -> jedis.mset(keysvalues));
	}

	@Override
	public Long msetnx(byte[]... keysvalues) {
		return execCommand(jedis -> jedis.msetnx(keysvalues));
	}

	@Override
	public String psetex(byte[] key, long milliseconds, byte[] value) {
		return execCommand(jedis -> jedis.psetex(key, milliseconds, value));
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return execCommand(jedis -> jedis.set(key, value));
	}

	@Override
	public String setex(byte[] key, long seconds, byte[] value) {
		return execCommand(jedis -> jedis.setex(key, seconds, value));
	}

	@Override
	public Long setnx(byte[] key, byte[] value) {
		return execCommand(jedis -> jedis.setnx(key, value));
	}

	@Override
	public Long setrange(byte[] key, long offset, byte[] value) {
		return execCommand(jedis -> jedis.setrange(key, offset, value));
	}

	@Override
	public Long strlen(byte[] key) {
		return execCommand(jedis -> jedis.strlen(key));
	}

	@Override
	public byte[] substr(byte[] key, int start, int end) {
		return execCommand(jedis -> jedis.substr(key, start, end));
	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {
		return execCommand(jedis -> jedis.keys(pattern));
	}

	@Override
	public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
		return execCommand(jedis -> jedis.copy(srcKey, dstKey, replace));
	}

	@Override
	public long del(byte[] key) {
		return execCommand(jedis -> jedis.del(key));
	}

	@Override
	public long del(byte[]... keys) {
		return execCommand(jedis -> jedis.del(keys));
	}

	@Override
	public byte[] dump(byte[] key) {
		return execCommand(jedis -> jedis.dump(key));
	}

	@Override
	public boolean exists(byte[] key) {
		return execCommand(jedis -> jedis.exists(key));
	}

	@Override
	public long exists(byte[]... keys) {
		return execCommand(jedis -> jedis.exists(keys));
	}

	@Override
	public long expire(byte[] key, long seconds) {
		return execCommand(jedis -> jedis.expire(key, seconds));
	}

	@Override
	public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
		return execCommand(jedis -> jedis.expire(key, seconds, JedisUtils.convertExpiryOption(expiryOption)));
	}

	@Override
	public long expireAt(byte[] key, long unixTime) {
		return execCommand(jedis -> jedis.expireAt(key, unixTime));
	}

	@Override
	public long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
		return execCommand(jedis -> jedis.expireAt(key, unixTime, JedisUtils.convertExpiryOption(expiryOption)));
	}

	@Override
	public long expireTime(byte[] key) {
		return execCommand(jedis -> jedis.expireTime(key));
	}

	@Override
	public String migrate(String host, int port, byte[] key, int timeout) {
		return execCommand(jedis -> jedis.migrate(host, port, key, timeout));
	}

	@Override
	public String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
		return execCommand(jedis -> jedis.migrate(host, port, timeout, JedisUtils.convertMigrateParams(params), keys));
	}

	@Override
	public byte[] objectEncoding(byte[] key) {
		return execCommand(jedis -> jedis.objectEncoding(key));
	}

	@Override
	public Long objectFreq(byte[] key) {
		return execCommand(jedis -> jedis.objectFreq(key));
	}

	@Override
	public Long objectIdletime(byte[] key) {
		return execCommand(jedis -> jedis.objectIdletime(key));
	}

	@Override
	public Long objectRefcount(byte[] key) {
		return execCommand(jedis -> jedis.objectRefcount(key));
	}

	@Override
	public long persist(byte[] key) {
		return execCommand(jedis -> jedis.persist(key));
	}

	@Override
	public long pexpire(byte[] key, long milliseconds) {
		return execCommand(jedis -> jedis.pexpire(key, milliseconds));
	}

	@Override
	public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
		return execCommand(jedis -> jedis.pexpire(key, milliseconds, JedisUtils.convertExpiryOption(expiryOption)));
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return execCommand(jedis -> jedis.pexpireAt(key, millisecondsTimestamp));
	}

	@Override
	public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
		return execCommand(
				jedis -> jedis.pexpireAt(key, millisecondsTimestamp, JedisUtils.convertExpiryOption(expiryOption)));
	}

	@Override
	public long pexpireTime(byte[] key) {
		return execCommand(jedis -> jedis.pexpireTime(key));
	}

	@Override
	public long pttl(byte[] key) {
		return execCommand(jedis -> jedis.pttl(key));
	}

	@Override
	public byte[] randomBinaryKey() {
		return execCommand(jedis -> jedis.randomBinaryKey());
	}

	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		return execCommand(jedis -> jedis.rename(oldkey, newkey));
	}

	@Override
	public long renamenx(byte[] oldkey, byte[] newkey) {
		return execCommand(jedis -> jedis.renamenx(oldkey, newkey));
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue) {
		return execCommand(jedis -> jedis.restore(key, ttl, serializedValue));
	}

	@Override
	public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
		return execCommand(jedis -> jedis.restore(key, ttl, serializedValue, JedisUtils.convertRestoreParams(params)));
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor) {
		return execCommand(jedis -> {
			ScanResult<byte[]> scanResult = jedis.scan(cursor);
			return JedisUtils.convertKeyScanCursor(scanResult);
		});
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params) {
		return execCommand(jedis -> {
			ScanResult<byte[]> scanResult = jedis.scan(cursor, JedisUtils.convertScanParams(params));
			return JedisUtils.convertKeyScanCursor(scanResult);
		});
	}

	@Override
	public KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params, byte[] type) {
		return execCommand(jedis -> {
			ScanResult<byte[]> scanResult = jedis.scan(cursor, JedisUtils.convertScanParams(params), type);
			return JedisUtils.convertKeyScanCursor(scanResult);
		});
	}

	@Override
	public List<byte[]> sort(byte[] key) {
		return execCommand(jedis -> jedis.sort(key));
	}

	@Override
	public List<byte[]> sort(byte[] key, SortArgs params) {
		return execCommand(jedis -> jedis.sort(key, JedisUtils.convertSortingParams(params)));
	}

	@Override
	public long sort(byte[] key, byte[] dstkey) {
		return execCommand(jedis -> jedis.sort(key, dstkey));
	}

	@Override
	public long sort(byte[] key, SortArgs params, byte[] dstkey) {
		return execCommand(jedis -> jedis.sort(key, JedisUtils.convertSortingParams(params), dstkey));
	}

	@Override
	public List<byte[]> sortReadonly(byte[] key, SortArgs params) {
		return execCommand(jedis -> jedis.sort(key, JedisUtils.convertSortingParams(params)));
	}

	@Override
	public long touch(byte[] key) {
		return execCommand(jedis -> jedis.touch(key));
	}

	@Override
	public long touch(byte[]... keys) {
		return execCommand(jedis -> jedis.touch(keys));
	}

	@Override
	public long ttl(byte[] key) {
		return execCommand(jedis -> jedis.ttl(key));
	}

	@Override
	public String type(byte[] key) {
		return execCommand(jedis -> jedis.type(key));
	}

	@Override
	public long unlink(byte[] key) {
		return execCommand(jedis -> jedis.unlink(key));
	}

	@Override
	public long unlink(byte[]... keys) {
		return execCommand(jedis -> jedis.unlink(keys));
	}

	@Override
	public Long memoryUsage(byte[] key) {
		return execCommand(jedis -> jedis.memoryUsage(key));
	}

	@Override
	public Long memoryUsage(byte[] key, int samples) {
		return execCommand(jedis -> jedis.memoryUsage(key, samples));
	}

	@Override
	public Long hdel(byte[] key, byte[]... fields) {
		return execCommand(jedis -> jedis.hdel(key, fields));
	}

	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		return execCommand(jedis -> jedis.hexists(key, field));
	}

	@Override
	public byte[] hget(byte[] key, byte[] field) {
		return execCommand(jedis -> jedis.hget(key, field));
	}

	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return execCommand(jedis -> jedis.hgetAll(key));
	}

	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return execCommand(jedis -> jedis.hincrBy(key, field, value));
	}

	@Override
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return execCommand(jedis -> jedis.hincrByFloat(key, field, value));
	}

	@Override
	public Set<byte[]> hkeys(byte[] key) {
		return execCommand(jedis -> jedis.hkeys(key));
	}

	@Override
	public Long hlen(byte[] key) {
		return execCommand(jedis -> jedis.hlen(key));
	}

	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return execCommand(jedis -> jedis.hmget(key, fields));
	}

	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return execCommand(jedis -> jedis.hmset(key, hash));
	}

	@Override
	public byte[] hrandfield(byte[] key) {
		return execCommand(jedis -> jedis.hrandfield(key));
	}

	@Override
	public List<byte[]> hrandfield(byte[] key, long count) {
		return execCommand(jedis -> jedis.hrandfield(key, count));
	}

	@Override
	public Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
		return execCommand(jedis -> jedis.hrandfieldWithValues(key, count));
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor) {
		return execCommand(jedis -> {
			ScanResult<Entry<byte[], byte[]>> scanResult = jedis.hscan(key, cursor);
			return JedisUtils.convertMapScanCursor(scanResult);
		});
	}

	@Override
	public MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor, ScanArgs params) {
		return execCommand(jedis -> {
			ScanResult<Entry<byte[], byte[]>> scanResult = jedis.hscan(key, cursor,
					JedisUtils.convertScanParams(params));
			return JedisUtils.convertMapScanCursor(scanResult);
		});
	}

	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		return execCommand(jedis -> jedis.hset(key, field, value));
	}

	@Override
	public Long hset(byte[] key, Map<byte[], byte[]> hash) {
		return execCommand(jedis -> jedis.hset(key, hash));
	}

	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return execCommand(jedis -> jedis.hsetnx(key, field, value));
	}

	@Override
	public Long hstrlen(byte[] key, byte[] field) {
		return execCommand(jedis -> jedis.hstrlen(key, field));
	}

	@Override
	public List<byte[]> hvals(byte[] key) {
		return execCommand(jedis -> jedis.hvals(key));
	}

	@Override
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, long timeout) {
		redis.clients.jedis.args.ListDirection f = redis.clients.jedis.args.ListDirection.valueOf(from.name());
		redis.clients.jedis.args.ListDirection t = redis.clients.jedis.args.ListDirection.valueOf(to.name());
		return execCommand(jedis -> jedis.blmove(srcKey, dstKey, f, t, timeout));
	}

	@Override
	public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
		redis.clients.jedis.args.ListDirection f = redis.clients.jedis.args.ListDirection.valueOf(from.name());
		redis.clients.jedis.args.ListDirection t = redis.clients.jedis.args.ListDirection.valueOf(to.name());
		return execCommand(jedis -> jedis.blmove(srcKey, dstKey, f, t, timeout));
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, byte[]... keys) {
		redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
		return execCommand(jedis -> {
			redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jedis.blmpop(timeout, d, keys);
			return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
		});
	}

	@Override
	public KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, long count, byte[]... keys) {
		redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
		return execCommand(jedis -> {
			redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jedis.blmpop(timeout, d, (int) count, keys);
			return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
		});
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(long timeout, byte[]... keys) {
		return execCommand(jedis -> {
			List<byte[]> list = jedis.blpop(timeout, keys);
			if (CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public KeyValue<byte[], byte[]> blpop(double timeout, byte[]... keys) {
		return execCommand(jedis -> {
			List<byte[]> list = jedis.blpop(timeout, keys);
			if (CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(long timeout, byte[]... keys) {
		return execCommand(jedis -> {
			List<byte[]> list = jedis.brpop(timeout, keys);
			if (CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public KeyValue<byte[], byte[]> brpop(double timeout, byte[]... keys) {
		return execCommand(jedis -> {
			List<byte[]> list = jedis.brpop(timeout, keys);
			if (CollectionUtils.isEmpty(list)) {
				return new KeyValue<byte[], byte[]>(null, null);
			}
			return new KeyValue<byte[], byte[]>(list.get(0), list.get(1));
		});
	}

	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, long timeout) {
		return execCommand(jedis -> {
			return jedis.brpoplpush(source, destination, (int) timeout);
		});
	}

	@Override
	public byte[] lindex(byte[] key, long index) {
		return execCommand(jedis -> {
			return jedis.lindex(key, index);
		});
	}

	@Override
	public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
		return execCommand(jedis -> {
			redis.clients.jedis.args.ListPosition w = redis.clients.jedis.args.ListPosition.valueOf(where.name());
			return jedis.linsert(key, w, pivot, value);
		});
	}

	@Override
	public Long llen(byte[] key) {
		return execCommand(jedis -> {
			return jedis.llen(key);
		});
	}

	@Override
	public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
		return execCommand(jedis -> {
			redis.clients.jedis.args.ListDirection f = redis.clients.jedis.args.ListDirection.valueOf(from.name());
			redis.clients.jedis.args.ListDirection t = redis.clients.jedis.args.ListDirection.valueOf(to.name());
			return jedis.lmove(srcKey, dstKey, f, t);
		});
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys) {
		return execCommand(jedis -> {
			redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
			redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jedis.lmpop(d, keys);
			return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
		});
	}

	@Override
	public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, long count, byte[]... keys) {
		return execCommand(jedis -> {
			redis.clients.jedis.args.ListDirection d = redis.clients.jedis.args.ListDirection.valueOf(direction.name());
			redis.clients.jedis.util.KeyValue<byte[], List<byte[]>> kv = jedis.lmpop(d, (int) count, keys);
			return new KeyValue<byte[], List<byte[]>>(kv.getKey(), kv.getValue());
		});
	}

	@Override
	public byte[] lpop(byte[] key) {
		return execCommand(jedis -> {
			return jedis.lpop(key);
		});
	}

	@Override
	public List<byte[]> lpop(byte[] key, long count) {
		return execCommand(jedis -> {
			return jedis.lpop(key, (int) count);
		});
	}

	@Override
	public Long lpos(byte[] key, byte[] element) {
		return execCommand(jedis -> {
			return jedis.lpos(key, element);
		});
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, long count) {
		return execCommand(jedis -> {
			redis.clients.jedis.params.LPosParams lPosParams = new redis.clients.jedis.params.LPosParams();
			return jedis.lpos(key, element, lPosParams, count);
		});
	}

	@Override
	public Long lpos(byte[] key, byte[] element, LPosParams params) {
		return execCommand(jedis -> {
			redis.clients.jedis.params.LPosParams lPosParams = JedisUtils.convertLPosParams(params);
			return jedis.lpos(key, element, lPosParams);
		});
	}

	@Override
	public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
		return execCommand(jedis -> {
			redis.clients.jedis.params.LPosParams lPosParams = JedisUtils.convertLPosParams(params);
			return jedis.lpos(key, element, lPosParams, count);
		});
	}

	@Override
	public Long lpush(byte[] key, byte[]... values) {
		return execCommand(jedis -> {
			return jedis.lpush(key, values);
		});
	}

	@Override
	public Long lpushx(byte[] key, byte[]... values) {
		return execCommand(jedis -> {
			return jedis.lpushx(key, values);
		});
	}

	@Override
	public List<byte[]> lrange(byte[] key, long start, long stop) {
		return execCommand(jedis -> {
			return jedis.lrange(key, start, stop);
		});
	}

	@Override
	public Long lrem(byte[] key, long count, byte[] value) {
		return execCommand(jedis -> {
			return jedis.lrem(key, count, value);
		});
	}

	@Override
	public String lset(byte[] key, long index, byte[] value) {
		return execCommand(jedis -> {
			return jedis.lset(key, index, value);
		});
	}

	@Override
	public String ltrim(byte[] key, long start, long stop) {
		return execCommand(jedis -> {
			return jedis.ltrim(key, start, stop);
		});
	}

	@Override
	public byte[] rpop(byte[] key) {
		return execCommand(jedis -> {
			return jedis.rpop(key);
		});
	}

	@Override
	public List<byte[]> rpop(byte[] key, long count) {
		return execCommand(jedis -> {
			return jedis.rpop(key, (int) count);
		});
	}

	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		return execCommand(jedis -> {
			return jedis.rpoplpush(srckey, dstkey);
		});
	}

	@Override
	public Long rpush(byte[] key, byte[]... values) {
		return execCommand(jedis -> {
			return jedis.rpush(key, values);
		});
	}

	@Override
	public Long rpushx(byte[] key, byte[]... values) {
		return execCommand(jedis -> {
			return jedis.rpushx(key, values);
		});
	}

	@Override
	public Long sadd(byte[] key, byte[]... members) {
		return execCommand(jedis -> {
			return jedis.sadd(key, members);
		});
	}

	@Override
	public Long scard(byte[] key) {
		return execCommand(jedis -> {
			return jedis.scard(key);
		});
	}

	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sdiff(keys);
		});
	}

	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sdiffstore(dstkey, keys);
		});
	}

	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sinter(keys);
		});
	}

	@Override
	public long sintercard(byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sintercard(keys);
		});
	}

	@Override
	public long sintercard(int limit, byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sintercard(limit, keys);
		});
	}

	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sinterstore(dstkey, keys);
		});
	}

	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		return execCommand(jedis -> {
			return jedis.sismember(key, member);
		});
	}

	@Override
	public Set<byte[]> smembers(byte[] key) {
		return execCommand(jedis -> {
			return jedis.smembers(key);
		});
	}

	@Override
	public List<Boolean> smismember(byte[] key, byte[]... members) {
		return execCommand(jedis -> {
			return jedis.smismember(key, members);
		});
	}

	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return execCommand(jedis -> {
			return jedis.smove(srckey, dstkey, member);
		});
	}

	@Override
	public byte[] spop(byte[] key) {
		return execCommand(jedis -> {
			return jedis.spop(key);
		});
	}

	@Override
	public Set<byte[]> spop(byte[] key, long count) {
		return execCommand(jedis -> {
			return jedis.spop(key, count);
		});
	}

	@Override
	public byte[] srandmember(byte[] key) {
		return execCommand(jedis -> {
			return jedis.srandmember(key);
		});
	}

	@Override
	public List<byte[]> srandmember(byte[] key, int count) {
		return execCommand(jedis -> {
			return jedis.srandmember(key, count);
		});
	}

	@Override
	public Long srem(byte[] key, byte[]... members) {
		return execCommand(jedis -> {
			return jedis.srem(key, members);
		});
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor) {
		return execCommand(jedis -> {
			ScanResult<byte[]> scanResult = jedis.sscan(key, cursor);
			return JedisUtils.convertValueScanCursor(scanResult);
		});
	}

	@Override
	public ValueScanCursor<byte[]> sscan(byte[] key, byte[] cursor, ScanArgs params) {
		return execCommand(jedis -> {
			ScanResult<byte[]> scanResult = jedis.sscan(key, cursor, JedisUtils.convertScanParams(params));
			return JedisUtils.convertValueScanCursor(scanResult);
		});
	}

	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sunion(keys);
		});
	}

	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		return execCommand(jedis -> {
			return jedis.sunionstore(dstkey, keys);
		});
	}

	@Override
	public List<Object> eval(byte[] script) {
		return execCommand(jedis -> {
			Object obj = jedis.eval(script);
			return EvalUtils.ofMultiReturnType(obj);
		});
	}

	@Override
	public List<Object> eval(byte[] script, int keyCount, byte[]... params) {
		return execCommand(jedis -> {
			Object obj = jedis.eval(script, keyCount, params);
			return EvalUtils.ofMultiReturnType(obj);
		});
	}

	@Override
	public List<Object> eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		return execCommand(jedis -> {
			Object obj = jedis.eval(script, keys, args);
			return EvalUtils.ofMultiReturnType(obj);
		});
	}

	@Override
	public List<Object> evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
		return execCommand(jedis -> {
			Object obj = jedis.eval(script, keys, args);
			return EvalUtils.ofMultiReturnType(obj);
		});
	}

//	@Override
//	public void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver) {
//		execCommand(jedis -> {
//			unsubscribeReceiver.accept(new Unsubscribe() {
//				@Override
//				public boolean isSubscribed() {
//					return jedisPubSub.isSubscribed();
//				}
//
//				@Override
//				public void unsubscribe(byte[]... channels) {
//					jedisPubSub.unsubscribe(channels);
//				}
//
//				@Override
//				public void unsubscribe() {
//					jedisPubSub.unsubscribe();
//					if (log.isInfoEnabled()) {
//						log.info(this.getClass().getSimpleName() + " unsubscribe channel:{}",
//								new String(channel, Charset.forName("utf-8")));
//					}
//				}
//			});
//
//			jedis.subscribe(jedisPubSub, channel);
//			return null;
//		});
//	}

	@Override
	public void subscribe(byte[] channel, RedisPubSubListener<byte[], byte[]> listener) {
		new Thread("Jedis-Sub-" + new String(channel, StandardCharsets.UTF_8)) {
			@Override
			public void run() {
				execCommand(jedis -> {
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

					jedis.subscribe(jedisPubSub, channel);
					return null;
				});
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
		execCommand(jedis -> {
			jedis.publish(channel, message);
			return null;
		});
	}

}
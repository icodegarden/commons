package io.github.icodegarden.commons.redis.jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
import io.github.icodegarden.commons.redis.util.EvalUtils;
import io.github.icodegarden.commons.redis.util.JedisUtils;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.resps.ScanResult;

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

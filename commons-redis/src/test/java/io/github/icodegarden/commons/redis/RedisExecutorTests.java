package io.github.icodegarden.commons.redis;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import io.github.icodegarden.commons.redis.args.Range;
import io.github.icodegarden.commons.redis.args.Range.Boundary;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.ScoredValue;
import io.github.icodegarden.commons.redis.args.ScoredValueScanCursor;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.SortedSetOption;
import io.github.icodegarden.commons.redis.args.ValueScanCursor;
import io.github.icodegarden.commons.redis.args.ZAddArgs;
import io.github.icodegarden.commons.redis.args.ZAggregateArgs;
import io.github.icodegarden.commons.redis.lettuce.AbstractLettuceRedisExecutor;
import io.github.icodegarden.commons.redis.spring.RedisTemplateRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class RedisExecutorTests {

	static byte[] key = "test{tag}key".getBytes();
	static byte[] k2 = "test{tag}key2".getBytes();

	RedisExecutor redisExecutor;

	@BeforeEach
	void init() {
		redisExecutor = newInstance();

		redisExecutor.del(key);
		redisExecutor.del(k2);
	}

	@AfterEach
	void end() throws IOException {
		redisExecutor.close();
	}

	protected abstract RedisExecutor newInstance();

	@Test
	public void append() throws Exception {
		Long l = redisExecutor.append(key, "Hello".getBytes());
		Assertions.assertThat(l).isEqualTo(5);
		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("Hello");

		l = redisExecutor.append(key, " World".getBytes());
		Assertions.assertThat(l).isEqualTo(11);

		bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("Hello World");
	}

	@Test
	public void decr() throws Exception {
		Long l = redisExecutor.decr(key);
		Assertions.assertThat(l).isEqualTo(-1);

		l = redisExecutor.decr(key);
		Assertions.assertThat(l).isEqualTo(-2);

		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("-2");
	}

	@Test
	public void decrBy() throws Exception {
		Long l = redisExecutor.decrBy(key, 10);
		Assertions.assertThat(l).isEqualTo(-10);

		l = redisExecutor.decrBy(key, 10);
		Assertions.assertThat(l).isEqualTo(-20);

		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("-20");
	}

	@Test
	public void get() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("abc");
	}

	@Test
	public void getDel() throws Exception {
		String set = redisExecutor.set(key, "abc".getBytes());
		Assertions.assertThat(set).isEqualTo("OK");

		byte[] bs = redisExecutor.getDel(key);
		Assertions.assertThat(new String(bs)).isEqualTo("abc");

		bs = redisExecutor.get(key);
		Assertions.assertThat(bs).isNull();
	}

	@Test
	public void getEx() throws Exception {
		long ttl = redisExecutor.ttl(key);
		Assertions.assertThat(ttl).isEqualTo(-2);

		redisExecutor.set(key, "abc".getBytes());
		ttl = redisExecutor.ttl(key);
		Assertions.assertThat(ttl).isEqualTo(-1);

		GetExArgs getExArgs = new GetExArgs();
		getExArgs.ex(60);
		byte[] bs = redisExecutor.getEx(key, getExArgs);
		Assertions.assertThat(new String(bs)).isEqualTo("abc");

		ttl = redisExecutor.ttl(key);
		Assertions.assertThat(ttl).isGreaterThan(0);
	}

	@Test
	public void getrange() throws Exception {
		String string = redisExecutor.set(key, "This is a string".getBytes());
		Assertions.assertThat(string).isEqualTo("OK");

		byte[] bs = redisExecutor.getrange(key, 0, 3);
		Assertions.assertThat(new String(bs)).isEqualTo("This");

		bs = redisExecutor.getrange(key, -3, -1);
		Assertions.assertThat(new String(bs)).isEqualTo("ing");

		bs = redisExecutor.getrange(key, 0, -1);
		Assertions.assertThat(new String(bs)).isEqualTo("This is a string");

		bs = redisExecutor.getrange(key, 10, 100);
		Assertions.assertThat(new String(bs)).isEqualTo("string");
	}

	@Test
	public void getSet() throws Exception {
		byte[] bs = redisExecutor.getSet(key, "abc".getBytes());
		Assertions.assertThat(bs).isNull();

		bs = redisExecutor.getSet(key, "ddd".getBytes());
		Assertions.assertThat(new String(bs)).isEqualTo("abc");

		bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("ddd");
	}

	@Test
	public void incr() throws Exception {
		Long l = redisExecutor.incr(key);
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.incr(key);
		Assertions.assertThat(l).isEqualTo(2);

		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("2");
	}

	@Test
	public void incrBy() throws Exception {
		Long l = redisExecutor.incrBy(key, 10);
		Assertions.assertThat(l).isEqualTo(10);

		l = redisExecutor.incrBy(key, 10);
		Assertions.assertThat(l).isEqualTo(20);

		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("20");
	}

	@Test
	public void incrByFloat() throws Exception {
		Double d = redisExecutor.incrByFloat(key, 10.01);
		Assertions.assertThat(d).isEqualTo(10.01);

		d = redisExecutor.incrByFloat(key, 10.01);
		Assertions.assertThat(d).isEqualTo(20.02);

		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(new String(bs)).isEqualTo("20.02");
	}

	@Test
	public void lcs() throws Exception {
		if (redisExecutor instanceof AbstractLettuceRedisExecutor) {
			return;
		}
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}

		String s = redisExecutor.mset(key, "ohmytext".getBytes(), k2, "mynewtext".getBytes());
		Assertions.assertThat(s).isEqualTo("OK");

		LCSParams lcsParams = new LCSParams();
		LCSMatchResult result = redisExecutor.lcs(key, k2, lcsParams);
		Assertions.assertThat(result.getMatchString()).isEqualTo("mytext");
	}

	@Test
	public void mget() throws Exception {
		byte[] k2 = "test{tag}key2".getBytes();

		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set(k2, "abcd".getBytes());

		List<byte[]> mget = redisExecutor.mget(key, "test{tag}key2".getBytes());

		Assertions.assertThat(mget).hasSize(2);
		Assertions.assertThat(new String(mget.get(0))).isEqualTo("abc");
		Assertions.assertThat(new String(mget.get(1))).isEqualTo("abcd");
	}

	@Test
	public void mset() throws Exception {
		byte[] k2 = "test{tag}key2".getBytes();

		String s = redisExecutor.mset(key, "abc".getBytes(), k2, "abcd".getBytes());
		Assertions.assertThat(s).isEqualTo("OK");

		List<byte[]> mget = redisExecutor.mget(key, "test{tag}key2".getBytes());

		Assertions.assertThat(mget).hasSize(2);
		Assertions.assertThat(new String(mget.get(0))).isEqualTo("abc");
		Assertions.assertThat(new String(mget.get(1))).isEqualTo("abcd");
	}

	@Test
	public void msetnx() throws Exception {
		Long l = redisExecutor.msetnx(key, "abc".getBytes());
		Assertions.assertThat(l).isEqualTo(1);

		byte[] k2 = "test{tag}key2".getBytes();
		l = redisExecutor.msetnx(key, "123".getBytes(), k2, "456".getBytes());
		Assertions.assertThat(l).isEqualTo(0);
	}

	@Test
	public void psetex() throws Exception {
		String s = redisExecutor.psetex(key, 1000, "abc".getBytes());
		Assertions.assertThat(s).isEqualTo("OK");

		long ms = redisExecutor.pttl(key);
		Assertions.assertThat(ms).isGreaterThan(0);
	}

	@Test
	public void set() throws Exception {
		String s = redisExecutor.set(key, "abc".getBytes());
		Assertions.assertThat(s).isEqualTo("OK");
	}

	@Test
	public void setex() throws Exception {
		redisExecutor.setex(key, 1, "abc".getBytes());
		byte[] bs = redisExecutor.get(key);
		Assertions.assertThat(bs).isNotNull();
	}

	@Test
	public void setnx() throws Exception {
		Long setnx = redisExecutor.setnx(key, "abc".getBytes());
		Assertions.assertThat(setnx).isEqualTo(1);

		setnx = redisExecutor.setnx(key, "abcd".getBytes());// key已存在
		Assertions.assertThat(setnx).isEqualTo(0);
	}

	@Test
	public void setrange() throws Exception {
		if (redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;// 因为RedisTemplateRedisExecutor的返回值不兼容，跳过测试
		}

		String s = redisExecutor.set(key, "Hello World".getBytes());
		Assertions.assertThat(s).isEqualTo("OK");

		Long l = redisExecutor.setrange(key, 6, "Redis".getBytes());
		if (!(redisExecutor instanceof RedisTemplateRedisExecutor)) {// 因为RedisTemplateRedisExecutor的返回值不兼容，跳过测试
			Assertions.assertThat(l).isEqualTo(11);
		}

		byte[] bs = redisExecutor.get(key);
		if (!(redisExecutor instanceof RedisTemplateRedisExecutor)) {// 因为RedisTemplateRedisExecutor的返回值不兼容，跳过测试
			Assertions.assertThat(new String(bs)).isEqualTo("Hello Redis");
		}

		redisExecutor.del(key);

		l = redisExecutor.setrange(key, 6, "Redis".getBytes());
		if (!(redisExecutor instanceof RedisTemplateRedisExecutor)) {// 因为RedisTemplateRedisExecutor的返回值不兼容，跳过测试
			Assertions.assertThat(l).isEqualTo(11);
		}

		bs = redisExecutor.get(key);// [0, 0, 0, 0, 0, 0, 82, 101, 100, 105, 115]
	}

	@Test
	public void strlen() throws Exception {
		redisExecutor.set(key, "Hello World".getBytes());
		Long l = redisExecutor.strlen(key);
		Assertions.assertThat(l).isEqualTo(11);

		l = redisExecutor.strlen("nonexisting".getBytes());
		Assertions.assertThat(l).isEqualTo(0);
	}

	@Deprecated
	@Test
	public void substr() throws Exception {
	}

	@Test
	public void copy() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set(k2, "abcd".getBytes());

		boolean b = redisExecutor.copy(key, k2, false);
		Assertions.assertThat(b).isEqualTo(false);

		byte[] bs = redisExecutor.get(k2);
		Assertions.assertThat(new String(bs)).isEqualTo("abcd");
		// ---------------------------------------------------

		b = redisExecutor.copy(key, k2, true);
		Assertions.assertThat(b).isEqualTo(true);

		bs = redisExecutor.get(k2);
		Assertions.assertThat(new String(bs)).isEqualTo("abc");
	}

	@Test
	public void del() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		Long del = redisExecutor.del(key);
		Assertions.assertThat(del).isEqualTo(1);

		// -------------------------------------------

		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set("test{tag}key2".getBytes(), "abcd".getBytes());

		del = redisExecutor.del(key, "test{tag}key2".getBytes());
		Assertions.assertThat(del).isEqualTo(2);
	}

	@Test
	public void dump() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		byte[] dump = redisExecutor.dump(key);
	}

	@Test
	public void exists() throws Exception {
		boolean b = redisExecutor.exists(key);
		Assertions.assertThat(b).isEqualTo(false);

		redisExecutor.set(key, "abc".getBytes());

		b = redisExecutor.exists(key);
		Assertions.assertThat(b).isEqualTo(true);
	}

	@Test
	public void expire() throws Exception {
		long l = redisExecutor.expire(key, 10);
		Assertions.assertThat(l).isEqualTo(0);

		redisExecutor.set(key, "abc".getBytes());

		l = redisExecutor.expire(key, 10);
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.expire(key, 10, ExpiryOption.XX);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void expireAt() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		long unixTime = System.currentTimeMillis() / 1000 + 10;// 单位 秒
		long l = redisExecutor.expireAt(key, unixTime);
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.ttl(key);

		Assertions.assertThat(l).isGreaterThan(0);
		Assertions.assertThat(l).isLessThanOrEqualTo(10);
	}

	@Test
	public void expireTime() throws Exception {
		if (redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;// 不支持
		}

		redisExecutor.set(key, "abc".getBytes());

		long unixTime = System.currentTimeMillis() / 1000 + 10;// 单位 秒
		redisExecutor.expireAt(key, unixTime);

		long expireTime = redisExecutor.expireTime(key);

		Assertions.assertThat(expireTime).isEqualTo(unixTime);
	}

	@Test
	public void keys() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set(k2, "abc".getBytes());

		Set<byte[]> keys = redisExecutor.keys("test{tag}*".getBytes());// 集群模式必须带tag

		Assertions.assertThat(keys.size()).isGreaterThan(0);
	}

	@Test
	public void migrate() throws Exception {
		// 不测了
//		redisExecutor.migrate(host, port, key, timeout);
	}

	@Test
	public void objectEncoding() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		byte[] bs = redisExecutor.objectEncoding(key);
		System.out.println(new String(bs));// embstr
	}

	@Test
	public void objectFreq() throws Exception {
//		redisExecutor.set(key, "abc".getBytes());
//		
//		Long l = redisExecutor.objectFreq(key);//要求设置数据淘汰策略是LFU
//		System.out.println(l);
//		
//		redisExecutor.get(key);
//		l = redisExecutor.objectFreq(key);
//		System.out.println(l);
//		
//		redisExecutor.get(key);
//		l = redisExecutor.objectFreq(key);
//		System.out.println(l);
	}

	@Test
	public void objectIdletime() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		Long l = redisExecutor.objectIdletime(key);// 要求不能是LFU
		System.out.println(l);

		redisExecutor.get(key);
		l = redisExecutor.objectIdletime(key);
		System.out.println(l);

		redisExecutor.get(key);
		l = redisExecutor.objectIdletime(key);
		System.out.println(l);
	}

	@Test
	public void objectRefcount() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		Long l = redisExecutor.objectRefcount(key);
		System.out.println(l);

		redisExecutor.get(key);
		l = redisExecutor.objectRefcount(key);
		System.out.println(l);

		redisExecutor.get(key);
		l = redisExecutor.objectRefcount(key);
		System.out.println(l);
	}

	@Test
	public void persist() throws Exception {
		redisExecutor.setex(key, 10, "abc".getBytes());

		long l = redisExecutor.persist(key);
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.ttl(key);
		Assertions.assertThat(l).isEqualTo(-1);
	}

	@Test
	public void pexpire() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		long l = redisExecutor.pexpire(key, 10 * 1000);
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.ttl(key);
		Assertions.assertThat(l).isGreaterThan(0);

		l = redisExecutor.pexpire(key, 10 * 1000, ExpiryOption.XX);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void pexpireAt() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		long millisecondsTimestamp = System.currentTimeMillis() + 10 * 1000;
		long l = redisExecutor.pexpireAt(key, millisecondsTimestamp);
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.ttl(key);
		Assertions.assertThat(l).isGreaterThan(0);

		l = redisExecutor.pexpireAt(key, millisecondsTimestamp, ExpiryOption.XX);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void pexpireTime() throws Exception {
		if (redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		redisExecutor.set(key, "abc".getBytes());

		long millisecondsTimestamp = System.currentTimeMillis() + 10 * 1000;
		redisExecutor.pexpireAt(key, millisecondsTimestamp);

		long pexpireTime = redisExecutor.pexpireTime(key);
		Assertions.assertThat(pexpireTime).isEqualTo(millisecondsTimestamp);
	}

	@Test
	public void pttl() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		long millisecondsTimestamp = System.currentTimeMillis() + 10 * 1000;
		redisExecutor.pexpireAt(key, millisecondsTimestamp);

		long l = redisExecutor.pttl(key);
		Assertions.assertThat(l).isGreaterThan(0);
	}

	@Test
	public void randomBinaryKey() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		byte[] bs = redisExecutor.randomBinaryKey();
		Assertions.assertThat(bs).isNotNull();
	}

	@Test
	public void rename() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.rename(key, k2);

		byte[] bs = redisExecutor.get(k2);
		Assertions.assertThat(new String(bs)).isEqualTo("abc");
	}

	@Test
	public void renamenx() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		long l = redisExecutor.renamenx(key, k2);

		Assertions.assertThat(l).isEqualTo(1);

		redisExecutor.set(key, "abc".getBytes());// rename后原key不存在了需要重新set
		l = redisExecutor.renamenx(key, k2);
		Assertions.assertThat(l).isEqualTo(0);
	}

	@Test
	public void restore() throws Exception {
	}

	@Test
	public void scan() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set(k2, "abc".getBytes());

		ScanArgs scanArgs = new ScanArgs();
		scanArgs.match("test{tag}*".getBytes());

		byte[] cursorbytes = "0".getBytes();
		KeyScanCursor<byte[]> cursor = null;
		do {
			cursor = redisExecutor.scan(cursorbytes, scanArgs, "string".getBytes());
			cursor.getKeys().forEach(bs -> {
				System.out.println(new String(bs));
			});

			cursorbytes = cursor.getCursor().getBytes();
		} while (!cursor.isFinished());
	}

	@Test
	public void sort() throws Exception {
		byte[][] arr1 = { "22".getBytes(), "11".getBytes(), "33".getBytes() };
		redisExecutor.rpush(key, arr1);

		List<byte[]> list = null;

		list = redisExecutor.sort(key);// 不支持，因为value不是数值
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[1]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr1[2]);

		list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr1[2]);
		// -------------------------------------------------------------
		redisExecutor.del(key);
		arr1 = new byte[][] { "weight_22".getBytes(), "weight_11".getBytes(), "weight_33".getBytes() };
		redisExecutor.rpush(key, arr1);

		SortArgs sortArgs = new SortArgs();
		sortArgs.asc();
		sortArgs.alpha();// 因为值不是数字，要按ascii排序就需要alpha
		list = redisExecutor.sort(key, sortArgs);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[1]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr1[2]);

		list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr1[2]);
		// -------------------------------------------------------------
		redisExecutor.del(key);
		arr1 = new byte[][] { "22".getBytes(), "11".getBytes(), "33".getBytes() };
		redisExecutor.rpush(key, arr1);

		long l = redisExecutor.sort(key, k2);
		Assertions.assertThat(l).isEqualTo(arr1.length);

		list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr1[2]);
		// -------------------------------------------------------------
		redisExecutor.del(key);
		arr1 = new byte[][] { "weight_22".getBytes(), "weight_11".getBytes(), "weight_33".getBytes() };
		redisExecutor.rpush(key, arr1);

		sortArgs = new SortArgs();
		sortArgs.limit(1, 10);// 倒排的结果中从1开始截取
		sortArgs.desc();
		sortArgs.alpha();// 因为值不是数字，要按ascii排序就需要alpha
		l = redisExecutor.sort(key, sortArgs, k2);
		Assertions.assertThat(l).isEqualTo(2);

		list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr1[2]);

		list = redisExecutor.lrange(k2, 0, -1);
		Assertions.assertThat(list.get(0)).isEqualTo("weight_22".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("weight_11".getBytes());
	}

	@Test
	public void sortRO() throws Exception {
		byte[][] arr1 = { "weight_22".getBytes(), "weight_11".getBytes(), "weight_33".getBytes() };
		redisExecutor.rpush(key, arr1);

		SortArgs sortArgs = new SortArgs();
		sortArgs.asc();
		sortArgs.alpha();// 因为值不是数字，要按ascii排序就需要alpha
		List<byte[]> list = redisExecutor.sortReadonly(key, sortArgs);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[1]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr1[2]);
		// -------------------------------------------------------------

		sortArgs = new SortArgs();
		sortArgs.limit(1, 10);// 倒排的结果中从1开始截取
		sortArgs.desc();
		sortArgs.alpha();// 因为值不是数字，要按ascii排序就需要alpha
		list = redisExecutor.sortReadonly(key, sortArgs);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);
		// -------------------------------------------------------------

		sortArgs = new SortArgs();
		sortArgs.by("weight_*".getBytes());// 有问题
		sortArgs.get("weight_*".getBytes());// 有问题
		sortArgs.limit(1, 10);// 倒排的结果中从1开始截取
		sortArgs.desc();
		sortArgs.alpha();// 因为值不是数字，要按ascii排序就需要alpha
//		list = redisExecutor.sortReadonly(key, sortArgs);
//		Assertions.assertThat(list.size()).isEqualTo(2);
//		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
//		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);
	}

	@Test
	public void touch() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set(k2, "abc".getBytes());

		long l = redisExecutor.touch(key, k2);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void ttl() throws Exception {
		// 不用单独测
	}

	@Test
	public void type() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		String s = redisExecutor.type(key);
		Assertions.assertThat(s).isEqualTo("string");
	}

	@Test
	public void unlink() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		long l = redisExecutor.unlink(key, k2);
		Assertions.assertThat(l).isEqualTo(1);

		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set(k2, "abc".getBytes());
		l = redisExecutor.unlink(key, k2);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Deprecated
	@Test
	public void memoryUsage() throws Exception {
//		redisExecutor.set(key, "abc".getBytes());
//		Long l = redisExecutor.memoryUsage(key);
//		Assertions.assertThat(l).isGreaterThan(0);
	}

	@Test
	public void hdel() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "1".getBytes());
		redisExecutor.hset(key, "b".getBytes(), "1".getBytes());
		redisExecutor.hset(key, "c".getBytes(), "1".getBytes());
		redisExecutor.hset(key, "d".getBytes(), "1".getBytes());

		Long l = redisExecutor.hdel(key, "nokey".getBytes());
		Assertions.assertThat(l).isEqualTo(0);

		l = redisExecutor.hdel(key, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.hdel(key, "b".getBytes(), "c".getBytes());
		Assertions.assertThat(l).isEqualTo(2);

		Map<byte[], byte[]> map = redisExecutor.hgetAll(key);
		Assertions.assertThat(map.size()).isEqualTo(1);
		Assertions.assertThat(Arrays.equals(map.keySet().iterator().next(), "d".getBytes())).isNotNull();
	}

	@Test
	public void hexists() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "1".getBytes());

		Boolean b = redisExecutor.hexists(key, "a".getBytes());
		Assertions.assertThat(b).isTrue();

		b = redisExecutor.hexists(key, "nokey".getBytes());
		Assertions.assertThat(b).isFalse();
	}

	@Test
	public void hget() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "1".getBytes());

		byte[] bs = redisExecutor.hget(key, "a".getBytes());
		Assertions.assertThat(new String(bs)).isEqualTo("1");

		bs = redisExecutor.hget(key, "nokey".getBytes());
		Assertions.assertThat(bs).isNull();
	}

	@Test
	public void hgetAll() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "1".getBytes());
		redisExecutor.hset(key, "b".getBytes(), "1".getBytes());

		Map<byte[], byte[]> map = redisExecutor.hgetAll(key);
		Assertions.assertThat(map.size()).isEqualTo(2);
	}

	@Test
	public void hincrBy() throws Exception {
		Long l = redisExecutor.hincrBy(key, "a".getBytes(), 10);
		Assertions.assertThat(l).isEqualTo(10);

		l = redisExecutor.hincrBy(key, "a".getBytes(), 10);
		Assertions.assertThat(l).isEqualTo(20);
	}

	@Test
	public void hincrByFloat() throws Exception {
		Double l = redisExecutor.hincrByFloat(key, "a".getBytes(), 10.01);
		Assertions.assertThat(l).isEqualTo(10.01);

		l = redisExecutor.hincrByFloat(key, "a".getBytes(), 10.01);
		Assertions.assertThat(l).isEqualTo(20.02);
	}

	@Test
	public void hkeys() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "1".getBytes());
		redisExecutor.hset(key, "b".getBytes(), "1".getBytes());

		Set<byte[]> set = redisExecutor.hkeys(key);
		Assertions.assertThat(set.size()).isEqualTo(2);
	}

	@Test
	public void hlen() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "1".getBytes());
		redisExecutor.hset(key, "b".getBytes(), "1".getBytes());

		Long l = redisExecutor.hlen(key);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void hmget() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "1".getBytes());
		redisExecutor.hset(key, "b".getBytes(), "2".getBytes());

		List<byte[]> list = redisExecutor.hmget(key, "a".getBytes(), "b".getBytes());
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(new String(list.get(0))).isEqualTo("1");
		Assertions.assertThat(new String(list.get(1))).isEqualTo("2");
	}

	@Test
	public void hmset() throws Exception {
		HashMap<byte[], byte[]> map = new HashMap<>();
		map.put("a".getBytes(), "1".getBytes());
		map.put("b".getBytes(), "1".getBytes());
		String s = redisExecutor.hmset(key, map);
		Assertions.assertThat(s).isEqualTo("OK");

		Map<byte[], byte[]> hgetAll = redisExecutor.hgetAll(key);
		Assertions.assertThat(hgetAll.size()).isEqualTo(2);
	}

	@Test
	public void hrandfield() throws Exception {
		HashMap<byte[], byte[]> map = new HashMap<>();
		map.put("a".getBytes(), "1".getBytes());
		map.put("b".getBytes(), "1".getBytes());
		map.put("c".getBytes(), "1".getBytes());
		map.put("d".getBytes(), "1".getBytes());
		redisExecutor.hset(key, map);

		byte[] field = redisExecutor.hrandfield(key);
		Assertions.assertThat(map.keySet().stream().anyMatch(k -> Arrays.equals(k, field))).isTrue();

		List<byte[]> list = redisExecutor.hrandfield(key, 2);
		Assertions.assertThat(list.size()).isEqualTo(2);

		list = redisExecutor.hrandfield(key, 4);
		Assertions.assertThat(list.size()).isEqualTo(4);
	}

	@Test
	public void hrandfieldWithValues() throws Exception {
		Map<byte[], byte[]> map = new HashMap<>();
		map.put("a".getBytes(), "1".getBytes());
		map.put("b".getBytes(), "1".getBytes());
		map.put("c".getBytes(), "1".getBytes());
		map.put("d".getBytes(), "1".getBytes());
		redisExecutor.hset(key, map);

		map = redisExecutor.hrandfieldWithValues(key, 2);
		Assertions.assertThat(map.size()).isEqualTo(2);
		Assertions.assertThat(new String(map.values().iterator().next())).isEqualTo("1");

		map = redisExecutor.hrandfieldWithValues(key, 4);
		Assertions.assertThat(map.size()).isEqualTo(4);
		Assertions.assertThat(new String(map.values().iterator().next())).isEqualTo("1");
	}

	@Test
	public void hscan() throws Exception {
		Map<byte[], byte[]> map = new HashMap<>();
		map.put("a".getBytes(), "1".getBytes());
		map.put("b".getBytes(), "1".getBytes());
		map.put("c".getBytes(), "1".getBytes());
		map.put("d".getBytes(), "1".getBytes());
		redisExecutor.hset(key, map);

		byte[] cursorbytes = "0".getBytes();
		MapScanCursor<byte[], byte[]> cursor = null;
		do {
			cursor = redisExecutor.hscan(key, cursorbytes);

			cursor.getMap().forEach((kbs, vbs) -> {
				System.out.println(new String(kbs) + ":" + new String(vbs));
			});

			cursorbytes = cursor.getCursor().getBytes();
		} while (!cursor.isFinished());

		// -----------------------------------------------------------------

		ScanArgs scanArgs = new ScanArgs();
		scanArgs.match("*".getBytes());

		cursorbytes = "0".getBytes();
		cursor = null;
		do {
			cursor = redisExecutor.hscan(key, cursorbytes, scanArgs);

			cursor.getMap().forEach((kbs, vbs) -> {
				System.out.println(new String(kbs) + ":" + new String(vbs));
			});

			cursorbytes = cursor.getCursor().getBytes();
		} while (!cursor.isFinished());
	}

	@Test
	public void hset() throws Exception {
		Map<byte[], byte[]> map = new HashMap<>();
		map.put("a".getBytes(), "1".getBytes());
		map.put("b".getBytes(), "1".getBytes());
		Long l = redisExecutor.hset(key, map);

		if (!(redisExecutor instanceof RedisTemplateRedisExecutor)) {
			Assertions.assertThat(l).isEqualTo(map.size());
		}
	}

	@Test
	public void hsetnx() throws Exception {
		Long l = redisExecutor.hsetnx(key, "a".getBytes(), "1".getBytes());
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.hsetnx(key, "a".getBytes(), "1".getBytes());
		Assertions.assertThat(l).isEqualTo(0);
	}

	@Test
	public void hstrlen() throws Exception {
		redisExecutor.hset(key, "a".getBytes(), "123456".getBytes());

		Long l = redisExecutor.hstrlen(key, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(6);
	}

	@Test
	public void hvals() throws Exception {
		Map<byte[], byte[]> map = new LinkedHashMap<>();
		map.put("a".getBytes(), "1".getBytes());
		map.put("b".getBytes(), "2".getBytes());
		redisExecutor.hset(key, map);

		List<byte[]> hvals = redisExecutor.hvals(key);
		Assertions.assertThat(hvals.size()).isEqualTo(2);
		Assertions.assertThat(new String(hvals.get(0))).isEqualTo("1");
		Assertions.assertThat(new String(hvals.get(1))).isEqualTo("2");
	}

	@Test
	public void blmove() throws Exception {
		redisExecutor.rpush(key, "a".getBytes(), "b".getBytes(), "c".getBytes());// 形成[a,b,c]

		byte[] bs = redisExecutor.blmove(key, k2, ListDirection.LEFT, ListDirection.LEFT, 0);// 左出左进
		Assertions.assertThat(new String(bs)).isEqualTo("a");

		List<byte[]> list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(new String(list.get(0))).isEqualTo("b");
		Assertions.assertThat(new String(list.get(1))).isEqualTo("c");

		list = redisExecutor.lrange(k2, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(new String(list.get(0))).isEqualTo("a");// 有了a
		// -------------------------------------------------------------------

		bs = redisExecutor.blmove(key, k2, ListDirection.RIGHT, ListDirection.RIGHT, 0);// you出you进
		Assertions.assertThat(new String(bs)).isEqualTo("c");

		list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(new String(list.get(0))).isEqualTo("b");

		list = redisExecutor.lrange(k2, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(new String(list.get(0))).isEqualTo("a");
		Assertions.assertThat(new String(list.get(1))).isEqualTo("c");// 有了c

		// -------------------------------------------------------------------
		bs = redisExecutor.blmove(key, k2, ListDirection.RIGHT, ListDirection.RIGHT, 0);// you出you进
		Assertions.assertThat(new String(bs)).isEqualTo("b");

		bs = redisExecutor.blmove(key, k2, ListDirection.RIGHT, ListDirection.RIGHT, 1);// 测试timeout>0
		Assertions.assertThat(bs).isNull();
	}

	@Test
	public void blmpop() throws Exception {
		if (redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}

		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "x".getBytes(), "y".getBytes(), "z".getBytes() };
		redisExecutor.rpush(key, arr1);
		redisExecutor.rpush(k2, arr2);

		KeyValue<byte[], List<byte[]>> kv = redisExecutor.blmpop(0, ListDirection.LEFT, key, k2);// 左边pop1个
		byte[] k = kv.getKey();
		List<byte[]> values = kv.getValue();
//		if(Arrays.equals(k,key)) {
		Assertions.assertThat(Arrays.equals(k, key)).isTrue();
		Assertions.assertThat(values.size()).isEqualTo(1);
		Assertions.assertThat(Arrays.equals(values.get(0), arr1[0])).isTrue();
//		}
//		if(Arrays.equals(k,k2)) {
//			Assertions.assertThat(values.size()).isEqualTo(1);
//			Assertions.assertThat(Arrays.equals(values.get(0), arr2[0])).isTrue();
//		}
		// ----------------------------------------------------------------------------------

		kv = redisExecutor.blmpop(0, ListDirection.RIGHT, 10, key, k2);// 右边pop最多10个
		k = kv.getKey();
		values = kv.getValue();
//		if(Arrays.equals(k,key)) {
		Assertions.assertThat(Arrays.equals(k, key)).isTrue();
		Assertions.assertThat(values.size()).isEqualTo(2);
		Assertions.assertThat(Arrays.equals(values.get(0), arr1[2])).isTrue();
		Assertions.assertThat(Arrays.equals(values.get(1), arr1[1])).isTrue();
//		}
//		if(Arrays.equals(k,k2)) {
//			Assertions.assertThat(values.size()).isEqualTo(2);
//			Assertions.assertThat(Arrays.equals(values.get(0), arr2[2])).isTrue();
//			Assertions.assertThat(Arrays.equals(values.get(1), arr2[1])).isTrue();
//		}
		// ----------------------------------------------------------------------------------

		kv = redisExecutor.blmpop(0, ListDirection.LEFT, 10, key, k2);// 测试只有k2了
		k = kv.getKey();
		values = kv.getValue();
		Assertions.assertThat(Arrays.equals(k, k2)).isTrue();
		Assertions.assertThat(values.size()).isEqualTo(3);
	}

	@Test
	public void blpop() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "x".getBytes(), "y".getBytes(), "z".getBytes() };
		redisExecutor.rpush(key, arr1);
		redisExecutor.rpush(k2, arr2);

		KeyValue<byte[], byte[]> kv = redisExecutor.blpop(0, key, k2);
		byte[] k = kv.getKey();
		byte[] value = kv.getValue();
		Assertions.assertThat(Arrays.equals(k, key)).isTrue();
		Assertions.assertThat(Arrays.equals(value, arr1[0])).isTrue();
	}

	@Test
	public void brpop() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "x".getBytes(), "y".getBytes(), "z".getBytes() };
		redisExecutor.rpush(key, arr1);
		redisExecutor.rpush(k2, arr2);

		KeyValue<byte[], byte[]> kv = redisExecutor.brpop(0, key, k2);
		byte[] k = kv.getKey();
		byte[] value = kv.getValue();
		Assertions.assertThat(Arrays.equals(k, key)).isTrue();
		Assertions.assertThat(Arrays.equals(value, arr1[2])).isTrue();
	}

	@Test
	public void brpoplpush() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "x".getBytes(), "y".getBytes(), "z".getBytes() };
		redisExecutor.rpush(key, arr1);
		redisExecutor.rpush(k2, arr2);

		redisExecutor.brpoplpush(key, k2, 0);

		List<byte[]> list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);

		list = redisExecutor.lrange(k2, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(4);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo(arr2[0]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr2[1]);
		Assertions.assertThat(list.get(3)).isEqualTo(arr2[2]);
	}

	@Test
	public void lindex() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.rpush(key, arr1);

		byte[] bs = redisExecutor.lindex(key, 0);
		Assertions.assertThat(bs).isEqualTo(arr1[0]);

		bs = redisExecutor.lindex(key, 1);
		Assertions.assertThat(bs).isEqualTo(arr1[1]);
	}

	@Test
	public void linsert() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.rpush(key, arr1);

		Long l = redisExecutor.linsert("nokey".getBytes(), ListPosition.BEFORE, "b".getBytes(), "d".getBytes());
		Assertions.assertThat(l).isEqualTo(0);// 0 key不存在

		l = redisExecutor.linsert(key, ListPosition.BEFORE, "bbb".getBytes(), "d".getBytes());
		Assertions.assertThat(l).isEqualTo(-1);// -1 pivot不存在

		l = redisExecutor.linsert(key, ListPosition.BEFORE, "b".getBytes(), "d".getBytes());
		Assertions.assertThat(l).isEqualTo(4);// >0 list的最终长度

		byte[] bs = redisExecutor.lindex(key, 1);
		Assertions.assertThat(bs).isEqualTo("d".getBytes());
	}

	@Test
	public void llen() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.rpush(key, arr1);

		Long l = redisExecutor.llen(key);
		Assertions.assertThat(l).isEqualTo(arr1.length);
	}

	@Test
	public void lmove() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.rpush(key, arr1);

		byte[] bs = redisExecutor.lmove(key, k2, ListDirection.RIGHT, ListDirection.LEFT);
		Assertions.assertThat(bs).isEqualTo("c".getBytes());
		List<byte[]> list = redisExecutor.lrange(k2, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());

		bs = redisExecutor.lmove(key, k2, ListDirection.RIGHT, ListDirection.LEFT);
		Assertions.assertThat(bs).isEqualTo("b".getBytes());
		list = redisExecutor.lrange(k2, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());
	}

	@Test
	public void lmpop() throws Exception {
		if (redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "x".getBytes(), "y".getBytes(), "z".getBytes() };
		redisExecutor.rpush(key, arr1);
		redisExecutor.rpush(k2, arr2);

		KeyValue<byte[], List<byte[]>> kv = redisExecutor.lmpop(ListDirection.LEFT, key, k2);// 左边pop1个
		byte[] k = kv.getKey();
		List<byte[]> values = kv.getValue();
//		if(Arrays.equals(k,key)) {
		Assertions.assertThat(Arrays.equals(k, key)).isTrue();
		Assertions.assertThat(values.size()).isEqualTo(1);
		Assertions.assertThat(Arrays.equals(values.get(0), arr1[0])).isTrue();
//		}
//		if(Arrays.equals(k,k2)) {
//			Assertions.assertThat(values.size()).isEqualTo(1);
//			Assertions.assertThat(Arrays.equals(values.get(0), arr2[0])).isTrue();
//		}
		// ----------------------------------------------------------------------------------

		kv = redisExecutor.lmpop(ListDirection.RIGHT, 10, key, k2);// 右边pop最多10个
		k = kv.getKey();
		values = kv.getValue();
//		if(Arrays.equals(k,key)) {
		Assertions.assertThat(Arrays.equals(k, key)).isTrue();
		Assertions.assertThat(values.size()).isEqualTo(2);
		Assertions.assertThat(Arrays.equals(values.get(0), arr1[2])).isTrue();
		Assertions.assertThat(Arrays.equals(values.get(1), arr1[1])).isTrue();
//		}
//		if(Arrays.equals(k,k2)) {
//			Assertions.assertThat(values.size()).isEqualTo(2);
//			Assertions.assertThat(Arrays.equals(values.get(0), arr2[2])).isTrue();
//			Assertions.assertThat(Arrays.equals(values.get(1), arr2[1])).isTrue();
//		}
		// ----------------------------------------------------------------------------------

		kv = redisExecutor.lmpop(ListDirection.LEFT, 10, key, k2);// 测试只有k2了
		k = kv.getKey();
		values = kv.getValue();
		Assertions.assertThat(Arrays.equals(k, k2)).isTrue();
		Assertions.assertThat(values.size()).isEqualTo(3);
	}

	@Test
	public void lpop() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.rpush(key, arr1);

		byte[] bs = redisExecutor.lpop(key);
		Assertions.assertThat(bs).isEqualTo("a".getBytes());

		List<byte[]> list = redisExecutor.lpop(key, 10);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());
	}

	@Test
	public void lpos() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes(), "a".getBytes() };
		redisExecutor.rpush(key, arr1);

		// ----------------------------------------------------------- 方法1
		Long l = redisExecutor.lpos(key, "no".getBytes());
		Assertions.assertThat(l).isNull();

		l = redisExecutor.lpos(key, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(0);
		// -----------------------------------------------------------方法2

		List<Long> list = redisExecutor.lpos(key, "no".getBytes(), 0);
		Assertions.assertThat(list.size()).isEqualTo(0);

		list = redisExecutor.lpos(key, "a".getBytes(), 0);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo(0);
		Assertions.assertThat(list.get(1)).isEqualTo(3);
		// -----------------------------------------------------------方法3

		LPosParams lPosParams = new LPosParams();
		lPosParams.rank(1);
		lPosParams.maxlen(1);
		l = redisExecutor.lpos(key, "no".getBytes(), lPosParams);
		Assertions.assertThat(l).isNull();

		l = redisExecutor.lpos(key, "a".getBytes(), lPosParams);
		Assertions.assertThat(l).isEqualTo(0);

		lPosParams = new LPosParams();
		lPosParams.rank(2);
		lPosParams.maxlen(0);
		l = redisExecutor.lpos(key, "a".getBytes(), lPosParams);
		Assertions.assertThat(l).isEqualTo(3);// 因为受rank maxlen影响
		// -----------------------------------------------------------方法4

		lPosParams = new LPosParams();
		lPosParams.rank(1);
		lPosParams.maxlen(1);
		list = redisExecutor.lpos(key, "no".getBytes(), lPosParams, 0);
		Assertions.assertThat(list.size()).isEqualTo(0);

		list = redisExecutor.lpos(key, "a".getBytes(), lPosParams, 0);
		Assertions.assertThat(list.size()).isEqualTo(1);// 因为受rank maxlen影响

		lPosParams.rank(1);
		lPosParams.maxlen(0);
		list = redisExecutor.lpos(key, "a".getBytes(), lPosParams, 0);
		Assertions.assertThat(list.size()).isEqualTo(2);// 因为受rank maxlen影响

		lPosParams.rank(2);
		lPosParams.maxlen(0);
		list = redisExecutor.lpos(key, "a".getBytes(), lPosParams, 0);
		Assertions.assertThat(l).isEqualTo(3);// 因为受rank maxlen影响
	}

	@Test
	public void lpush() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		Long l = redisExecutor.lpush(key, arr1);
		Assertions.assertThat(l).isEqualTo(arr1.length);
	}

	@Test
	public void lpushx() throws Exception {
		if (redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		Long l = redisExecutor.lpushx(key, arr1);
		Assertions.assertThat(l).isEqualTo(0);

		redisExecutor.lpush(key, "z".getBytes());
		l = redisExecutor.lpushx(key, arr1);
		Assertions.assertThat(l).isEqualTo(4);
	}

	@Test
	public void lrange() throws Exception {
		// 不需要单独测
	}

	@Test
	public void lrem() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "a".getBytes(), "a".getBytes() };
		redisExecutor.lpush(key, arr1);

		Long l = redisExecutor.lrem(key, 1, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(1);
		List<byte[]> list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(2);

		l = redisExecutor.lrem(key, 0, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(2);
		list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(0);
	}

	@Test
	public void lset() throws Exception {
		Assertions.assertThatExceptionOfType(Exception.class)
				.isThrownBy(() -> redisExecutor.lset(key, 0, "a".getBytes()));// key必须存在

		byte[][] arr1 = { "a".getBytes(), "a".getBytes(), "a".getBytes() };
		redisExecutor.lpush(key, arr1);

		String s = redisExecutor.lset(key, 0, "b".getBytes());
		Assertions.assertThat(s).isEqualTo("OK");

		List<byte[]> list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
	}

	@Test
	public void ltrim() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.rpush(key, arr1);

		redisExecutor.ltrim(key, 1, -1);// 只保留1 - -1

		List<byte[]> list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());
	}

	@Test
	public void rpop() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.rpush(key, arr1);

		byte[] bs = redisExecutor.rpop(key);
		Assertions.assertThat(bs).isEqualTo("c".getBytes());

		List<byte[]> list = redisExecutor.rpop(key, 10);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("a".getBytes());
	}

	@Test
	public void rpoplpush() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "x".getBytes(), "y".getBytes(), "z".getBytes() };
		redisExecutor.rpush(key, arr1);
		redisExecutor.rpush(k2, arr2);

		redisExecutor.rpoplpush(key, k2);

		List<byte[]> list = redisExecutor.lrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo(arr1[0]);
		Assertions.assertThat(list.get(1)).isEqualTo(arr1[1]);

		list = redisExecutor.lrange(k2, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(4);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo(arr2[0]);
		Assertions.assertThat(list.get(2)).isEqualTo(arr2[1]);
		Assertions.assertThat(list.get(3)).isEqualTo(arr2[2]);
	}

	@Test
	public void rpush() throws Exception {
		// 不需要单独测
	}

	@Test
	public void rpushx() throws Exception {
		if (redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		Long l = redisExecutor.rpushx(key, arr1);
		Assertions.assertThat(l).isEqualTo(0);

		redisExecutor.rpush(key, "z".getBytes());
		l = redisExecutor.rpushx(key, arr1);
		Assertions.assertThat(l).isEqualTo(4);
	}

	@Test
	public void sadd() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		Long l = redisExecutor.sadd(key, arr1);
		Assertions.assertThat(l).isEqualTo(arr1.length);

		l = redisExecutor.scard(key);
		Assertions.assertThat(l).isEqualTo(arr1.length);
	}

	@Test
	public void scard() throws Exception {
		// 不需要单独测
	}

	@Test
	public void sdiff() throws Exception {
		byte[] k3 = "test{tag}key3".getBytes();
		redisExecutor.del(k3);

		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "c".getBytes(), "d".getBytes(), "e".getBytes() };
		byte[][] arr3 = { "f".getBytes(), "b".getBytes() };
		redisExecutor.sadd(key, arr1);
		redisExecutor.sadd(k2, arr2);
		redisExecutor.sadd(k3, arr3);

		Set<byte[]> set = redisExecutor.sdiff(key, k2);
		Assertions.assertThat(set.size()).isEqualTo(2);
		Assertions.assertThat(set).containsAll(Arrays.asList("a".getBytes(), "b".getBytes()));// 是key对k2的差集

		set = redisExecutor.sdiff(key, k2, k3);
		Assertions.assertThat(set.size()).isEqualTo(1);
		Assertions.assertThat(set).containsAll(Arrays.asList("a".getBytes()));// 是key对k2、k3的差集
	}

	@Test
	public void sdiffstore() throws Exception {
		byte[] k3 = "test{tag}key3".getBytes();
		redisExecutor.del(k3);

		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "c".getBytes(), "d".getBytes(), "e".getBytes() };
		redisExecutor.sadd(key, arr1);
		redisExecutor.sadd(k2, arr2);

		Long l = redisExecutor.sdiffstore(k3, key, k2);
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.scard(k3);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void sinter() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "b".getBytes(), "d".getBytes(), "e".getBytes() };
		redisExecutor.sadd(key, arr1);
		redisExecutor.sadd(k2, arr2);

		Set<byte[]> set = redisExecutor.sinter(key, k2);
		Assertions.assertThat(set.size()).isEqualTo(1);
		Assertions.assertThat(set).containsAll(Arrays.asList("b".getBytes()));
	}

	@Test
	public void sintercard() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "b".getBytes(), "c".getBytes(), "d".getBytes() };
		redisExecutor.sadd(key, arr1);
		redisExecutor.sadd(k2, arr2);

		long l = redisExecutor.sintercard(key, k2);
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.sintercard(1, key, k2);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void sinterstore() throws Exception {
		byte[] k3 = "test{tag}key3".getBytes();
		redisExecutor.del(k3);

		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "b".getBytes(), "c".getBytes(), "d".getBytes() };
		redisExecutor.sadd(key, arr1);
		redisExecutor.sadd(k2, arr2);

		long l = redisExecutor.sinterstore(k3, key, k2);
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.scard(k3);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void sismember() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.sadd(key, arr1);

		Boolean b = redisExecutor.sismember(key, "b".getBytes());
		Assertions.assertThat(b).isTrue();

		b = redisExecutor.sismember(key, "d".getBytes());
		Assertions.assertThat(b).isFalse();
	}

	@Test
	public void smembers() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.sadd(key, arr1);

		Set<byte[]> set = redisExecutor.smembers(key);
		Assertions.assertThat(set.size()).isEqualTo(3);
	}

	@Test
	public void smismember() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.sadd(key, arr1);

		List<Boolean> list = redisExecutor.smismember(key, "b".getBytes(), "c".getBytes(), "d".getBytes());
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0)).isTrue();
		Assertions.assertThat(list.get(1)).isTrue();
		Assertions.assertThat(list.get(2)).isFalse();
	}

	@Test
	public void smove() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.sadd(key, arr1);

		Long l = redisExecutor.smove(key, k2, "b".getBytes());
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.scard(key);
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.scard(k2);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void spop() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.sadd(key, arr1);

		byte[] bs = redisExecutor.spop(key);
		Assertions.assertThat(bs).isNotNull();

		Long l = redisExecutor.scard(key);
		Assertions.assertThat(l).isEqualTo(2);

		Set<byte[]> set = redisExecutor.spop(key, 10);
		Assertions.assertThat(set.size()).isEqualTo(2);

		l = redisExecutor.scard(key);
		Assertions.assertThat(l).isEqualTo(0);
	}

	@Test
	public void srandmember() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.sadd(key, arr1);

		byte[] bs = redisExecutor.srandmember(key);
		Assertions.assertThat(bs).isNotNull();

		Long l = redisExecutor.scard(key);
		Assertions.assertThat(l).isEqualTo(arr1.length);

		List<byte[]> list = redisExecutor.srandmember(key, 10);
		Assertions.assertThat(list.size()).isEqualTo(arr1.length);

		l = redisExecutor.scard(key);
		Assertions.assertThat(l).isEqualTo(arr1.length);
	}

	@Test
	public void srem() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		redisExecutor.sadd(key, arr1);

		Long l = redisExecutor.srem(key, "b".getBytes(), "c".getBytes(), "z".getBytes());
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.scard(key);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void sscan() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes(), "d".getBytes() };
		redisExecutor.sadd(key, arr1);

		byte[] cursorbytes = "0".getBytes();
		ValueScanCursor<byte[]> cursor = null;
		do {
			cursor = redisExecutor.sscan(key, cursorbytes);

			cursor.getValues().forEach(bs -> {
				System.out.println(new String(bs));
			});

			cursorbytes = cursor.getCursor().getBytes();
		} while (!cursor.isFinished());

		// -----------------------------------------------------------------

		ScanArgs scanArgs = new ScanArgs();
		scanArgs.match("*".getBytes());

		cursorbytes = "0".getBytes();
		cursor = null;
		do {
			cursor = redisExecutor.sscan(key, cursorbytes, scanArgs);

			cursor.getValues().forEach(bs -> {
				System.out.println(new String(bs));
			});

			cursorbytes = cursor.getCursor().getBytes();
		} while (!cursor.isFinished());
	}

	@Test
	public void sunion() throws Exception {
		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "a".getBytes(), "x".getBytes(), "y".getBytes() };
		redisExecutor.sadd(key, arr1);
		redisExecutor.sadd(k2, arr2);

		Set<byte[]> set = redisExecutor.sunion(key, k2);
		Assertions.assertThat(set.size()).isEqualTo(5);
	}

	@Test
	public void sunionstore() throws Exception {
		byte[] k3 = "test{tag}key3".getBytes();
		redisExecutor.del(k3);

		byte[][] arr1 = { "a".getBytes(), "b".getBytes(), "c".getBytes() };
		byte[][] arr2 = { "a".getBytes(), "x".getBytes(), "y".getBytes() };
		redisExecutor.sadd(key, arr1);
		redisExecutor.sadd(k2, arr2);

		Long l = redisExecutor.sunionstore(k3, key, k2);
		Assertions.assertThat(l).isEqualTo(5);

		l = redisExecutor.scard(k3);
		Assertions.assertThat(l).isEqualTo(5);
	}

	@Test
	public void bzmpop() throws Exception {
		if (redisExecutor instanceof AbstractLettuceRedisExecutor) {
			return;
		}
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}

		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(k2, 0.1, "c".getBytes());
		redisExecutor.zadd(k2, 0.2, "d".getBytes());

		/**
		 * 它总是从key的顺序中取，而不是多个key中的最优先
		 */
		KeyValue<byte[], ScoredValue<byte[]>> kv = redisExecutor.bzmpop(0, SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(1.1);
		// -----------------------------------------------------------
		kv = redisExecutor.bzmpop(0, SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(2.2);
		// -----------------------------------------------------------
		kv = redisExecutor.bzmpop(0, SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.1);
		// -----------------------------------------------------------
		kv = redisExecutor.bzmpop(0, SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.2);

		// -----------------------------------------------------------
		// -----------------------------------------------------------
		// -----------------------------------------------------------

		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(k2, 0.1, "c".getBytes());
		redisExecutor.zadd(k2, 0.2, "d".getBytes());

		KeyValue<byte[], List<ScoredValue<byte[]>>> bzmpop = redisExecutor.bzmpop(0, SortedSetOption.MAX, 10, key, k2);
		Assertions.assertThat(bzmpop.getKey()).isEqualTo(key);
		Assertions.assertThat(bzmpop.getValue().size()).isEqualTo(2);
		Assertions.assertThat(bzmpop.getValue().get(0).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(0).getScore()).isEqualTo(2.2);
		Assertions.assertThat(bzmpop.getValue().get(1).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(1).getScore()).isEqualTo(1.1);
		// -----------------------------------------------------------
		bzmpop = redisExecutor.bzmpop(0, SortedSetOption.MAX, 10, key, k2);
		Assertions.assertThat(bzmpop.getKey()).isEqualTo(k2);
		Assertions.assertThat(bzmpop.getValue().size()).isEqualTo(2);
		Assertions.assertThat(bzmpop.getValue().get(0).getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(0).getScore()).isEqualTo(0.2);
		Assertions.assertThat(bzmpop.getValue().get(1).getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(1).getScore()).isEqualTo(0.1);
	}

	@Test
	public void bzpopmax() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(k2, 0.1, "c".getBytes());
		redisExecutor.zadd(k2, 0.2, "d".getBytes());

		/**
		 * 它总是从key的顺序中取，而不是多个key中的最优先
		 */
		KeyValue<byte[], ScoredValue<byte[]>> kv = redisExecutor.bzpopmax(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(2.2);
		// -----------------------------------------------------------
		kv = redisExecutor.bzpopmax(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(1.1);
		// -----------------------------------------------------------
		kv = redisExecutor.bzpopmax(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.2);
		// -----------------------------------------------------------
		kv = redisExecutor.bzpopmax(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.1);
	}

	@Test
	public void bzpopmin() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(k2, 0.1, "c".getBytes());
		redisExecutor.zadd(k2, 0.2, "d".getBytes());

		/**
		 * 它总是从key的顺序中取，而不是多个key中的最优先
		 */
		KeyValue<byte[], ScoredValue<byte[]>> kv = redisExecutor.bzpopmin(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(1.1);
		// -----------------------------------------------------------
		kv = redisExecutor.bzpopmin(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(2.2);
		// -----------------------------------------------------------
		kv = redisExecutor.bzpopmin(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.1);
		// -----------------------------------------------------------
		kv = redisExecutor.bzpopmin(0, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.2);
	}

	@Test
	public void zadd() throws Exception {
		long l = redisExecutor.zadd(key, 1.1, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(1);
		l = redisExecutor.zadd(key, 11.11, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(0);// score会变但返回是0
		Double d = redisExecutor.zscore(key, "a".getBytes());
		Assertions.assertThat(d).isEqualTo(11.11);

		ZAddArgs zAddArgs = new ZAddArgs();
		zAddArgs.xx();
		l = redisExecutor.zadd(key, 11.11, "x".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(0);// xx要求元素已存在
		d = redisExecutor.zscore(key, "x".getBytes());
		Assertions.assertThat(d).isNull();// 元素不会被add
		l = redisExecutor.zadd(key, 11.11, "a".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(0);// 虽然返回0但是score变了
		d = redisExecutor.zscore(key, "a".getBytes());
		Assertions.assertThat(d).isEqualTo(11.11);// score不变

		// --------------------------------------------------------------

		zAddArgs = new ZAddArgs();
		zAddArgs.nx();
		l = redisExecutor.zadd(key, 11.11, "x".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(1);// nx要求元素不存在
		l = redisExecutor.zadd(key, 22.22, "x".getBytes(), zAddArgs);
		d = redisExecutor.zscore(key, "x".getBytes());
		Assertions.assertThat(d).isEqualTo(11.11);// score不变

		// --------------------------------------------------------------

		zAddArgs = new ZAddArgs();
		zAddArgs.lt();
		l = redisExecutor.zadd(key, 11.11, "y".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(1);// lt要求元素score比已存在的小
		l = redisExecutor.zadd(key, 22.22, "y".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(0);// lt要求元素score比已存在的小
		d = redisExecutor.zscore(key, "y".getBytes());
		Assertions.assertThat(d).isEqualTo(11.11);// score不变
		l = redisExecutor.zadd(key, 9.9, "y".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(0);// lt要求元素score比已存在的小
		d = redisExecutor.zscore(key, "y".getBytes());
		Assertions.assertThat(d).isEqualTo(9.9);// score变了

		// --------------------------------------------------------------

		zAddArgs = new ZAddArgs();
		zAddArgs.ch();
		l = redisExecutor.zadd(key, 9.9, "y".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(0);// score没有被修改，返回值0
		l = redisExecutor.zadd(key, 8.8, "y".getBytes(), zAddArgs);
		Assertions.assertThat(l).isEqualTo(1);// ch是true时，只要元素被修改，返回值1

		// --------------------------------------------------------------重载方法测试

		ScoredValue<byte[]> scoredValue1 = new ScoredValue<>(1.1, "m".getBytes());
		ScoredValue<byte[]> scoredValue2 = new ScoredValue<>(1.1, "n".getBytes());
		l = redisExecutor.zadd(key, Arrays.asList(scoredValue1, scoredValue2));
		Assertions.assertThat(l).isEqualTo(2);

		zAddArgs = new ZAddArgs();
		ScoredValue<byte[]> scoredValue3 = new ScoredValue<>(1.1, "mm".getBytes());
		ScoredValue<byte[]> scoredValue4 = new ScoredValue<>(1.1, "nn".getBytes());
		l = redisExecutor.zadd(key, Arrays.asList(scoredValue3, scoredValue4), zAddArgs);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void zcard() throws Exception {
		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 1.1, "b".getBytes());

		long l = redisExecutor.zcard(key);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void zcount() throws Exception {
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		Range<Integer> range = Range.create(1, 3);// 两边都是包含
		long l = redisExecutor.zcount(key, range);
		Assertions.assertThat(l).isEqualTo(3);

		range = Range.from(Boundary.excluding(1), Boundary.including(3));
		l = redisExecutor.zcount(key, range);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void zdiff() throws Exception {
		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(key, 3.3, "c".getBytes());
		redisExecutor.zadd(k2, 0.1, "a".getBytes());
		redisExecutor.zadd(k2, 0.2, "b".getBytes());

		List<byte[]> list = redisExecutor.zdiff(key, k2);
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());

		List<ScoredValue<byte[]>> zdiffWithScores = redisExecutor.zdiffWithScores(key, k2);
		Assertions.assertThat(zdiffWithScores.size()).isEqualTo(1);
		Assertions.assertThat(zdiffWithScores.get(0).getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(zdiffWithScores.get(0).getScore()).isEqualTo(3.3);
	}

	@Test
	public void zdiffStore() throws Exception {
		byte[] k3 = "test{tag}key3".getBytes();

		redisExecutor.del(k3);

		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(key, 3.3, "c".getBytes());
		redisExecutor.zadd(k2, 0.1, "a".getBytes());
		redisExecutor.zadd(k2, 0.2, "b".getBytes());

		long l = redisExecutor.zdiffStore(k3, key, k2);
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.zcard(k3);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void zincrby() throws Exception {
		double d = redisExecutor.zincrby(key, 0.5, "a".getBytes());
		Assertions.assertThat(d).isEqualTo(0.5);

		d = redisExecutor.zincrby(key, 0.5, "a".getBytes());
		Assertions.assertThat(d).isEqualTo(1);

		d = redisExecutor.zscore(key, "a".getBytes());
		Assertions.assertThat(d).isEqualTo(1);
	}

	@Test
	public void zinter() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());
		redisExecutor.zadd(k2, 1, "a".getBytes());
		redisExecutor.zadd(k2, 2, "b".getBytes());

		List<byte[]> list = redisExecutor.zinter(key, k2);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());

		ZAggregateArgs zAggregateArgs = new ZAggregateArgs();
		zAggregateArgs.sum();
		zAggregateArgs.weights(2, 3);// 分别指向2个key
		list = redisExecutor.zinter(zAggregateArgs, key, k2);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());

		// ----------------------------------------------------------------------

		List<ScoredValue<byte[]>> zinterWithScores = redisExecutor.zinterWithScores(key, k2);
		Assertions.assertThat(zinterWithScores.size()).isEqualTo(2);
		Assertions.assertThat(zinterWithScores.get(0).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(zinterWithScores.get(0).getScore()).isEqualTo(2);// 默认是sum
		Assertions.assertThat(zinterWithScores.get(1).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(zinterWithScores.get(1).getScore()).isEqualTo(4);// 默认是sum

		// ----------------------------------------------------------------------

		zAggregateArgs = new ZAggregateArgs();
		zAggregateArgs.sum();
		zAggregateArgs.weights(2, 3);// 分别指向2个key

		zinterWithScores = redisExecutor.zinterWithScores(zAggregateArgs, key, k2);
		Assertions.assertThat(zinterWithScores.size()).isEqualTo(2);
		Assertions.assertThat(zinterWithScores.get(0).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(zinterWithScores.get(0).getScore()).isEqualTo(5);// 默认是sum
		Assertions.assertThat(zinterWithScores.get(1).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(zinterWithScores.get(1).getScore()).isEqualTo(10);// 默认是sum

		// ----------------------------------------------------------------------

		zAggregateArgs = new ZAggregateArgs();
		zAggregateArgs.max();
		zAggregateArgs.weights(2, 3);// 分别指向2个key

		zinterWithScores = redisExecutor.zinterWithScores(zAggregateArgs, key, k2);
		Assertions.assertThat(zinterWithScores.size()).isEqualTo(2);
		Assertions.assertThat(zinterWithScores.get(0).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(zinterWithScores.get(0).getScore()).isEqualTo(3);// max
		Assertions.assertThat(zinterWithScores.get(1).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(zinterWithScores.get(1).getScore()).isEqualTo(6);// max
	}

	@Test
	public void zintercard() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());
		redisExecutor.zadd(k2, 1, "a".getBytes());
		redisExecutor.zadd(k2, 2, "b".getBytes());

		long l = redisExecutor.zintercard(key, k2);
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.zintercard(1, key, k2);
		Assertions.assertThat(l).isEqualTo(1);
	}

	@Test
	public void zinterstore() throws Exception {
		byte[] k3 = "test{tag}key3".getBytes();
		redisExecutor.del(k3);

		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());
		redisExecutor.zadd(k2, 1, "a".getBytes());
		redisExecutor.zadd(k2, 2, "b".getBytes());

		long l = redisExecutor.zinterstore(k3, key, k2);
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.zcard(k3);
		Assertions.assertThat(l).isEqualTo(2);

		// --------------------------------------------------------------------------------------
		redisExecutor.del(k3);

		ZAggregateArgs zAggregateArgs = new ZAggregateArgs();
		zAggregateArgs.sum();
		zAggregateArgs.weights(2, 3);// 分别指向2个key
		l = redisExecutor.zinterstore(k3, zAggregateArgs, key, k2);
		Assertions.assertThat(l).isEqualTo(2);

		l = redisExecutor.zcard(k3);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void zlexcount() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 0, "a".getBytes());
		redisExecutor.zadd(key, 0, "b".getBytes());
		redisExecutor.zadd(key, 0, "c".getBytes());
		redisExecutor.zadd(key, 0, "d".getBytes());
		redisExecutor.zadd(key, 0, "e".getBytes());
		redisExecutor.zadd(key, 0, "f".getBytes());
		redisExecutor.zadd(key, 0, "g".getBytes());

		long l = redisExecutor.zlexcount(key, "-".getBytes(), "+".getBytes());
		Assertions.assertThat(l).isEqualTo(7);

		l = redisExecutor.zlexcount(key, "[b".getBytes(), "[f".getBytes());
		Assertions.assertThat(l).isEqualTo(5);
	}

	@Test
	public void zmpop() throws Exception {
		if (redisExecutor instanceof AbstractLettuceRedisExecutor) {
			return;
		}
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(k2, 0.1, "c".getBytes());
		redisExecutor.zadd(k2, 0.2, "d".getBytes());

		/**
		 * 它总是从key的顺序中取，而不是多个key中的最优先
		 */
		KeyValue<byte[], ScoredValue<byte[]>> kv = redisExecutor.zmpop(SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(1.1);
		// -----------------------------------------------------------
		kv = redisExecutor.zmpop(SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(key);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(2.2);
		// -----------------------------------------------------------
		kv = redisExecutor.zmpop(SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.1);
		// -----------------------------------------------------------
		kv = redisExecutor.zmpop(SortedSetOption.MIN, key, k2);
		Assertions.assertThat(kv.getKey()).isEqualTo(k2);
		Assertions.assertThat(kv.getValue().getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(kv.getValue().getScore()).isEqualTo(0.2);

		// -----------------------------------------------------------
		// -----------------------------------------------------------
		// -----------------------------------------------------------

		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(k2, 0.1, "c".getBytes());
		redisExecutor.zadd(k2, 0.2, "d".getBytes());

		KeyValue<byte[], List<ScoredValue<byte[]>>> bzmpop = redisExecutor.zmpop(SortedSetOption.MAX, 10, key, k2);
		Assertions.assertThat(bzmpop.getKey()).isEqualTo(key);
		Assertions.assertThat(bzmpop.getValue().size()).isEqualTo(2);
		Assertions.assertThat(bzmpop.getValue().get(0).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(0).getScore()).isEqualTo(2.2);
		Assertions.assertThat(bzmpop.getValue().get(1).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(1).getScore()).isEqualTo(1.1);
		// -----------------------------------------------------------
		bzmpop = redisExecutor.zmpop(SortedSetOption.MAX, 10, key, k2);
		Assertions.assertThat(bzmpop.getKey()).isEqualTo(k2);
		Assertions.assertThat(bzmpop.getValue().size()).isEqualTo(2);
		Assertions.assertThat(bzmpop.getValue().get(0).getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(0).getScore()).isEqualTo(0.2);
		Assertions.assertThat(bzmpop.getValue().get(1).getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(bzmpop.getValue().get(1).getScore()).isEqualTo(0.1);
	}

	@Test
	public void zmscore() throws Exception {
		List<Double> list = redisExecutor.zmscore(key, "a".getBytes(), "c".getBytes());// 不会返回null
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isNull();
		Assertions.assertThat(list.get(1)).isNull();

		redisExecutor.zadd(key, 1.5, "a".getBytes());
		redisExecutor.zadd(key, 1.2, "b".getBytes());
		redisExecutor.zadd(key, 1.3, "c".getBytes());

		list = redisExecutor.zmscore(key, "a".getBytes(), "c".getBytes());
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo(1.5);// a
		Assertions.assertThat(list.get(1)).isEqualTo(1.3);// c
	}

	@Test
	public void zpopmax() throws Exception {
		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(key, 3.3, "c".getBytes());

		ScoredValue<byte[]> zpopmax = redisExecutor.zpopmax(key);
		Assertions.assertThat(zpopmax.getScore()).isEqualTo(3.3);
		Assertions.assertThat(zpopmax.getValue()).isEqualTo("c".getBytes());

		List<ScoredValue<byte[]>> list = redisExecutor.zpopmax(key, 10);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0).getScore()).isEqualTo(2.2);
		Assertions.assertThat(list.get(1).getScore()).isEqualTo(1.1);
	}

	@Test
	public void zpopmin() throws Exception {
		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(key, 3.3, "c".getBytes());

		ScoredValue<byte[]> zpopmax = redisExecutor.zpopmin(key);
		Assertions.assertThat(zpopmax.getScore()).isEqualTo(1.1);
		Assertions.assertThat(zpopmax.getValue()).isEqualTo("a".getBytes());

		List<ScoredValue<byte[]>> list = redisExecutor.zpopmin(key, 10);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0).getScore()).isEqualTo(2.2);
		Assertions.assertThat(list.get(1).getScore()).isEqualTo(3.3);
	}

	@Test
	public void zrandmember() throws Exception {
		byte[] bs = redisExecutor.zrandmember(key);
		Assertions.assertThat(bs).isNull();

		List<byte[]> list = redisExecutor.zrandmember(key, 10);
		Assertions.assertThat(list.size()).isEqualTo(0);

		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(key, 3.3, "c".getBytes());

		bs = redisExecutor.zrandmember(key);
		Assertions.assertThat(bs).isNotNull();

		long l = redisExecutor.zcard(key);
		Assertions.assertThat(l).isEqualTo(3);

		list = redisExecutor.zrandmember(key, 10);
		Assertions.assertThat(l).isEqualTo(3);

		List<ScoredValue<byte[]>> zrandmemberWithScores = redisExecutor.zrandmemberWithScores(key, 10);
		Assertions.assertThat(zrandmemberWithScores.size()).isEqualTo(3);
	}

	@Test
	public void zrange() throws Exception {
		List<byte[]> list = redisExecutor.zrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(0);

		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(key, 3.3, "c".getBytes());

		list = redisExecutor.zrange(key, 1, -1);// 从index 1到末尾
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());

		List<ScoredValue<byte[]>> zrangeWithScores = redisExecutor.zrangeWithScores(key, 1, -1);
		Assertions.assertThat(zrangeWithScores.size()).isEqualTo(2);
		Assertions.assertThat(zrangeWithScores.stream().allMatch(one -> one.getScore() > 0 && one.getValue() != null))
				.isTrue();
	}

	@Test
	public void zrangeByLex() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 0, "a".getBytes());
		redisExecutor.zadd(key, 0, "b".getBytes());
		redisExecutor.zadd(key, 0, "c".getBytes());
		redisExecutor.zadd(key, 0, "d".getBytes());
		redisExecutor.zadd(key, 0, "e".getBytes());
		redisExecutor.zadd(key, 0, "f".getBytes());
		redisExecutor.zadd(key, 0, "g".getBytes());

		Range<byte[]> range = Range.create("-".getBytes(), "[c".getBytes());// 包含c
		List<byte[]> list = redisExecutor.zrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("c".getBytes());

		range = Range.from(Boundary.including("-".getBytes()), Boundary.including("[c".getBytes()));// 不同的方式创建
		list = redisExecutor.zrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("c".getBytes());

		// ---------------------------------------------------------------------------------

		range = Range.create("-".getBytes(), "(c".getBytes());// 不包含c
		list = redisExecutor.zrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());

		range = Range.from(Boundary.including("-".getBytes()), Boundary.including("(c".getBytes()));// 不同的方式创建
		list = redisExecutor.zrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());

		// ---------------------------------------------------------------------------------

		range = Range.create("[aaa".getBytes(), "(g".getBytes());
		list = redisExecutor.zrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(5);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(3)).isEqualTo("e".getBytes());
		Assertions.assertThat(list.get(4)).isEqualTo("f".getBytes());

		range = Range.from(Boundary.including("[aaa".getBytes()), Boundary.including("(g".getBytes()));// 不同的方式创建
		list = redisExecutor.zrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(5);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(3)).isEqualTo("e".getBytes());
		Assertions.assertThat(list.get(4)).isEqualTo("f".getBytes());

		// ---------------------------------------------------------------------------------

		list = redisExecutor.zrangeByLex(key, range, 0, 10);
		Assertions.assertThat(list.size()).isEqualTo(5);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(3)).isEqualTo("e".getBytes());
		Assertions.assertThat(list.get(4)).isEqualTo("f".getBytes());

		list = redisExecutor.zrangeByLex(key, range, 2, 2);// limit 2,2
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("e".getBytes());
	}

	@Test
	public void zrangeByScore() throws Exception {
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		Range<Integer> range = Range.create(1, 3);
		List<byte[]> list = redisExecutor.zrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("c".getBytes());

		range = Range.from(Boundary.including(1), Boundary.including(2));// 不同的方式创建
		list = redisExecutor.zrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());

		range = Range.from(Boundary.excluding(1), Boundary.including(2));// 不同的方式创建
		list = redisExecutor.zrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());

		range = Range.from(Boundary.excluding(1), Boundary.excluding(2));// 不同的方式创建
		list = redisExecutor.zrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(0);

		range = Range.from(Boundary.including(1), Boundary.including(3));// 不同的方式创建
		list = redisExecutor.zrangeByScore(key, range, 2, 2);// limit 2,2
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());

		// ----------------------------------------------------------------------------

		List<ScoredValue<byte[]>> zrangeByScoreWithScores = redisExecutor.zrangeByScoreWithScores(key, range);
		Assertions.assertThat(zrangeByScoreWithScores.size()).isEqualTo(3);
		Assertions
				.assertThat(
						zrangeByScoreWithScores.stream().allMatch(one -> one.getScore() > 0 && one.getValue() != null))
				.isTrue();

		zrangeByScoreWithScores = redisExecutor.zrangeByScoreWithScores(key, range, 2, 2);
		Assertions.assertThat(zrangeByScoreWithScores.size()).isEqualTo(1);
		Assertions
				.assertThat(
						zrangeByScoreWithScores.stream().allMatch(one -> one.getScore() > 0 && one.getValue() != null))
				.isTrue();
	}

	@Test
	public void zrangestore() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		byte[] k3 = "test{tag}key3".getBytes();
		redisExecutor.del(k3);

		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		long l = redisExecutor.zrangestore(k3, key, Range.create(1L, 10L));// 这是index的start 和 stop
		Assertions.assertThat(l).isEqualTo(2);
		l = redisExecutor.zcard(k3);
		Assertions.assertThat(l).isEqualTo(2);

		// ------------------------------------------------------------------

		if (!(redisExecutor instanceof AbstractLettuceRedisExecutor)) {
			redisExecutor.del(k3);

			l = redisExecutor.zrangestoreByLex(k3, key, Range.create("-".getBytes(), "[c".getBytes()), 1, 2);
			Assertions.assertThat(l).isEqualTo(2);
			l = redisExecutor.zcard(k3);
			Assertions.assertThat(l).isEqualTo(2);
		}

		// ------------------------------------------------------------------

		redisExecutor.del(k3);

		l = redisExecutor.zrangestoreByScore(k3, key, Range.from(Boundary.including(1), Boundary.including(3)), 1, 2);
		Assertions.assertThat(l).isEqualTo(2);
		l = redisExecutor.zcard(k3);
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void zrank() throws Exception {
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		Long l = redisExecutor.zrank(key, "c".getBytes());
		Assertions.assertThat(l).isEqualTo(2);// 首位是0
	}

	@Test
	public void zrem() throws Exception {
		long l = redisExecutor.zrem(key, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(0);

		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		l = redisExecutor.zrem(key, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(1);

		l = redisExecutor.zrem(key, "a".getBytes(), "b".getBytes(), "c".getBytes());
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void zremrangeByLex() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		Range<byte[]> range = Range.create("-".getBytes(), "[c".getBytes());
		long l = redisExecutor.zremrangeByLex(key, range);
		Assertions.assertThat(l).isEqualTo(3);
	}

	@Test
	public void zremrangeByRank() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		long l = redisExecutor.zremrangeByRank(key, 1, 2);// index 1到2，保留0
		Assertions.assertThat(l).isEqualTo(2);
	}

	@Test
	public void zremrangeByScore() throws Exception {
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		Range<Integer> range = Range.from(Boundary.including(1), Boundary.including(3));
		long l = redisExecutor.zremrangeByScore(key, range);
		Assertions.assertThat(l).isEqualTo(3);
	}

	@Test
	public void zrevrange() throws Exception {
		List<byte[]> list = redisExecutor.zrevrange(key, 0, -1);
		Assertions.assertThat(list.size()).isEqualTo(0);

		redisExecutor.zadd(key, 1.1, "a".getBytes());
		redisExecutor.zadd(key, 2.2, "b".getBytes());
		redisExecutor.zadd(key, 3.3, "c".getBytes());

		list = redisExecutor.zrevrange(key, 1, -1);// 从index 1到末尾
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("a".getBytes());

		List<ScoredValue<byte[]>> zrangeWithScores = redisExecutor.zrevrangeWithScores(key, 1, -1);
		Assertions.assertThat(zrangeWithScores.size()).isEqualTo(2);
		Assertions.assertThat(zrangeWithScores.stream().allMatch(one -> one.getScore() > 0 && one.getValue() != null))
				.isTrue();
	}

	@Test
	public void zrevrangeByLex() throws Exception {
		if (redisExecutor instanceof AbstractLettuceRedisExecutor) {
			return;
		}
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 0, "a".getBytes());
		redisExecutor.zadd(key, 0, "b".getBytes());
		redisExecutor.zadd(key, 0, "c".getBytes());
		redisExecutor.zadd(key, 0, "d".getBytes());
		redisExecutor.zadd(key, 0, "e".getBytes());
		redisExecutor.zadd(key, 0, "f".getBytes());
		redisExecutor.zadd(key, 0, "g".getBytes());

		Range<byte[]> range = Range.create("-".getBytes(), "[c".getBytes());// 包含c
		List<byte[]> list = redisExecutor.zrevrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("a".getBytes());

		range = Range.from(Boundary.including("-".getBytes()), Boundary.including("[c".getBytes()));// 不同的方式创建
		list = redisExecutor.zrevrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("a".getBytes());

		// ---------------------------------------------------------------------------------

		range = Range.create("-".getBytes(), "(c".getBytes());// 不包含c
		list = redisExecutor.zrevrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("a".getBytes());

		range = Range.from(Boundary.including("-".getBytes()), Boundary.including("(c".getBytes()));// 不同的方式创建
		list = redisExecutor.zrevrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("a".getBytes());

		// ---------------------------------------------------------------------------------

		range = Range.create("[aaa".getBytes(), "(g".getBytes());
		list = redisExecutor.zrevrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(5);
		Assertions.assertThat(list.get(0)).isEqualTo("f".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("e".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(3)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(4)).isEqualTo("b".getBytes());

		range = Range.from(Boundary.including("[aaa".getBytes()), Boundary.including("(g".getBytes()));// 不同的方式创建
		list = redisExecutor.zrevrangeByLex(key, range);
		Assertions.assertThat(list.size()).isEqualTo(5);
		Assertions.assertThat(list.get(0)).isEqualTo("f".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("e".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(3)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(4)).isEqualTo("b".getBytes());

		// ---------------------------------------------------------------------------------

		list = redisExecutor.zrevrangeByLex(key, range, 0, 10);
		Assertions.assertThat(list.size()).isEqualTo(5);
		Assertions.assertThat(list.get(0)).isEqualTo("f".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("e".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(3)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(4)).isEqualTo("b".getBytes());

		list = redisExecutor.zrevrangeByLex(key, range, 2, 2);// limit 2,2
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("d".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());
	}

	@Test
	public void zrevrangeByScore() throws Exception {
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		Range<Integer> range = Range.create(1, 3);
		List<byte[]> list = redisExecutor.zrevrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0)).isEqualTo("c".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(2)).isEqualTo("a".getBytes());

		range = Range.from(Boundary.including(1), Boundary.including(2));// 不同的方式创建
		list = redisExecutor.zrevrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(2);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());
		Assertions.assertThat(list.get(1)).isEqualTo("a".getBytes());

		range = Range.from(Boundary.excluding(1), Boundary.including(2));// 不同的方式创建
		list = redisExecutor.zrevrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(list.get(0)).isEqualTo("b".getBytes());

		range = Range.from(Boundary.excluding(1), Boundary.excluding(2));// 不同的方式创建
		list = redisExecutor.zrevrangeByScore(key, range);
		Assertions.assertThat(list.size()).isEqualTo(0);

		range = Range.from(Boundary.including(1), Boundary.including(3));// 不同的方式创建
		list = redisExecutor.zrevrangeByScore(key, range, 2, 2);// limit 2,2
		Assertions.assertThat(list.size()).isEqualTo(1);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());

		// ----------------------------------------------------------------------------

		List<ScoredValue<byte[]>> zrangeByScoreWithScores = redisExecutor.zrevrangeByScoreWithScores(key, range);
		Assertions.assertThat(zrangeByScoreWithScores.size()).isEqualTo(3);
		Assertions
				.assertThat(
						zrangeByScoreWithScores.stream().allMatch(one -> one.getScore() > 0 && one.getValue() != null))
				.isTrue();

		zrangeByScoreWithScores = redisExecutor.zrevrangeByScoreWithScores(key, range, 2, 2);
		Assertions.assertThat(zrangeByScoreWithScores.size()).isEqualTo(1);
		Assertions
				.assertThat(
						zrangeByScoreWithScores.stream().allMatch(one -> one.getScore() > 0 && one.getValue() != null))
				.isTrue();
	}

	@Test
	public void zrevrank() throws Exception {
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		Long l = redisExecutor.zrevrank(key, "a".getBytes());
		Assertions.assertThat(l).isEqualTo(2);// 首位是0
	}

	@Test
	public void zscan() throws Exception {
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());

		byte[] cursorbytes = "0".getBytes();
		ScoredValueScanCursor<byte[]> cursor = null;
		do {
			cursor = redisExecutor.zscan(key, cursorbytes);

			cursor.getValues().forEach(sv -> {
				System.out.println(new String(sv.getValue()) + ":" + sv.getScore());
			});

			cursorbytes = cursor.getCursor().getBytes();
		} while (!cursor.isFinished());

		// -----------------------------------------------------------------

		ScanArgs scanArgs = new ScanArgs();
		scanArgs.match("*".getBytes());

		cursorbytes = "0".getBytes();
		cursor = null;
		do {
			cursor = redisExecutor.zscan(key, cursorbytes, scanArgs);

			cursor.getValues().forEach(sv -> {
				System.out.println(new String(sv.getValue()) + ":" + sv.getScore());
			});

			cursorbytes = cursor.getCursor().getBytes();
		} while (!cursor.isFinished());
	}

	@Test
	public void zscore() throws Exception {
		// 不需要单独测
	}

	@Test
	public void zunion() throws Exception {
		if(redisExecutor instanceof RedisTemplateRedisExecutor) {
			return;
		}
		
		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());
		redisExecutor.zadd(k2, 1, "a".getBytes());
		redisExecutor.zadd(k2, 2, "b".getBytes());
		redisExecutor.zadd(k2, 4, "d".getBytes());

		List<byte[]> list = redisExecutor.zunion(key, k2);
		Assertions.assertThat(list.size()).isEqualTo(4);// 结果按score从小到大排序， 默认是sum
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());// 2
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());// 3
		Assertions.assertThat(list.get(2)).isEqualTo("b".getBytes());// 4
		Assertions.assertThat(list.get(3)).isEqualTo("d".getBytes());// 4

		ZAggregateArgs zAggregateArgs = new ZAggregateArgs();
		zAggregateArgs.sum();
		zAggregateArgs.weights(2, 3);// 分别指向2个key
		list = redisExecutor.zunion(zAggregateArgs, key, k2);
		Assertions.assertThat(list.size()).isEqualTo(4);
		Assertions.assertThat(list.get(0)).isEqualTo("a".getBytes());// 5
		Assertions.assertThat(list.get(1)).isEqualTo("c".getBytes());// 6
		Assertions.assertThat(list.get(2)).isEqualTo("b".getBytes());// 10
		Assertions.assertThat(list.get(3)).isEqualTo("d".getBytes());// 12

		// ----------------------------------------------------------------------

		List<ScoredValue<byte[]>> zunionWithScores = redisExecutor.zunionWithScores(key, k2);
		Assertions.assertThat(zunionWithScores.size()).isEqualTo(4);
		Assertions.assertThat(zunionWithScores.get(0).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(zunionWithScores.get(0).getScore()).isEqualTo(2);// 默认是sum
		Assertions.assertThat(zunionWithScores.get(1).getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(zunionWithScores.get(1).getScore()).isEqualTo(3);// 默认是sum
		Assertions.assertThat(zunionWithScores.get(2).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(zunionWithScores.get(2).getScore()).isEqualTo(4);// 默认是sum
		Assertions.assertThat(zunionWithScores.get(3).getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(zunionWithScores.get(3).getScore()).isEqualTo(4);// 默认是sum

		// ----------------------------------------------------------------------

		zunionWithScores = redisExecutor.zunionWithScores(zAggregateArgs, key, k2);
		Assertions.assertThat(zunionWithScores.size()).isEqualTo(4);
		Assertions.assertThat(zunionWithScores.get(0).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(zunionWithScores.get(0).getScore()).isEqualTo(5);// 默认是sum
		Assertions.assertThat(zunionWithScores.get(1).getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(zunionWithScores.get(1).getScore()).isEqualTo(6);// 默认是sum
		Assertions.assertThat(zunionWithScores.get(2).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(zunionWithScores.get(2).getScore()).isEqualTo(10);// 默认是sum
		Assertions.assertThat(zunionWithScores.get(3).getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(zunionWithScores.get(3).getScore()).isEqualTo(12);// 默认是sum

		// ----------------------------------------------------------------------

		zAggregateArgs = new ZAggregateArgs();
		zAggregateArgs.max();
		zAggregateArgs.weights(2, 3);// 分别指向2个key

		zunionWithScores = redisExecutor.zunionWithScores(zAggregateArgs, key, k2);
		Assertions.assertThat(zunionWithScores.size()).isEqualTo(4);
		Assertions.assertThat(zunionWithScores.get(0).getValue()).isEqualTo("a".getBytes());
		Assertions.assertThat(zunionWithScores.get(0).getScore()).isEqualTo(3);// max
		Assertions.assertThat(zunionWithScores.get(1).getValue()).isEqualTo("b".getBytes());
		Assertions.assertThat(zunionWithScores.get(1).getScore()).isEqualTo(6);// max
		Assertions.assertThat(zunionWithScores.get(2).getValue()).isEqualTo("c".getBytes());
		Assertions.assertThat(zunionWithScores.get(2).getScore()).isEqualTo(6);// max
		Assertions.assertThat(zunionWithScores.get(3).getValue()).isEqualTo("d".getBytes());
		Assertions.assertThat(zunionWithScores.get(3).getScore()).isEqualTo(12);// max
	}

	@Test
	public void zunionstore() throws Exception {
		byte[] k3 = "test{tag}key3".getBytes();
		redisExecutor.del(k3);

		redisExecutor.zadd(key, 1, "a".getBytes());
		redisExecutor.zadd(key, 2, "b".getBytes());
		redisExecutor.zadd(key, 3, "c".getBytes());
		redisExecutor.zadd(k2, 1, "a".getBytes());
		redisExecutor.zadd(k2, 2, "b".getBytes());
		redisExecutor.zadd(k2, 4, "d".getBytes());

		long l = redisExecutor.zunionstore(k3, key, k2);
		Assertions.assertThat(l).isEqualTo(4);
		l = redisExecutor.zcard(k3);
		Assertions.assertThat(l).isEqualTo(4);

		// --------------------------------------------------------------------------------------
		redisExecutor.del(k3);

		ZAggregateArgs zAggregateArgs = new ZAggregateArgs();
		zAggregateArgs.sum();
		zAggregateArgs.weights(2, 3);// 分别指向2个key
		l = redisExecutor.zunionstore(k3, zAggregateArgs, key, k2);
		Assertions.assertThat(l).isEqualTo(4);

		l = redisExecutor.zcard(k3);
		Assertions.assertThat(l).isEqualTo(4);
	}

	@Test
	void pubsub() throws Exception {
		byte[] channel = "test.channel".getBytes("utf-8");

		AtomicReference<Object> atomicReference = new AtomicReference<Object>();

		redisExecutor.subscribe(channel, new RedisPubSubListener<byte[], byte[]>() {

			@Override
			public void unsubscribed(byte[] channel, long count) {
				System.out.println("unsubscribed channel:" + new String(channel) + ", count:" + count);
			}

			@Override
			public void subscribed(byte[] channel, long count) {
				System.out.println("subscribed channel:" + new String(channel) + ", count:" + count);
			}

			@Override
			public void punsubscribed(byte[] pattern, long count) {
				System.out.println("punsubscribed pattern:" + new String(pattern) + ", count:" + count);
			}

			@Override
			public void psubscribed(byte[] pattern, long count) {
				System.out.println("psubscribed pattern:" + new String(pattern) + ", count:" + count);
			}

			@Override
			public void message(byte[] pattern, byte[] channel, byte[] message) {
				System.out.println("message pattern:" + new String(pattern) + ", channel:" + new String(channel)
						+ ",message:" + new String(message));
			}

			@Override
			public void message(byte[] channel, byte[] message) {
				System.out.println("message channel:" + new String(channel) + ",message:" + new String(message));

				atomicReference.set(message);
			}
		});
		Thread.sleep(500);// 等待sub生效

		byte[] msg = "abc".getBytes();

		redisExecutor.publish(channel, msg);
		Thread.sleep(500);// 等待收到消息

		Assertions.assertThat(atomicReference.get()).isEqualTo(msg);// 能订阅到消息

		// 验证unsubscribe后不能订阅到消息------------------------------------------------------------
		atomicReference.set(null);
		redisExecutor.unsubscribe(channel);

		msg = "ddd".getBytes();

		redisExecutor.publish(channel, msg);
		Thread.sleep(500);// 等待收到消息

		Assertions.assertThat(atomicReference.get()).isNull();// 不再订阅到
	}
}

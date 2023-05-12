package io.github.icodegarden.commons.redis;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.ScanArgs;
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
	}

	@AfterEach
	void end() throws IOException {
		redisExecutor.del(key);
		redisExecutor.del(k2);
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
		// TODO lettuce还没支持

//		byte[] k1 = "k{tag}k1".getBytes();
//		byte[] k2 = "k{tag}k2".getBytes();
//		
//		String s = redisExecutor.mset(k1,"ohmytext".getBytes(),k2,"mynewtext".getBytes());
//		Assertions.assertThat(s).isEqualTo("OK");
//		
//		LCSParams lcsParams = new LCSParams();
//		LCSMatchResult result = redisExecutor.lcs(k1, k2, lcsParams);
//		Assertions.assertThat(result.getMatchString()).isEqualTo("mytext");
//		
//		redisExecutor.del(k1,k2);
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
		// TODO
		throw new RuntimeException();
	}

	@Test
	public void sortRO() throws Exception {
		// TODO
		throw new RuntimeException();
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

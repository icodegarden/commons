package io.github.icodegarden.commons.redis;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class RedisExecutorTests {

	static byte[] key = "test{tag}key".getBytes();

	RedisExecutor redisExecutor;

	@BeforeEach
	void init() {
		redisExecutor = newInstance();
	}
	
	@AfterEach
	void end() throws IOException {
		redisExecutor.close();
	}
	
	protected abstract RedisExecutor newInstance();

	@Test
	public void del() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		Long del = redisExecutor.del(key);
		Assertions.assertThat(del).isEqualTo(1);
	}

	@Test
	public void del_multi() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set("test{tag}key2".getBytes(), "abcd".getBytes());

		Long del = redisExecutor.del(key, "test{tag}key2".getBytes());

		Assertions.assertThat(del).isEqualTo(2);
	}

	@Test
	public void expire() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		Long expire = redisExecutor.expire(key, 2);

		Assertions.assertThat(expire).isEqualTo(1);
	}

	@Test
	public void get() throws Exception {
		redisExecutor.set(key, "abc".getBytes());

		byte[] bs = redisExecutor.get(key);

		Assertions.assertThat(new String(bs)).isEqualTo("abc");
	}

	@Test
	public void mget() throws Exception {
		redisExecutor.set(key, "abc".getBytes());
		redisExecutor.set("test{tag}key2".getBytes(), "abcd".getBytes());

		List<byte[]> mget = redisExecutor.mget(key, "test{tag}key2".getBytes());

		Assertions.assertThat(mget).hasSize(2);
		Assertions.assertThat(new String(mget.get(0))).isEqualTo("abc");
		Assertions.assertThat(new String(mget.get(1))).isEqualTo("abcd");
	}

	@Test
	public void setex() throws Exception {
		redisExecutor.setex(key, 1, "abc".getBytes());

		byte[] bs = redisExecutor.get(key);

		Assertions.assertThat(bs).isNotNull();

		Thread.sleep(1001);// 等待过期

		bs = redisExecutor.get(key);
		Assertions.assertThat(bs).isNull();
	}

	@Test
	public void setnx() throws Exception {
		Long setnx = redisExecutor.setnx(key, "abc".getBytes());

		Assertions.assertThat(setnx).isEqualTo(1);

		setnx = redisExecutor.setnx(key, "abcd".getBytes());//key已存在

		Assertions.assertThat(setnx).isEqualTo(0);
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
		Thread.sleep(500);//等待sub生效
		
		byte[] msg = "abc".getBytes();
		
		redisExecutor.publish(channel, msg);
		Thread.sleep(500);//等待收到消息
		
		Assertions.assertThat(atomicReference.get()).isEqualTo(msg);//能订阅到消息

		//验证unsubscribe后不能订阅到消息------------------------------------------------------------
		atomicReference.set(null);
		redisExecutor.unsubscribe(channel);
		
		msg = "ddd".getBytes();
		
		redisExecutor.publish(channel, msg);
		Thread.sleep(500);//等待收到消息
		
		Assertions.assertThat(atomicReference.get()).isNull();//不再订阅到
	}
}

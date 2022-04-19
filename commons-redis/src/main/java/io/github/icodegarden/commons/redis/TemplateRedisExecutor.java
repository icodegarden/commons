package io.github.icodegarden.commons.redis;

import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.Subscription;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.BinaryJedisPubSub;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SuppressWarnings("rawtypes")
public class TemplateRedisExecutor implements RedisExecutor {
	private static final Logger log = LoggerFactory.getLogger(TemplateRedisExecutor.class);

	private RedisTemplate redisTemplate;

	public TemplateRedisExecutor(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public byte[] get(byte[] key) {
		return (byte[]) redisTemplate.execute((RedisCallback) connection -> {
			return connection.get(key);
		});
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		return (List) redisTemplate.execute((RedisCallback) connection -> {
			return connection.mGet(keys);
		});
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			return connection.set(key, value).toString();
		});
	}

	@Override
	public String setex(byte[] key, long seconds, byte[] value) {
		return (String) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setEx(key, seconds, value).toString();
		});
	}

	@Override
	public Long setnx(byte[] key, byte[] value) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.setNX(key, value) ? 1L : 0L;
		});
	}

	@Override
	public Long expire(byte[] key, long seconds) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.expire(key, seconds) ? 1L : 0L;
		});
	}

	@Override
	public Object eval(byte[] script, int keyCount, byte[]... params) {
		return redisTemplate.execute((RedisCallback) connection -> {
			return connection.eval(script, ReturnType.BOOLEAN, keyCount, params);
		});
	}

	@Override
	public Long del(byte[] key) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.del(key);
		});
	}

	@Override
	public Long del(byte[]... keys) {
		return (Long) redisTemplate.execute((RedisCallback) connection -> {
			return connection.del(keys);
		});
	}

	@Override
	public void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub, Consumer<Unsubscribe> unsubscribeReceiver) {
		redisTemplate.execute((RedisCallback) connection -> {
			unsubscribeReceiver.accept(new Unsubscribe() {
				@Override
				public boolean isSubscribed() {
					return connection.isSubscribed();
				}

				@Override
				public void unsubscribe(byte[]... channels) {
					Subscription subscription = connection.getSubscription();
					if (subscription != null) {
						subscription.unsubscribe(channels);
					}
				}

				@Override
				public void unsubscribe() {
					Subscription subscription = connection.getSubscription();
					if (subscription != null) {
						subscription.unsubscribe();
					}
					if (log.isInfoEnabled()) {
						log.info(this.getClass().getSimpleName() + " unsubscribe channel:{}",
								new String(channel, Charset.forName("utf-8")));
					}
				}
			});

			connection.subscribe((message, pattern) -> {
				jedisPubSub.onMessage(message.getChannel(), message.getBody());
			}, channel);
			return null;
		});
	}

	@Override
	public void publish(byte[] channel, byte[] message) {
		redisTemplate.execute((RedisCallback) connection -> {
			connection.publish(channel, message);
			return null;
		});
	}
}

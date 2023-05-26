package io.github.icodegarden.commons.redis.concurrent.lock;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.concurrent.lock.LockExceedExpectedException;
import io.github.icodegarden.commons.lang.concurrent.lock.LockException;
import io.github.icodegarden.commons.lang.concurrent.lock.LockInterruptedException;
import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class RedisReadWriteLock implements DistributedLock {

	private static final Charset CHARSET = Charset.forName("utf-8");

	private static final byte[] SCRIPT = "local v = redis.call('hset',KEYS[1],ARGV[1],ARGV[2]);if(v==1) then redis.call('expire',KEYS[1],ARGV[3]); end;return v;"
			.getBytes(CHARSET);

	private final boolean readType;

	private final RedisExecutor redisExecutor;

	private final byte[] originIdentifier;
	private final byte[] fieldIdentifier;

	private final byte[] key;
	private final byte[] expireSecondsBytes;

	private long acquireIntervalMillis = 100;

	public RedisReadWriteLock(RedisExecutor redisExecutor, String name, String identifier, Long expireSeconds,
			boolean readType) {
		this.redisExecutor = redisExecutor;
		this.originIdentifier = identifier.getBytes(CHARSET);
		this.fieldIdentifier = identifierToField(identifier, readType);
		this.key = lockName(name).getBytes(CHARSET);
		this.expireSecondsBytes = Long.toString(expireSeconds).getBytes(CHARSET);
		this.readType = readType;
	}

	public void setAcquiredIntervalMillis(long acquireIntervalMillis) {
		Assert.isTrue(acquireIntervalMillis > 0, "acquireIntervalMillis must gt 0");
		this.acquireIntervalMillis = acquireIntervalMillis;
	}

	private String lockName(String name) {
		return "rwlock:" + name;
	}

	private Set<String> toStrFields(Map<byte[], byte[]> hgetAll) {
		return hgetAll.keySet().stream().map(field -> new String(field, StandardCharsets.UTF_8))
				.collect(Collectors.toSet());
	}

//	private Map<String, Boolean> toReadableMap(Map<byte[], byte[]> hgetAll){
//		Map<String, Boolean> map = hgetAll.entrySet().stream().collect(Collectors.toMap(one->new String(one.getKey(),StandardCharsets.UTF_8), one->{
//			int intValue = Integer.parseInt(new String(one.getValue(),StandardCharsets.UTF_8));
//			return intValue==1;
//		}));
//		return map;
//	}

	private byte[] identifierToField(String identifier, boolean readType) {
		String suffix = readType ? "_r":"_w";
		return (identifier+suffix).getBytes(CHARSET);
	}
	
	private byte[] toHashBytes(boolean b) {
		Integer i = b ? 1 : 0;
		return i.toString().getBytes(StandardCharsets.UTF_8);
	}

	private boolean booleanFromHashBytes(byte[] bs) {
		int intValue = Integer.parseInt(new String(bs, StandardCharsets.UTF_8));
		return intValue == 1;
	}

	@Override
	public boolean isAcquired() throws LockException {
		try {
			Map<byte[], byte[]> hgetAll = redisExecutor.hgetAll(key);
			if (hgetAll.isEmpty()) {
				return false;
			}
			Set<String> strFields = toStrFields(hgetAll);
			return strFields.contains(new String(key, StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new LockExceedExpectedException(e);
		}
	}

	@Override
	public void acquire() throws LockException {
		acquire(Long.MAX_VALUE);
	}

//获取读锁->
//	检查本地是否已获取->
//		是->成功+1
//		否->获取锁名称所有lock条数->
//			检查是否有wirte锁->
//				是->write锁是否本地持有的-》
//					是->成功，写入read锁
//					否-》失败
//				否->写入read锁
//					
//释放读锁->删除对应lock数据	
//
//获取写锁->
//    检查本地是否已获取->
//		是-》成功+1
//		否-》获取锁名称所有lock条数->
//			检查是否有任何锁-》
//				是-》失败
//				否-》写入write锁
//				
//释放写锁-》删除对应lock数据					
	@Override
	public boolean acquire(long timeoutMillis) throws LockException {
		LocalDateTime start = SystemUtils.now();
		for (;;) {
			try {
				boolean b = mutex();
				if (!b) {
					List<Object> list = redisExecutor.eval(SCRIPT, 1, key, fieldIdentifier, toHashBytes(readType),
							expireSecondsBytes);
					Long result = (Long) list.get(0);
					boolean success = result == 1;

					if (success) {
						return true;
					}
				}

				if (SystemUtils.now().minus(timeoutMillis, ChronoUnit.MILLIS).isAfter(start)) {
					return false;
				}
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
			sleep();
		}
	}

	private boolean mutex() {
		Map<byte[], byte[]> hgetAll = redisExecutor.hgetAll(key);

		if (this.readType) {
			/**
			 * 是否有write锁且不是本地持有的
			 */
			boolean b = hgetAll.entrySet().stream().anyMatch(one -> {
				boolean isReadType = booleanFromHashBytes(one.getValue());
				return !isReadType && ! CollectionUtils.arrayStartWith(one.getKey(), originIdentifier); 
			});

			return b;
		} else {
			/**
			 * 是否有任何锁
			 */
			boolean b = !hgetAll.isEmpty();
			return b;
		}
	}

	@Override
	public void release() throws LockException {
		try {
			redisExecutor.hdel(key, fieldIdentifier);
		} catch (Exception e) {
			throw new LockExceedExpectedException(e);
		}
	}

	private void sleep() throws LockInterruptedException {
		try {
			Thread.sleep(acquireIntervalMillis);
		} catch (InterruptedException e) {
			throw new LockInterruptedException(e);
		}
	}
}

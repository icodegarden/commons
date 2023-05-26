package io.github.icodegarden.commons.mybatis.concurrent.lock;

import java.util.UUID;

import io.github.icodegarden.commons.lang.concurrent.lock.CustomizeReentrantLock;
import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseReadWriteLock;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;
import io.github.icodegarden.commons.lang.concurrent.lock.MysqlReentrantReadWriteLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlMybatisReentrantReadWriteLock implements MysqlReentrantReadWriteLock {

	private final ReentrantLock readLock;
	private final ReentrantLock writeLock;

	public MysqlMybatisReentrantReadWriteLock(MysqlMybatisReadWriteLockMapper mapper, String name, Long expireSeconds) {
		String identifier = UUID.randomUUID().toString();
		readLock = new ReentrantLock(mapper, name, identifier, expireSeconds, true);
		writeLock = new ReentrantLock(mapper, name, identifier, expireSeconds, false);
	}

	private class ReentrantLock extends CustomizeReentrantLock {

		public ReentrantLock(MysqlMybatisReadWriteLockMapper mapper, String name, String identifier, Long expireSeconds,
				boolean readType) {
			super(new MysqlMyabtisLock(mapper, name, identifier, expireSeconds, readType));
		}
	}

	private class MysqlMyabtisLock extends DatabaseReadWriteLock implements MybatisLock {

		public MysqlMyabtisLock(MysqlMybatisReadWriteLockMapper mapper, String name, String identifier,
				Long expireSeconds, boolean readType) {
			super(mapper, name, identifier, expireSeconds, readType);
		}
	}

	@Override
	public DistributedReentrantLock readLock() {
		return readLock;
	}

	@Override
	public DistributedReentrantLock writeLock() {
		return writeLock;
	}

}

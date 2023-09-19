package io.github.icodegarden.commons.lang.concurrent.lock;

import java.util.UUID;

import javax.sql.DataSource;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcReentrantReadWriteLock implements MysqlReentrantReadWriteLock {

	private final ReentrantLock readLock;
	private final ReentrantLock writeLock;

	public MysqlJdbcReentrantReadWriteLock(DataSource dataSource, String name, Long expireSeconds) {
		String identifier = UUID.randomUUID().toString();
		readLock = new ReentrantLock(dataSource, name, identifier, expireSeconds, true);
		writeLock = new ReentrantLock(dataSource, name, identifier, expireSeconds, false);
	}

	private class ReentrantLock extends CustomizeReentrantLock {

		public ReentrantLock(DataSource dataSource, String name, String identifier, Long expireSeconds,
				boolean readType) {
			super(new MysqlJdbcLock(dataSource, name, identifier, expireSeconds, readType));
		}
	}

	private class MysqlJdbcLock extends DatabaseReadWriteLock implements JdbcLock {

		public MysqlJdbcLock(DataSource dataSource, String name, String identifier, Long expireSeconds,
				boolean readType) {
			super(new MysqlJdbcReadWriteLockRepository(dataSource), name, identifier, expireSeconds, readType);
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

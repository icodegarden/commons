package io.github.icodegarden.commons.lang.concurrent.lock;

import javax.sql.DataSource;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcDistributedLock extends DatabaseDistributedLock implements JdbcDistributedLock {

	public MysqlJdbcDistributedLock(DataSource dataSource, String name, Long expireSeconds) {
		super(new MysqlJdbcDistributedLockDao(dataSource), name, expireSeconds);
	}
}

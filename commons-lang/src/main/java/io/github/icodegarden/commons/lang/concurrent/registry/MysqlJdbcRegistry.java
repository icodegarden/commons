package io.github.icodegarden.commons.lang.concurrent.registry;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.concurrent.lock.MysqlJdbcLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcRegistry extends DatabaseRegistry<Long> implements JdbcRegistry<Registration> {

	private final DataSource dataSource;

	public MysqlJdbcRegistry(DataSource dataSource, RegistryListener listener) {
		super(new MysqlJdbcRegistryRepository(dataSource), listener);
		this.dataSource = dataSource;
	}

	@Override
	protected DistributedLock getRegisterLock(String name, Long expireSeconds) {
		return new MysqlJdbcLock(dataSource, name, expireSeconds);
	}

}

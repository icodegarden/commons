package io.github.icodegarden.commons.lang.concurrent.lock;

import io.github.icodegarden.commons.test.TestsDataSourceDependent;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcLockTests extends DistributedLockTests {

	@Override
	protected DistributedLock newDistributedLock(String name) {
		return new MysqlJdbcLock(TestsDataSourceDependent.DATASOURCE, name, getExpireSeconds());
	}
	
	@Override
	protected long getExpireSeconds() {
		return 5;
	}
}

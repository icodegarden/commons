package io.github.icodegarden.commons.lang.concurrent.lock;

import io.github.icodegarden.commons.test.TestsDataSourceDependent;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedReentrantLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcReentrantLockTests extends DistributedReentrantLockTests {

	@Override
	protected DistributedReentrantLock newLock(String name) {
		return new MysqlJdbcReentrantLock(TestsDataSourceDependent.DATASOURCE, name, 5L);
	}

}

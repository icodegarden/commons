package io.github.icodegarden.commons.lang.concurrent.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.icodegarden.commons.test.TestsDataSourceDependent;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedReentrantReadWriteLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcReentrantReadWriteLockTests extends DistributedReentrantReadWriteLockTests {

	@BeforeEach
	void initDS() {
		TestsDataSourceDependent.clearTable(DatabaseReadWriteLockRepository.TABLE_NAME);
	}

	@AfterEach
	void closeDS() {
	}

	@Override
	protected DistributedReentrantReadWriteLock newLock(String name) {
		return new MysqlJdbcReentrantReadWriteLock(TestsDataSourceDependent.DATASOURCE, name, 5L);
	}

}

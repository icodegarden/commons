package io.github.icodegarden.commons.mybatis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;
import io.github.icodegarden.commons.mybatis.MybatisTestUtils;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedReentrantLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlMybatisReentrantLockTests extends DistributedReentrantLockTests {

	@Override
	protected DistributedReentrantLock newLock(String name) {
		MysqlMybatisLockMapper mapper = MybatisTestUtils.getMapper(MysqlMybatisLockMapper.class);
		return new MysqlMybatisReentrantLock(mapper, name, 5L);
	}

}

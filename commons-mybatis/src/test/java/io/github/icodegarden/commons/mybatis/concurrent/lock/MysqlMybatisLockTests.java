package io.github.icodegarden.commons.mybatis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.mybatis.MybatisTestUtils;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlMybatisLockTests extends DistributedLockTests {

	@Override
	protected DistributedLock newDistributedLock(String name) {
		MysqlMybatisLockMapper mapper = MybatisTestUtils.getMapper(MysqlMybatisLockMapper.class);
		return new MysqlMybatisLock(mapper, name, getExpireSeconds());
	}

	@Override
	protected long getExpireSeconds() {
		return 5;
	}
}

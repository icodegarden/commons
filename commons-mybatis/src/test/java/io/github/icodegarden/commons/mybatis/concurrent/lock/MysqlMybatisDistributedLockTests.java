package io.github.icodegarden.commons.mybatis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.mybatis.MybatisTestUtils;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlMybatisDistributedLockTests extends DistributedLockTests {

	@Override
	protected DistributedLock newDistributedLock(String name) {
		MysqlMybatisDistributedLockMapper mapper = MybatisTestUtils.getMapper(MysqlMybatisDistributedLockMapper.class);
		return new MysqlMybatisDistributedLock(mapper, name, getExpireSeconds());
	}

	@Override
	protected long getExpireSeconds() {
		return 5;
	}
}

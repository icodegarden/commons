package io.github.icodegarden.commons.mybatis.concurrent.registry;

import io.github.icodegarden.commons.lang.concurrent.registry.Registration;
import io.github.icodegarden.commons.lang.concurrent.registry.Registry;
import io.github.icodegarden.commons.lang.concurrent.registry.RegistryListener;
import io.github.icodegarden.commons.mybatis.MybatisTestUtils;
import io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisLockMapper;
import io.github.icodegarden.commons.test.concurrent.registry.RegistryTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlMybatisRegistryTests extends RegistryTests {

	@Override
	protected Registry<Registration> newRegistry(RegistryListener registryListener) {
		MysqlMybatisLockMapper lockMapper = MybatisTestUtils.getMapper(MysqlMybatisLockMapper.class);
		MysqlMybatisRegistryMapper mapper = MybatisTestUtils.getMapper(MysqlMybatisRegistryMapper.class);
		return new MysqlMybatisRegistry(lockMapper, mapper, registryListener);
	}
}

package io.github.icodegarden.commons.mybatis.registry;

import io.github.icodegarden.commons.lang.registry.Registration;
import io.github.icodegarden.commons.lang.registry.Registry;
import io.github.icodegarden.commons.lang.registry.RegistryListener;
import io.github.icodegarden.commons.mybatis.MybatisTestUtils;
import io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisLockMapper;
import io.github.icodegarden.commons.mybatis.registry.MysqlMybatisRegistry;
import io.github.icodegarden.commons.mybatis.registry.MysqlMybatisRegistryMapper;
import io.github.icodegarden.commons.test.registry.RegistryTests;

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

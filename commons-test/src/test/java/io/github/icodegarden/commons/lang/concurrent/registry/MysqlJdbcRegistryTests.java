package io.github.icodegarden.commons.lang.concurrent.registry;

import io.github.icodegarden.commons.test.TestsDataSourceDependent;
import io.github.icodegarden.commons.test.concurrent.registry.RegistryTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcRegistryTests extends RegistryTests {

	@Override
	protected Registry<Registration> newRegistry(RegistryListener registryListener) {
		return new MysqlJdbcRegistry(TestsDataSourceDependent.DATASOURCE, registryListener);
	}

}

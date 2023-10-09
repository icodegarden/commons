package io.github.icodegarden.commons.lang.registry;

import io.github.icodegarden.commons.lang.registry.MysqlJdbcRegistry;
import io.github.icodegarden.commons.lang.registry.Registration;
import io.github.icodegarden.commons.lang.registry.Registry;
import io.github.icodegarden.commons.lang.registry.RegistryListener;
import io.github.icodegarden.commons.test.TestsDataSourceDependent;
import io.github.icodegarden.commons.test.registry.RegistryTests;

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

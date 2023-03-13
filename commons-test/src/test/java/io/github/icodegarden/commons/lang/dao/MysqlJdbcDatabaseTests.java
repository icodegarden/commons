package io.github.icodegarden.commons.lang.dao;

import io.github.icodegarden.commons.test.TestsDataSourceDependent;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MysqlJdbcDatabaseTests extends DatabaseTests {

	@Override
	protected Database getDatabase() {
		return new MysqlJdbcDatabase(TestsDataSourceDependent.DATASOURCE);
	}
}

package io.github.icodegarden.commons.mybatis.repository;

import io.github.icodegarden.commons.lang.dao.DatabaseTests;
import io.github.icodegarden.commons.lang.repository.Database;
import io.github.icodegarden.commons.mybatis.MybatisTestUtils;
import io.github.icodegarden.commons.mybatis.repository.MysqlMybatisDatabase;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MysqlMybatisDatabaseTests extends DatabaseTests {

	@Override
	protected Database getDatabase() {
		return MybatisTestUtils.getMapper(MysqlMybatisDatabase.class);
	}
}
package io.github.icodegarden.commons.mybatis.dao;

import io.github.icodegarden.commons.lang.dao.Database;
import io.github.icodegarden.commons.lang.dao.DatabaseTests;
import io.github.icodegarden.commons.mybatis.MybatisTestUtils;

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
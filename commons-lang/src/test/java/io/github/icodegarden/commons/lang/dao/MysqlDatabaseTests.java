package io.github.icodegarden.commons.lang.dao;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.TestsDataSourceDependent;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MysqlDatabaseTests {

	MysqlDatabase database = new MysqlDatabase(TestsDataSourceDependent.DATASOURCE);

	@Test
	void version() throws Exception {
		String version = database.version();
		System.out.println(version);
		Assertions.assertThat(version).isNotEmpty();
	}

	@Test
	void listTables() throws Exception {
		List<String> listTables = database.listTables();
		System.out.println(listTables);
		Assertions.assertThat(listTables).isNotEmpty();
	}

	@Test
	void countTable() throws Exception {
		/**
		 * 需要先人工建表
		 */
		long countTable = database.countTable("table_data_count");
		System.out.println(countTable);
		Assertions.assertThat(countTable).isGreaterThanOrEqualTo(0);
	}

	@Test
	void optimizeTable() throws Exception {
		/**
		 * 需要先人工建表
		 */
		List<String> desc = database.optimizeTable("table_data_count");
		System.out.println(desc);
	}
}

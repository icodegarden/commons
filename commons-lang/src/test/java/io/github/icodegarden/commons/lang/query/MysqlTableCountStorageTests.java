package io.github.icodegarden.commons.lang.query;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.TestsDataSourceDependent;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MysqlTableCountStorageTests {

	MysqlTableDataCountStorage dao = new MysqlTableDataCountStorage(TestsDataSourceDependent.DATASOURCE);

	@Test
	void add() throws Exception {
		TableDataCountPO po = new TableDataCountPO();
		po.setTableName("test_table");
		po.setDataCount(10L);
		po.setUpdatedAt(LocalDateTime.now());
		dao.add(po);
		
		List<TableDataCountPO> list = dao.findAll();

		Assertions.assertThat(list).hasSize(1);

		dao.delete(list.get(0).getId());
	}

	@Test
	void updateCount() throws Exception {
		TableDataCountPO po = new TableDataCountPO();
		po.setTableName("test_table");
		po.setDataCount(10L);
		po.setUpdatedAt(LocalDateTime.now());
		dao.add(po);

		dao.updateCount(po.getTableName(), 100);

		List<TableDataCountPO> list = dao.findAll();

		Assertions.assertThat(list.get(0).getDataCount()).isEqualTo(100);

		dao.delete(list.get(0).getId());
	}
}

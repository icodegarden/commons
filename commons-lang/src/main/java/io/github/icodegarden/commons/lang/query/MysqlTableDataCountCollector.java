package io.github.icodegarden.commons.lang.query;

import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.repository.MysqlJdbcDatabase;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlTableDataCountCollector extends AbstractTableDataCountCollector {

	private final MysqlJdbcDatabase database;

	public MysqlTableDataCountCollector(DataSource dataSource) {
		this(dataSource, null);
	}

	public MysqlTableDataCountCollector(DataSource dataSource, Set<String> whiteListTables) {
		super(whiteListTables);
		this.database = new MysqlJdbcDatabase(dataSource);
	}

	@Override
	public String version() {
		return database.version();
	}

	@Override
	public List<String> doListTables() {
		return database.listTables();
	}

	@Override
	public long countTable(String tableName) {
		return database.countTable(tableName);
	}

}
package io.github.icodegarden.commons.lang.dao;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Database {

	/**
	 * DB version
	 * @return
	 */
	String version();

	List<String> listTables();

	long countTable(String tableName);

	OptimizeTableResults<OptimizeTableResults.Result> optimizeTable(String tableName);
}

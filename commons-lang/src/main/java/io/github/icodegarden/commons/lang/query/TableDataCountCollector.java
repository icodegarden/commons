package io.github.icodegarden.commons.lang.query;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface TableDataCountCollector {

	/**
	 * DB version
	 * @return
	 */
	String version();

	List<String> listTables();

	long countTable(String tableName);

}
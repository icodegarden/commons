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

	/**
	 * @return 描述信息
	 * @throws IllegalStateException 如果失败
	 */
	List<String> optimizeTable(String tableName) throws IllegalStateException;
}

package io.github.icodegarden.commons.shardingsphere.util;

import java.util.LinkedHashMap;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class DataSourceUtils {

	public static DataSource firstDataSource(ShardingSphereDataSource dataSource) {
		LinkedHashMap<String, DataSource> dataSources = dataSources(dataSource);
//		DataSource dataSource = dataSources.get("ds0");

		return dataSources.values().stream().findFirst().get();
	}

	public static String firstDataSourceName(ShardingSphereDataSource dataSource) {
		LinkedHashMap<String, DataSource> dataSources = dataSources(dataSource);
		return dataSources.keySet().stream().findFirst().get();
	}

	private static LinkedHashMap<String, DataSource> dataSources(ShardingSphereDataSource dataSource) {
		ShardingSphereMetaData shardingSphereMetaData = dataSource.getContextManager().getMetaDataContexts()
				.getMetaDataMap().get("logic_db");
		// 是个LinkedHashMap，这里作为标记强转以防ShardingSphere在后续的版本中不再是LinkedHashMap时我能感知
		LinkedHashMap<String, DataSource> dataSources = (LinkedHashMap) shardingSphereMetaData.getResource()
				.getDataSources();
		return dataSources;
	}
}

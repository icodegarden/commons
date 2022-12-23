package io.github.icodegarden.commons.shardingsphere.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import com.zaxxer.hikari.HikariDataSource;

import io.github.icodegarden.commons.shardingsphere.builder.DataSourceConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class DataSourceUtils {

	public static LinkedHashMap<String, DataSource> createDataSourceMap(List<DataSourceConfig> datasources) {
		/**
		 * shardingsphere以springboot自动创建的ShardingSphereDataSource，其内部的Map<String,
		 * DataSource>是LinkedHashMap，业务上获取第一个DataSource确实也需要他是有序的，因此这里强制使用LinkedHashMap
		 */
		LinkedHashMap<String, DataSource> dataSourceMap = new LinkedHashMap<>();
		for (DataSourceConfig datasource : datasources) {
			HikariDataSource ds = new HikariDataSource();
			ds.setPoolName(datasource.getName());
			ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
			ds.setJdbcUrl(datasource.getJdbcUrl());
			ds.setUsername(datasource.getUsername());
			ds.setPassword(datasource.getPassword());

			if (datasource.getMinimumIdle() != null) {
				ds.setMinimumIdle(datasource.getMinimumIdle());
			}
			if (datasource.getIdleTimeout() != null) {
				ds.setIdleTimeout(datasource.getIdleTimeout());
			}
			if (datasource.getMaximumPoolSize() != null) {
				ds.setMaximumPoolSize(datasource.getMaximumPoolSize());
			}
			if (datasource.getMaxLifetime() != null) {
				ds.setMaxLifetime(datasource.getMaxLifetime());
			}
			if (datasource.getConnectionTimeout() != null) {
				ds.setConnectionTimeout(datasource.getConnectionTimeout());
			}
			if (datasource.getConnectionTestQuery() != null) {
				ds.setConnectionTestQuery(datasource.getConnectionTestQuery());
			}
			if (datasource.getKeepaliveTime() != null) {
				ds.setKeepaliveTime(datasource.getKeepaliveTime());
			}
			if (datasource.getValidationTimeout() != null) {
				ds.setValidationTimeout(datasource.getValidationTimeout());
			}
			dataSourceMap.put(datasource.getName(), ds);
		}
		return dataSourceMap;
	}

	public static DataSource firstDataSource(ShardingSphereDataSource dataSource) {
		LinkedHashMap<String, DataSource> dataSources = dataSources(dataSource);
		return firstDataSource(dataSources);
	}
	
	public static DataSource firstDataSource(LinkedHashMap<String, DataSource> dataSources) {
		return dataSources.values().stream().findFirst().get();
	}

	public static String firstDataSourceName(ShardingSphereDataSource dataSource) {
		LinkedHashMap<String, DataSource> dataSources = dataSources(dataSource);
		return firstDataSourceName(dataSources);
	}
	
	public static String firstDataSourceName(LinkedHashMap<String, DataSource> dataSources) {
		return dataSources.keySet().stream().findFirst().get();
	}

	/**
	 * @return name:DataSource
	 */
	public static Map<String, DataSource> dataSourceMap(ShardingSphereDataSource dataSource) {
		return dataSources(dataSource);
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

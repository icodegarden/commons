package io.github.icodegarden.commons.test;

import java.io.IOException;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public final class TestsDataSourceDependent {

	private static Properties properties = new Properties();
	static{
		try {
			properties.load(TestsDataSourceDependent.class.getClassLoader().getResourceAsStream("test.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final HikariDataSource DATASOURCE = new HikariDataSource();
	static{
		DATASOURCE.setDriverClassName(properties.getProperty("dataSource.driverClassName"));
		DATASOURCE.setJdbcUrl(properties.getProperty("dataSource.jdbcUrl"));
		DATASOURCE.setUsername(properties.getProperty("dataSource.username"));
		DATASOURCE.setPassword(properties.getProperty("dataSource.password"));
	}
}

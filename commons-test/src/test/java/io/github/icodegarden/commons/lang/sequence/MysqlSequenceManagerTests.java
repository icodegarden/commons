package io.github.icodegarden.commons.lang.sequence;

import java.io.IOException;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MysqlSequenceManagerTests extends SequenceManagerTests {

	Properties properties = new Properties();
	{
		try {
			properties.load(MysqlSequenceManagerTests.class.getClassLoader().getResourceAsStream("test.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected SequenceManager getForOneProcess() {
		return newSequenceManager();
	}

	@Override
	protected SequenceManager newForMultiProcess() {
		return newSequenceManager();
	}

	private SequenceManager newSequenceManager() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(properties.getProperty("dataSource.driverClassName"));
		dataSource.setJdbcUrl(properties.getProperty("dataSource.jdbcUrl"));
		dataSource.setUsername(properties.getProperty("dataSource.username"));
		dataSource.setPassword(properties.getProperty("dataSource.password"));

		MysqlSequenceManager idGenerator = new MysqlSequenceManager("GLOBAL", dataSource);
		return idGenerator;
	}
}

package io.github.icodegarden.commons.lang.sequence;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariDataSource;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MysqlSequenceManagerTests {

	Properties properties = new Properties();
	{
		try {
			properties.load(MysqlSequenceManagerTests.class.getClassLoader().getResourceAsStream("test.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void nextId() throws InterruptedException, SQLException, IOException {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(properties.getProperty("dataSource.driverClassName"));
		dataSource.setJdbcUrl(properties.getProperty("dataSource.jdbcUrl"));
		dataSource.setUsername(properties.getProperty("dataSource.username"));
		dataSource.setPassword(properties.getProperty("dataSource.password"));

		MysqlSequenceManager idGenerator = new MysqlSequenceManager("GLOBAL", dataSource);

		int threads = 8;
		int threadLoop = 2000;

		CountDownLatch latch = new CountDownLatch(threads);

		ArrayList<HashSet<Long>> arrayList = new ArrayList<HashSet<Long>>();
		for (int i = 0; i < threads; i++) {
			new Thread() {
				public void run() {
					HashSet<Long> hashSet = new HashSet<Long>();
					try {
						for (int i = 0; i < threadLoop; i++) {
							long id = idGenerator.nextId();

							hashSet.add(id);
						}
						arrayList.add(hashSet);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
					latch.countDown();
				};
			}.start();
		}

		latch.await();

		Set<Long> set = arrayList.stream().flatMap(list -> list.stream()).collect(Collectors.toSet());// 去重的
		Assertions.assertThat(set).hasSize(threads * threadLoop);
	}

}

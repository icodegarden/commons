package io.github.icodegarden.commons.lang.sequence;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
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
class SnowflakeSequenceManagerTests {

//	@Test
//	void nextIdLoop() throws InterruptedException, SQLException, IOException {
//		for (int i = 0; i < 100; i++) {
//			try{
//				nextId();
//				System.out.println("running loop="+i);
//			}catch (Throwable e) {
//				System.out.println("error in loop="+i);
//				throw e;
//			}
//		}
//	}

	@Test
	void nextId() throws InterruptedException, SQLException, IOException {
		SnowflakeSequenceManager idGenerator = new SnowflakeSequenceManager(0, 0);

		int threads = 100;
		int threadLoop = 20000;

		CountDownLatch latch = new CountDownLatch(threads);

		ArrayList<ArrayList<Long>> arrayList = new ArrayList<ArrayList<Long>>();
		for (int i = 0; i < threads; i++) {
			new Thread() {
				public void run() {
					ArrayList<Long> ids = new ArrayList<Long>();
					try {
						for (int i = 0; i < threadLoop; i++) {
							long id = idGenerator.nextId();

							ids.add(id);
						}
						// 验证id是顺序获得的
						ArrayList<Long> list = new ArrayList<Long>(ids);
						ids.sort(Comparator.naturalOrder());
						Assertions.assertThat(list).isEqualTo(ids);

						arrayList.add(ids);
					} catch (Throwable e) {
						e.printStackTrace();
						System.exit(-1);
						return;
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

package io.github.icodegarden.commons.lang.sequence;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class SequenceManagerTests {

	protected abstract SequenceManager getForOneProcess();

	protected abstract SequenceManager newForMultiProcess();

	@Test
	void nextId_oneProcess() throws InterruptedException, SQLException, IOException {
		SequenceManager idGenerator = getForOneProcess();

		int threads = 10;
		int threadLoop = 2000;

		CountDownLatch latch = new CountDownLatch(threads);

		CopyOnWriteArrayList<ArrayList<Long>> arrayList = new CopyOnWriteArrayList<ArrayList<Long>>();
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

	@Test
	void nextId_multiProcess() throws InterruptedException, SQLException, IOException {
		int process = 10;
		int threads = 10;
		int threadLoop = 2000;

		CountDownLatch latch = new CountDownLatch(process * threads);

		CopyOnWriteArrayList<ArrayList<Long>> arrayList = new CopyOnWriteArrayList<ArrayList<Long>>();
		for (int p = 0; p < process; p++) {
			/**
			 * 每个线程独占数据源，模拟不同的进程
			 */
			SequenceManager idGenerator = newForMultiProcess();

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
		}

		latch.await();

		Set<Long> set = arrayList.stream().flatMap(list -> list.stream()).collect(Collectors.toSet());// 去重的
		Assertions.assertThat(set).hasSize(process * threads * threadLoop);
	}

}

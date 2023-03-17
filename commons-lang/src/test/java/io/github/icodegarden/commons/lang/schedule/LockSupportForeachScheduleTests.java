package io.github.icodegarden.commons.lang.schedule;

import java.util.Arrays;
import java.util.LinkedList;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;

/**
 * 
 * @author xff
 *
 */
public class LockSupportForeachScheduleTests {

	private class BizSchedule extends LockSupportForeachSchedule {
		public BizSchedule(DistributedLock lock) {
			super(lock);
		}
		
		@Override
		protected DoForeachResult doForeach(long loop) throws Throwable {
			Integer i = datas.removeFirst();
			System.out.println(i + " was remoed, ts:" + System.currentTimeMillis());
			if (datas.isEmpty()) {
				synchronized (this) {
					this.notify();
				}
				
				return new DoForeachResult(true);
			}
			return new DoForeachResult(false);
		}
	}

	private LinkedList<Integer> datas;
	private BizSchedule bizSchedule;

	@BeforeEach
	void start() {
		datas = new LinkedList<Integer>(Arrays.asList(1, 2, 3, 4, 5));

		bizSchedule = new BizSchedule(new MockDistributedLock());
	}

	@AfterEach
	void end() throws Exception {
		// 等待执行一次
		synchronized (bizSchedule) {
			bizSchedule.wait();
		}

		bizSchedule.shutdown();

		/**
		 * validate
		 */
		Assertions.assertThat(datas.size()).isEqualTo(0);
	}

	@Test
	public void scheduleWithFixedDelay() throws Exception {
		bizSchedule.scheduleWithFixedDelay(100, 100);
	}

	@Test
	public void scheduleAtFixedRate() throws Exception {
		bizSchedule.scheduleAtFixedRate(100, 100);
	}

	@Test
	public void scheduleWithCron() throws Exception {
		bizSchedule.scheduleWithCron("0/1 * * * * *");
	}
}

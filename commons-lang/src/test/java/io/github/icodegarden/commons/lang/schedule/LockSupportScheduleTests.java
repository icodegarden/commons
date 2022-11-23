package io.github.icodegarden.commons.lang.schedule;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;

/**
 * 
 * @author xff
 *
 */
public class LockSupportScheduleTests {

	private static List<Integer> datas = new LinkedList<Integer>(Arrays.asList(1, 2));

	private static class BizSchedule extends LockSupportSchedule {
		public BizSchedule(DistributedLock lock) {
			super(lock);
		}

		@Override
		protected void doScheduleAfterLocked() throws Throwable {
			datas.clear();

			synchronized (this) {
				this.notify();
			}
		}
	}

	@Test
	public void schedule() throws Exception {
		// validate
		Assertions.assertThat(datas.size()).isEqualTo(2);

		BizSchedule bizSchedule = new BizSchedule(new MockDistributedLock());

		bizSchedule.start(100, 100);

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

}

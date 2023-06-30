package io.github.icodegarden.commons.lang.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ThreadUtilsTests {

	@Test
	void test() throws Exception {
		ThreadPoolExecutor threadPool = ThreadUtils.newEagerThreadPool(2, 100, 10000000, 10000, null);
//		ThreadPoolExecutor threadPool = ThreadUtils.newFixedThreadPool(20, 10000000, null);
//		ThreadPoolExecutor threadPool = ThreadUtils.newCachedThreadPool(2, 200, 10000000, 10000, null);
//		ThreadPoolExecutor threadPool = ThreadUtils.newLimitedThreadPool(2, 20, 10000000, null);

		AtomicLong atomicLong = new AtomicLong();

		long s = System.currentTimeMillis();
		for (int i = 0; i < 200; i++) {
			threadPool.execute(() -> {
				System.out.println(atomicLong.incrementAndGet());
				ThreadUtils.sleep(1000);
			});
		}
		System.out.println("use:" + (System.currentTimeMillis() - s));
		
		ThreadUtils.sleep(5000);
	}
}

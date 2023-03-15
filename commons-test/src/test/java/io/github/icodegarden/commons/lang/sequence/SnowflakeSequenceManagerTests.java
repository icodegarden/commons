package io.github.icodegarden.commons.lang.sequence;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class SnowflakeSequenceManagerTests extends SequenceManagerTests {

	AtomicInteger i_multi = new AtomicInteger();
	ArrayList<Tuple2<Long, Long>> list = new ArrayList<>();
	{
		for (long i = 0; i < 32; i++) {
			for (long j = 0; j < 32; j++) {
				list.add(Tuples.of(i, j));
			}
		}
		System.out.println("SnowflakeSequenceManagerTests Snowflake number size:"+list.size());
	}

	@Override
	protected SequenceManager getForOneProcess() {
		return new SnowflakeSequenceManager(31, 31);
	}

	@Override
	protected SequenceManager newForMultiProcess() {
		Tuple2<Long, Long> tuple2 = list.get(i_multi.getAndIncrement());
		System.out.println("SnowflakeSequenceManagerTests newForMultiProcess:"+tuple2);
		return new SnowflakeSequenceManager(tuple2.getT1(), tuple2.getT2());
	}
}

package io.github.icodegarden.commons.kafka.reliability;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class PropertiesConstants {

	public static final Tuple2<String, String> CLIENT_NAME = Tuples.of("io.kafka.client.name", SystemUtils.getIp());

	public static final Tuple2<String, Integer> HANDLERECORDS_THREADPOOL_CORESIZE = Tuples.of(
			"io.kafka.record.handle.threadpool.coreSize", Math.max(5, Runtime.getRuntime().availableProcessors() + 1));

	public static final Tuple2<String, Integer> HANDLERECORDS_THREADPOOL_MAXSIZE = Tuples.of(
			"io.kafka.record.handle.threadpool.maxSize", Math.max(10, Runtime.getRuntime().availableProcessors() + 1));

	public static final Tuple2<String, Long> HANDLERECORDS_THREADPOOL_KEEPALIVEMILLIS = Tuples
			.of("io.kafka.record.handle.threadpool.keepAliveMillis", 600000L);
	/**
	 * 建议不做修改，因为每个批次的预处理必须等待全部完成的结果，如果max.poll.records小于这个数量，将无法提升至maxsize
	 */
	public static final Tuple2<String, Integer> HANDLERECORDS_THREADPOOL_QUEUESIZE = Tuples
			.of("io.kafka.record.handle.threadpool.queueSize", 0);

	public static final Tuple2<String, String> HANDLERECORDS_THREADPOOL_NAMEPREFIX = Tuples
			.of("io.kafka.record.handle.threadpool.namePrefix", "io.kafka.record.handle.threadpool");

	public static final Tuple2<String, RejectedExecutionHandler> HANDLERECORDS_THREADPOOL_REJECTEDPOLICY = Tuples
			.of("io.kafka.record.handle.threadpool.rejectedPolicy", new ThreadPoolExecutor.CallerRunsPolicy());

	public static final Tuple2<String, Long> POLL_TIMEOUTMILLIS = Tuples.of("io.kafka.poll.timeoutMillis", 100L);

	public static final Tuple2<String, Class<? extends ReliabilityProcessor>> RECORD_RELIABILITY_PROCESSOR = Tuples
			.of("io.kafka.record.reliability.processor", BatchCompletionReliabilityProcessor.class);
}

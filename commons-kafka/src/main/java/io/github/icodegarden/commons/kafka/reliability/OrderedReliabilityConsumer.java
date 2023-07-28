package io.github.icodegarden.commons.kafka.reliability;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import io.github.icodegarden.commons.kafka.ConsumerRecordTask;
import io.github.icodegarden.commons.kafka.RecordExecutor;
import io.github.icodegarden.commons.lang.algorithm.JavaStringFunction;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 * @param <V>
 */
public class OrderedReliabilityConsumer<K, V> extends ReliabilityConsumer<K, V> {

	public OrderedReliabilityConsumer(Properties props, ReliabilityHandler<K, V> recordReliabilityHandler) {
		super(props = buildConsumerProperties(props, 200), recordReliabilityHandler);
	}

	public OrderedReliabilityConsumer(Properties props, int maxParallel,
			ReliabilityHandler<K, V> recordReliabilityHandler) {
		super(props = buildConsumerProperties(props, maxParallel), recordReliabilityHandler);
	}

	private static Properties buildConsumerProperties(Properties properties, int maxParallel) {
		Properties props = new Properties();

		/**
		 * 若用户没有指定，则使用默认
		 */
		if (!properties.containsKey(PropertiesConstants.HANDLERECORDS_EXECUTOR.getT1())) {
			OrderedExecutor executor = new OrderedExecutor(maxParallel);
			props.put(PropertiesConstants.HANDLERECORDS_EXECUTOR.getT1(), executor);
		}
		
		props.put("max.poll.records", Math.max(10, maxParallel));//默认配置最少拉取10

		props.putAll(properties);// 如已配置，覆盖以上

		return props;
	}

	private static class OrderedExecutor implements RecordExecutor {

		private final List<RecordExecutor> executors;
		private final BiFunction<ConsumerRecord, List<RecordExecutor>, RecordExecutor> executorSelector;

		public OrderedExecutor(int maxParallel) {
			this.executors = Stream.generate(() -> 1).limit(maxParallel).map(i -> {
				ThreadPoolExecutor executor = new ThreadPoolExecutor(1/* 单线程 */, 1/* 单线程 */, 0, TimeUnit.SECONDS,
						new LinkedBlockingQueue<Runnable>());
				return new RecordExecutor.ThreadPoolHandleRecordExecutor(executor);
			}).collect(Collectors.toList());

			final JavaStringFunction executorSelectorHashFunc = new JavaStringFunction();

			this.executorSelector = (record, executors) -> {
//				int hash = executorSelectorHashFunc.hash(record);

				int hash;
				if (record.key() != null) {
					hash = executorSelectorHashFunc.hash(record.key());
				} else {
					hash = executorSelectorHashFunc.hash(record.partition());
				}

				int mod = Math.abs(hash) % executors.size();
				return executors.get(mod);
			};
		}

		@Override
		public void execute(ConsumerRecordTask task) {
			RecordExecutor select = executorSelector.apply(task.getRecord(), executors);
			select.execute(task);
		}

		public void shutdown() {
			executors.forEach(RecordExecutor::shutdown);
		};
	}
}

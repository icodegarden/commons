package io.github.icodegarden.commons.kafka.reliability;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.kafka.ConsumerRecordTask;
import io.github.icodegarden.commons.kafka.RecordExecutor;
import io.github.icodegarden.commons.kafka.RetryableExecutor;
import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.commons.lang.result.Result1;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractReliabilityProcessor<K, V> implements ReliabilityProcessor<K, V> {

	private static final Logger log = LoggerFactory.getLogger(AbstractReliabilityProcessor.class);
	protected static final OffsetCommitCallback OFFSETCOMMIT_CALLBACK = new OffsetCommitCallback() {
		@Override
		public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
			if (exception != null) {
				log.warn("commit offsets error,offsets:{},these offsets can be cover by next commit", offsets/* 可读 */,
						exception);
			}
			if (log.isDebugEnabled()) {
				log.debug("offsets commit callback success:{}", exception == null);
			}
		}
	};
	private static final RetryableExecutor<Boolean> RETRYABLE_EXECUTOR = new RetryableExecutor<>();// thread safe

	protected String processorName = this.getClass().getSimpleName();
	protected KafkaConsumer<K, V> consumer;
	protected ReliabilityHandler<K, V> recordReliabilityHandler;
	protected RecordExecutor handleRecordExecutor;

	/**
	 * 处理中的record数量
	 */
	protected AtomicLong processingCount = new AtomicLong(0);

	public AbstractReliabilityProcessor(KafkaConsumer<K, V> consumer, ReliabilityHandler<K, V> recordReliabilityHandler,
			Properties consumerProperties) {
		this.consumer = consumer;
		this.recordReliabilityHandler = recordReliabilityHandler;

		int coreSize = (int) consumerProperties.getOrDefault(
				PropertiesConstants.HANDLERECORDS_THREADPOOL_CORESIZE.getT1(),
				PropertiesConstants.HANDLERECORDS_THREADPOOL_CORESIZE.getT2());
		int maxSize = (int) consumerProperties.getOrDefault(
				PropertiesConstants.HANDLERECORDS_THREADPOOL_MAXSIZE.getT1(),
				PropertiesConstants.HANDLERECORDS_THREADPOOL_MAXSIZE.getT2());
		long keepAliveMillis = (long) consumerProperties.getOrDefault(
				PropertiesConstants.HANDLERECORDS_THREADPOOL_KEEPALIVEMILLIS.getT1(),
				PropertiesConstants.HANDLERECORDS_THREADPOOL_KEEPALIVEMILLIS.getT2());
		int queueSize = (int) consumerProperties.getOrDefault(
				PropertiesConstants.HANDLERECORDS_THREADPOOL_QUEUESIZE.getT1(),
				PropertiesConstants.HANDLERECORDS_THREADPOOL_QUEUESIZE.getT2());
		String prefix = (String) consumerProperties.getOrDefault(
				PropertiesConstants.HANDLERECORDS_THREADPOOL_NAMEPREFIX.getT1(),
				PropertiesConstants.HANDLERECORDS_THREADPOOL_NAMEPREFIX.getT2());
		RejectedExecutionHandler rejectedPolicy = (RejectedExecutionHandler) consumerProperties.getOrDefault(
				PropertiesConstants.HANDLERECORDS_THREADPOOL_REJECTEDPOLICY.getT1(),
				PropertiesConstants.HANDLERECORDS_THREADPOOL_REJECTEDPOLICY.getT2());

		RecordExecutor executor = (RecordExecutor) consumerProperties.getOrDefault(
				PropertiesConstants.HANDLERECORDS_EXECUTOR.getT1(), PropertiesConstants.HANDLERECORDS_EXECUTOR.getT2());

		if (executor != null) {
			handleRecordExecutor = executor;
		} else {
			if (maxSize <= 1) {
				handleRecordExecutor = new RecordExecutor.CallerRunsExecutor();
			} else {
				ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, keepAliveMillis,
						TimeUnit.MILLISECONDS,
						queueSize == 0 ? new SynchronousQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queueSize),
						new NamedThreadFactory(prefix), rejectedPolicy);
				handleRecordExecutor = new RecordExecutor.ThreadPoolHandleRecordExecutor(threadPoolExecutor);
			}
		}
	}

	/**
	 * @param records
	 */
	public abstract void handleReliability(ConsumerRecords<K, V> records);

	protected void handleReliability(ConsumerRecords<K, V> records, boolean handled) {
		handleReliability(records, records.count(), handled);
	}

	protected void handleReliability(Collection<ConsumerRecord<K, V>> records, boolean handled) {
		handleReliability(records, records.size(), handled);
	}

	/**
	 * retry and store until all success
	 */
	private void handleReliability(Iterable<ConsumerRecord<K, V>> records, int count, boolean handled) {
		if (count > 0) {
			/**
			 * CountDownLatch允许先执行countDown，因此线程池满了执行CallerRun也会正确处理
			 */
			CountDownLatch countDownLatch = new CountDownLatch(count);
			records.forEach(record -> {
				try {
					ConsumerRecordTask<K, V> task = new ConsumerRecordTask<K, V>() {
						@Override
						public void run() {
							try {
								Result1<Exception> result = RETRYABLE_EXECUTOR.execute(() -> {
									return recordReliabilityHandler.handle(record);
								}, recordReliabilityHandler.handleRetries(),
										recordReliabilityHandler.handleRetryBackoffMillis(), handled);

								if (!result.isSuccess()) {
									Exception cause = result.getT1();
									result = RETRYABLE_EXECUTOR.execute(() -> {
										return recordReliabilityHandler.primaryStore(record, cause);
									}, recordReliabilityHandler.storeRetries(),
											recordReliabilityHandler.storeRetryBackoffMillis());
								}

								if (!result.isSuccess()) {
									Exception cause = result.getT1();
									result = RETRYABLE_EXECUTOR.execute(() -> {
										return recordReliabilityHandler.secondaryStore(record, cause);
									}, recordReliabilityHandler.storeRetries(),
											recordReliabilityHandler.storeRetryBackoffMillis());
								}

								if (!result.isSuccess()) {
									try {
										recordReliabilityHandler.onStoreFailed(record, result.getT1());
									} catch (Exception e) {
										log.error("ex on storeFailed", e);
									}
								}
							} finally {
								countDownLatch.countDown();
							}
						}

						@Override
						public ConsumerRecord<K, V> getRecord() {
							return record;
						}
					};

					handleRecordExecutor.execute(task);
				} catch (Exception e) {// for rejected
					countDownLatch.countDown();
					log.error("ex on execution", e);
				}
			});
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
			}
		}
	}

	public long processingCount() {
		return processingCount.get();
	}

	public void waitProcessingComplete(long timeoutMillis) {
		long waitIntervalMillis = 100;
		long waitedMills = 0;
		while (processingCount() > 0 && waitedMills < timeoutMillis) {
			log.info("wait processing records to be complete ,processingCount:{} ,left timeout millis:{}",
					processingCount(), timeoutMillis - waitedMills);
			// 这里超时不需要很精确，sleep即可
			try {
				Thread.sleep(waitIntervalMillis);
			} catch (InterruptedException ignore) {
			}
			waitedMills += waitIntervalMillis;
		}
		if (processingCount() > 0) {
			log.warn("wait timeout , there are {} records in process", processingCount());
		}
	}

	protected Set<TopicPartition> resolveTopicPartitions(Collection<ConsumerRecord<K, V>> records) {
		return records.stream().map(r -> {
			return new TopicPartition(r.topic(), r.partition());
		}).collect(Collectors.toSet());
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}
}

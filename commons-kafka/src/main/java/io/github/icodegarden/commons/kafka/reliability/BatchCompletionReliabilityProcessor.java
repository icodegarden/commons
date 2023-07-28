package io.github.icodegarden.commons.kafka.reliability;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 总是等待每个批次的records都处理完成再poll新的 注意预估处理整个批次所需要的时间，不能大于max.poll.interval.ms
 * 
 * @author Fangfang.Xu
 *
 */
public class BatchCompletionReliabilityProcessor<K, V> extends AbstractReliabilityProcessor<K, V> {

	private static final Logger log = LoggerFactory.getLogger(BatchCompletionReliabilityProcessor.class);

	public BatchCompletionReliabilityProcessor(KafkaConsumer<K, V> consumer,
			ReliabilityHandler<K, V> recordReliabilityHandler, Properties consumerProperties) {
		super(consumer, recordReliabilityHandler, consumerProperties);
	}

	@Override
	public void handleReliability(ConsumerRecords<K, V> records) {
		if (!records.isEmpty()) {
			int recordsCount = records.count();
			// add
			processingCount.addAndGet(recordsCount);
			// 进行可靠性的完整处理
			super.handleReliability(records, false);
			// 调用结束即表示已进行了完整的可靠处理，可以进行commit了
			try {
				try {
					consumer.commitAsync(OFFSETCOMMIT_CALLBACK);
				} catch (Exception e) {
					log.warn("commit offset failed after handle record.", e);
					// commit失败则忽略，待下批覆盖式commit
				}
			} finally {
				// minus
				processingCount.addAndGet(-recordsCount);
			}
		}
	}

	@Override
	public void close() throws IOException {
		this.close(Duration.ofMillis(ReliabilityConsumer.DEFAULT_CLOSE_TIMEOUT_MS));
	}

	@Override
	public void close(long timeoutMillis) throws IOException {
		log.info("start close {} named {} ", ReliabilityProcessor.class.getSimpleName(), processorName);

		try {
			// 要等待处理中的消息处理完
			waitProcessingComplete(timeoutMillis);
			// 关闭线程池
			handleRecordExecutor.shutdown();

			log.info("commitSync before close ,timeout millis:{}", timeoutMillis);

			try {
				consumer.commitSync(Duration.ofMillis(timeoutMillis));
				log.info("commitSync offsets before close success");
			} catch (CommitFailedException e) {
				log.error("ex on commitSync offsets before close", e);
			}
		} finally {
			consumer.close(Duration.ofMillis(timeoutMillis));
			log.info("{} named {} closed ...", ReliabilityProcessor.class.getSimpleName(), processorName);
		}
	}
}

package io.github.icodegarden.commons.kafka.reliability;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.kafka.DelayedRetryableReference;
import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;

/**
 * 遍历每个record执行结果 <br>
 * 没有失败的，则提交offsets对应的topic、partition <br>
 * 如果有失败的，则pause对应的topic、partition<br>
 * pause失败，则使用完全处理
 * pause成功，在新线程中，把执行失败的record重试N次，如果还是有失败的则尝试进行持久化，一直到持久化都成功；最后把需要提交的offsets和需要resume的partitons放入队列<br>
 * 最后，总是检查并提交队列中offset并resume对应的topic、partition
 * 
 * @author Fangfang.Xu
 *
 */
public class PausePollReliabilityProcessor<K, V> extends AbstractReliabilityProcessor<K, V> {

	private static final Logger log = LoggerFactory.getLogger(PausePollReliabilityProcessor.class);

	/**
	 * 采用固定配置即够用
	 */
	private ThreadPoolExecutor handleFailedRecordsThreadPool = new ThreadPoolExecutor(0,
			Math.max(5, Runtime.getRuntime().availableProcessors() + 1), 600000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1 << 4)/* 预设对应16个分区 */,
			new NamedThreadFactory("io.kafka.failedRecords.handle.threadpool"),
			new ThreadPoolExecutor.CallerRunsPolicy()/* 必须是run */);

	/**
	 * 为什么需要以下2个队列<br>
	 * 因为处理失败的消息使用线程池再可靠处理，处理后进行commit和resume有可能失败，不能让线程长时间的循环commit、resume直到成功，因此使用队列方式不断重试
	 */
	/**
	 * 已pause了的TopicPartition
	 */
	private Queue<DelayedRetryableReference<Collection<TopicPartition>>> pausedTopicPartitionsQueue = new DelayQueue<>();
	private Queue<Map<TopicPartition, OffsetAndMetadata>> toCommitOffsetsQueue = new LinkedList<>();

	public PausePollReliabilityProcessor(KafkaConsumer<K, V> consumer,
			ReliabilityHandler<K, V> recordReliabilityHandler, Properties consumerProperties) {
		super(consumer, recordReliabilityHandler, consumerProperties);
	}

	@Override
	public void handleReliability(ConsumerRecords<K, V> records) {
		if (!records.isEmpty()) {
			int recordsCount = records.count();
			// add
			processingCount.addAndGet(recordsCount);

			final List<ConsumerRecord<K, V>> failedRecords = Collections.synchronizedList(new LinkedList<>());

			/**
			 * 先执行一次消费
			 */
			CountDownLatch countDownLatch = new CountDownLatch(recordsCount);
			records.forEach(record -> {
				try {
					handleRecordsThreadPool.execute(() -> {
						try {
							try {
								boolean success = recordReliabilityHandler.handle(record);
								if (!success) {
									failedRecords.add(record);
								}
							} catch (Exception e) {
								failedRecords.add(record);
							}
						} finally {
							countDownLatch.countDown();
						}
					});
				} catch (Exception e) {// for rejected
					failedRecords.add(record);
					countDownLatch.countDown();
					log.error("ex on execution", e);
				}
			});
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
			}

			final Map<TopicPartition, OffsetAndMetadata> toCommitOffsets = new HashMap<TopicPartition, OffsetAndMetadata>(
					1 << 4);// 通常只订阅1个topic，最多16个 partition
			records.forEach(r -> {
				// 原始消息集合的offset是递增的
				// TopicPartition实现了hashcode和equals，offset大的会覆盖小的
				toCommitOffsets.put(new TopicPartition(r.topic(), r.partition()),
						new OffsetAndMetadata(r.offset() + 1));
			});

			if (failedRecords.isEmpty()) {
				/**
				 * 消费全部成功，commit
				 */
				if (log.isDebugEnabled()) {
					log.debug("failed records is empty , commit directly");
				}
				try {
					try {
						consumer.commitAsync(toCommitOffsets, OFFSETCOMMIT_CALLBACK);
					} catch (Exception e) {
					}
				} finally {
					// minus
					processingCount.addAndGet(-recordsCount);
				}
			} else {
				/**
				 * 有失败的
				 */
				final Set<TopicPartition> toPauseTopicPartitions = resolveTopicPartitions(failedRecords);

				try {
					/**
					 * 对失败的topicPartition进行pause
					 */
					consumer.pause(toPauseTopicPartitions);
					if (log.isDebugEnabled()) {
						log.debug("pause success...,topicPartitions:{}", toPauseTopicPartitions);
					}
					/**
					 * 用处理失败消息的线程池进行可靠处理
					 * 用线程池处理的原因是为了不影响没有pause的topicPartition的poll和继续消费，否则如果使用同步处理则影响没有pause的topicPartition
					 */
					handleFailedRecordsThreadPool.execute(() -> {
						super.handleReliability(failedRecords, true);// 因为之前已经尝试消费过一次了，现在给个true标识
						toCommitOffsetsQueue.offer(toCommitOffsets);
						/**
						 * 把pause的放入延迟队列
						 */
						DelayedRetryableReference<Collection<TopicPartition>> delayedRetryableReference = new DelayedRetryableReference<>(
								toPauseTopicPartitions, pausedTopicPartitionsQueue, 0);// 0 这里不需要延迟，可以直接resume
						delayedRetryableReference.enQueue();

						// minus
						processingCount.addAndGet(-recordsCount);
						if (log.isDebugEnabled()) {
							log.debug("toCommit & paused offered...");
						}
					});
				} catch (Exception e) {
					/**
					 * 进行pause失败，只能直接使用可靠处理
					 */
					log.error("pause error,toPauseTopicPartitions:{}", toPauseTopicPartitions, e);
					super.handleReliability(failedRecords, true);
					try {
						try {
							consumer.commitAsync(toCommitOffsets, OFFSETCOMMIT_CALLBACK);
						} catch (Exception e1) {
						}
					} finally {
						// minus
						processingCount.addAndGet(-recordsCount);
					}
				}
			}
		}
		/**
		 * commit
		 */
		commitQueuedOffsets(commitOffsets -> consumer.commitAsync(commitOffsets, OFFSETCOMMIT_CALLBACK));
		/**
		 * resume（继续）pause了的topicPartition
		 */
		resumeQueuedTopicPartitions();
	}

	private void commitQueuedOffsets(Consumer<Map<TopicPartition, OffsetAndMetadata>> c) {
		Map<TopicPartition, OffsetAndMetadata> commitOffsets = null;
		while ((commitOffsets = toCommitOffsetsQueue.poll()) != null) {
			try {
				c.accept(commitOffsets);
			} catch (Exception e) {
				log.error("commit offsets error,commitOffsets:{}", commitOffsets, e);
			}
		}
	}

	private void resumeQueuedTopicPartitions() {
		DelayedRetryableReference<Collection<TopicPartition>> reference = null;
		/**
		 * 把延迟队列中pause了的可resume的进行poll（延迟时间没到的不会poll到）
		 */
		while ((reference = pausedTopicPartitionsQueue.poll()) != null) {
			Collection<TopicPartition> pausedTopicPartitions = reference.get();
			try {
				consumer.resume(pausedTopicPartitions);
				log.debug("resume batch success...,topicPartitions:{}", pausedTopicPartitions);
			} catch (Exception e) {
				/**
				 * resume失败 如果size == 1 && delayMillis > 0的继续进行enQueue 否则拆分成1个个的进行resume
				 */
				log.error("resume batch error,topicPartitions:{}", pausedTopicPartitions, e);
				/**
				 * whats old delay: size == 1 && delayMillis > 0<br>
				 */
				if (pausedTopicPartitions.size() == 1 && reference.getDelayMillis() > 0) {// not new
					reference.enQueue();
				} else {
					/**
					 * resume by every one and enqueue on fail
					 */
					pausedTopicPartitions.forEach(topicPartition -> {
						List<TopicPartition> one = Arrays.asList(topicPartition);
						try {
							consumer.resume(one);
							log.debug("resume one success...,topicPartition:{}", topicPartition);
						} catch (Exception e1) {
							/**
							 * resume失败以size 1 的方式进行队列
							 */
							log.error("resume one error,topicPartition:{}", topicPartition, e1);
							/**
							 * 因为resume失败了，所以需要使用默认的延迟30秒进队列
							 */
							new DelayedRetryableReference<>(one, pausedTopicPartitionsQueue).enQueue();
						}
					});
				}
			}
		}
	}

	@Override
	public void close() throws IOException {
		this.close(Duration.ofMillis(ReliabilityConsumer.DEFAULT_CLOSE_TIMEOUT_MS));
	}

	/**
	 * 检测处理中的records数量，等待处理完毕或直到超时（前置已不会再poll到新的records） 关闭record线程池、失败record线程池
	 * commit queue中offsets 清理queue 关闭consumer
	 */
	@Override
	public void close(long timeoutMillis) throws IOException {
		log.info("start close {} named {} ", ReliabilityProcessor.class.getSimpleName(), processorName);

		try {
			waitProcessingComplete(timeoutMillis);

			handleRecordsThreadPool.shutdown();
			handleFailedRecordsThreadPool.shutdown();
		} finally {
			log.info("commitSync offsets...");
			commitQueuedOffsets(commitOffsets -> {
				// must be sync
				try {
					consumer.commitSync(commitOffsets, Duration.ofMillis(timeoutMillis));
					log.info("commitSync offsets before close success,commitOffsets:{}", commitOffsets);
				} catch (CommitFailedException e) {
					log.error("commitSync offsets before close error,commitOffsets:{}", commitOffsets, e);
					/**
					 * 只要不发生不可恢复的错误，commit会一直重试直到超时；<br>
					 * 万一发生不可恢复的错误，跳过这次commit让下一次来弥补
					 * 
					 * This exception is raised when an offset commit with
					 * {@link KafkaConsumer#commitSync()} fails with an unrecoverable error. This
					 * can happen when a group rebalance completes before the commit could be
					 * successfully applied. In this case, the commit cannot generally be retried
					 * because some of the partitions may have already been assigned to another
					 * member in the group.
					 */
				}
			});

			toCommitOffsetsQueue.clear();
			pausedTopicPartitionsQueue.clear();

			/**
			 * This method waits up to {@code timeout} for the consumer to complete pending
			 * commits and leave the group. If auto-commit is enabled, this will commit the
			 * current offsets if possible within the timeout. If the consumer is unable to
			 * complete offset commits and gracefully leave the group before the timeout
			 * expires, the consumer is force closed
			 */
			consumer.close(Duration.ofMillis(timeoutMillis));
			log.info("{} named {} closed ...", ReliabilityProcessor.class.getSimpleName(), processorName);
		}
	}
}

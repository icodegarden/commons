package io.github.icodegarden.commons.kafka.reliability;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 * @param <V>
 */
public class ReliabilityConsumer<K, V> extends KafkaConsumer<K, V> {

	private static final Logger log = LoggerFactory.getLogger(ReliabilityConsumer.class);

	public static final long DEFAULT_CLOSE_TIMEOUT_MS = 30 * 1000;

	private final String name;
	private ReliabilityProcessor<K, V> recordReliabilityProcessor;
	private boolean closed;
	private Duration timeout = Duration.ofMillis(DEFAULT_CLOSE_TIMEOUT_MS);
	private ReentrantLock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();

	public ReliabilityConsumer(Properties props, ReliabilityHandler<K, V> recordReliabilityHandler) {
		super(props = buildConsumerProperties(props));
		name = (String) props.getOrDefault(PropertiesConstants.CLIENT_NAME.getT1(),
				PropertiesConstants.CLIENT_NAME.getT2());
		try {
			Class<ReliabilityProcessor<K, V>> cla = (Class<ReliabilityProcessor<K, V>>) props.getOrDefault(
					PropertiesConstants.RECORD_RELIABILITY_PROCESSOR.getT1(),
					PropertiesConstants.RECORD_RELIABILITY_PROCESSOR.getT2());

			this.recordReliabilityProcessor = cla
					.getDeclaredConstructor(KafkaConsumer.class, ReliabilityHandler.class, Properties.class)
					.newInstance(this, recordReliabilityHandler, props);

			if (recordReliabilityProcessor instanceof AbstractReliabilityProcessor) {
				((AbstractReliabilityProcessor) recordReliabilityProcessor).setProcessorName(name);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static Properties buildConsumerProperties(Properties properties) {
		Properties props = new Properties();
		props.put("fetch.min.bytes", 1);// ??????
		props.put("fetch.max.bytes", 52428800);// ??????
		props.put("fetch.max.wait.ms", 500);
		props.put("max.poll.interval.ms", 600000);// ??????
		props.put("max.poll.records", 10);// ??????????????????????????????????????????
		props.put("heartbeat.interval.ms", 3000);//
		props.put("session.timeout.ms", 10000);//
		props.put("max.partition.fetch.bytes", 1048576);// ?????????broker??????????????????
		props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RoundRobinAssignor");
		props.put("auto.offset.reset", "latest");
		props.put("connections.max.idle.ms", Integer.MAX_VALUE);// ??????

		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			props.put("client.id", hostName + "-" + properties.getOrDefault(PropertiesConstants.CLIENT_NAME.getT1(),
					PropertiesConstants.CLIENT_NAME.getT2()));
		} catch (UnknownHostException e) {
		}

		props.putAll(properties);// ???????????????????????????

		props.put("enable.auto.commit", false);// ????????????????????????
		return props;
	}

	/**
	 * @throws IllegalStateException running || not sub ...
	 */
	public void consume(long pollTimeoutMillis) throws IllegalStateException {
		if (closed) {
			throw new IllegalStateException("consumer is not running");
		}
		try {
			while (!closed) {
				ConsumerRecords<K, V> records = this.poll(Duration.ofMillis(pollTimeoutMillis));
				recordReliabilityProcessor.handleReliability(records);
			}
		} catch (WakeupException e) {
		} finally {
			try {
				recordReliabilityProcessor.close(timeout);
			} catch (Exception e) {
				log.error("ex on close {}", ReliabilityProcessor.class.getSimpleName(), e);
			} finally {
				try {
					lock.lock();
					condition.signal();
				} finally {
					lock.unlock();
				}
			}
		}
	}

	/**
	 * ?????????
	 */
	@Override
	public void close() {
		this.close(timeout);
	}

	/**
	 * ?????????
	 */
	@Override
	public void close(Duration timeout) {
		if (this.closed) {
			return;
		}
		this.closed = true;
		this.timeout = timeout;
		log.info("consumer named {} received close signal , wait to close ...", name);
		this.wakeup();

		try {
			lock.lock();
			condition.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		} finally {
			lock.unlock();
		}
	}

	/**
	 * ??????????????????????????????????????????commit??????
	 */
	public void forceClose() {
		this.closed = true;
		log.info("consumer named {} received forceClose signal", name);

		this.wakeup();
		super.close(timeout);//????????????super.close(); ??????????????????this.close(Duration timeout);
	}
}

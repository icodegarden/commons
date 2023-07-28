package io.github.icodegarden.commons.kafka.reliability;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.kafka.KafkaException;
import io.github.icodegarden.commons.lang.TimeoutableCloseable;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 * @param <V>
 */
public class ReliabilityProducer<K, V> implements TimeoutableCloseable {

	private static final Logger log = LoggerFactory.getLogger(ReliabilityProducer.class);

	private static final AtomicLong CLINET_SEQ = new AtomicLong();

	private final String name;
	private final KafkaProducer<K, V> producer;

	public ReliabilityProducer(KafkaProducer<K, V> producer) {
		this.producer = producer;
		name = PropertiesConstants.CLIENT_NAME.getT2();
	}

	public ReliabilityProducer(Properties properties) {
//		Object bootstrapServers = properties.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG);

		name = (String) properties.getOrDefault(PropertiesConstants.CLIENT_NAME.getT1(),
				PropertiesConstants.CLIENT_NAME.getT2());

		Properties props = new Properties();
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			props.put("client.id", hostName + "-" + name + "-" + CLINET_SEQ.incrementAndGet());
		} catch (UnknownHostException e) {
		}
		props.put("acks", "all");// 所有同步副本复制成功
		props.put("retries", 2);// 重试
		props.put("max.request.size", 1000012);// 减小一些，根据消息量评估是否需要加大，但必须小于 broker的 message.max.bytes默认1000012
		props.put("delivery.timeout.ms", 3000);// 同步发送，实时性要求高
		props.put("linger.ms", 0);// 同步发送，停留没用
		props.put("request.timeout.ms", 2500);
		props.put("buffer.memory", 33554432);// 同步的没意义
		props.put("batch.size", 16384);// 同步的没意义
		props.put("max.block.ms", 3000);// 减少阻塞时间，同步的没意义
		props.put("compression.type", "lz4");

		props.putAll(properties);

		this.producer = new KafkaProducer<>(props);
	}

	public RecordMetadata sendSync(ProducerRecord<K, V> record) throws KafkaException {
		try {
			return producer.send(record).get();
		} catch (Exception e) {
			throw new KafkaException(e);
		}
	}

	public Future<RecordMetadata> sendCallback(ProducerRecord<K, V> record, Callback callback) throws KafkaException {
		try {
			return producer.send(record, callback);
		} catch (Exception e) {
			throw new KafkaException(e);
		}
	}

	public RecordMetadata sendSyncOrdered(ProducerRecord<K, V> record) throws KafkaException {
		try {
			validateOrdered(record);
			return producer.send(record).get();
		} catch (Exception e) {
			throw new KafkaException(e);
		}
	}

	public Future<RecordMetadata> sendCallbackOrdered(ProducerRecord<K, V> record, Callback callback)
			throws KafkaException {
		try {
			validateOrdered(record);
			return producer.send(record, callback);
		} catch (Exception e) {
			throw new KafkaException(e);
		}
	}

	private void validateOrdered(ProducerRecord<K, V> record) {
		Assert.isTrue(record.key() != null || record.partition() != null,
				"key or partition must present on send ordered.");
	}

	@Override
	public void close() throws IOException {
		this.close(Duration.ofMillis(Long.MAX_VALUE));
	}

	@Override
	public void close(long timeoutMillis) throws IOException {
		log.info("start close {} named {} ", ReliabilityProducer.class.getSimpleName(), name);
		/**
		 * blocks until all previously sent requests complete.
		 */
		producer.close(Duration.ofMillis(timeoutMillis));
		log.info("{} named {} closed ...", ReliabilityProducer.class.getSimpleName(), name);
	}
}

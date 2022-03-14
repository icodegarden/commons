package io.github.icodegarden.commons.kafka.scene.log;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.github.icodegarden.commons.kafka.ProducerTest;

public class LogProducer {

	public static void main(String[] args) {
		Properties props = ProducerTest.buildProducerProperties();
		props.put("bootstrap.servers", "192.168.229.134:9092");
		props.put("acks", "0");// 允许丢失
		props.put("retries", 0);// 不重试
		props.put("max.request.size", 1048576);// 根据消息量评估是否需要加大
		props.put("delivery.timeout.ms", 120000);
		props.put("linger.ms", 50);// 不要求实时发送，提高吞吐量
		props.put("request.timeout.ms", 3000);
		props.put("buffer.memory", 33554432);// 根据消息量评估是否需要加大
		props.put("batch.size", 16384);
		props.put("max.block.ms", 3000);// 减少阻塞时间
		props.put("compression.type", "lz4");// 压缩log /**压缩类型 none, gzip, snappy, lz4, or zstd . 在吞吐方面：LZ4>Snappy>zstd、GZIP；在压缩比方面：zstd>lz4>gzip>snappy*/

		KafkaProducer<String, String> producer = new KafkaProducer<>(props);
		System.out.println("producer created...");
		String log = "[2020-03-19 17:59:48,972] INFO Socket error occurred: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		AtomicInteger send = new AtomicInteger();
		for (int i = 0; i < 1000; i++) {
			try {
				producer.send(new ProducerRecord<String, String>("test-log", Integer.toString(i), log + "-" + i),
						(r, e) -> {
							send.getAndIncrement();
							if (e != null) {
								//  回调异常
								System.out.println("callback error:" + e);
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
				//  不可重试异常
			}
		}
		producer.close(Duration.ofMillis(30000));// blocks until all previously sent requests complete.
		System.out.println("producer closed...");
		System.out.println("total send:" + send.get());
	}
}

package io.github.icodegarden.commons.kafka.scene.log;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.kafka.ConsumerTest;

public class LogConsumer {
	
	static class S1 implements Closeable{
		void m() {
			System.out.println("s1 m...");
		}
		@Override
		public void close() throws IOException {
			System.out.println("s1 close...");
		}
	}
	static class S2 implements Closeable{
		S1 s1;
		S2(S1 s1){
			this.s1 = s1;
		}
		void m() {
			System.out.println("s2 m...");
		}
		@Override
		public void close() throws IOException {
			System.out.println("s2 close...");
			s1.close();
		}
	}

	static boolean closed = false;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		try(S1 s1 = new S1();S2 s2 = new S2(s1);){
			s1.m();
			s2.m();
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(1);
		
		
		Properties props = ConsumerTest.buildConsumerProperties();

		props.put("bootstrap.servers", "192.168.229.134:9092");
		props.put("fetch.min.bytes", 1048576);// 不要求实时性，适当增大批量
		props.put("fetch.max.bytes", 5242880);// 不要太大，比默认小10倍
		props.put("fetch.max.wait.ms", 1000);// 不要求实时性，适当增大等待
		/** poll()之间的最大间隔时间。如果在此超时过期之前未调用poll（），则认为consumer不可用，触发再均衡 */
		props.put("max.poll.interval.ms", 300000);// Type: intDefault: 300000 Valid Values: [1,...]Importance: medium
		props.put("max.poll.records", 300);// 适当减少数量，不采取特别措施，让300s够用
		props.put("group.id", "xff");//
		props.put("heartbeat.interval.ms", 9000);// 增大心跳，不需要很及时的再均衡
		props.put("session.timeout.ms", 30000);// 增大
		props.put("max.partition.fetch.bytes", 3048576);// 增大，防止单个日志太大
		props.put("enable.auto.commit", true);// Type: booleanDefault: trueValid Values: Importance: medium
		props.put("auto.commit.interval.ms", 5000);// Type: intDefault: 5000 Valid Values: [0,...]Importance: low
		props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RoundRobinAssignor");
		props.put("auto.offset.reset", "latest");// Type: string Default: latest Valid Values: [latest, earliest,none]Importance: medium

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList("test-log"));

		// 按实际配置
		int coreSize = 50;
		int maxSize = 50;
		int queueSize = 300;// 与max.poll.records一致
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(queueSize),
				/* fix use NamedThreadFactory */Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy());

		try {
			while (!closed) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				for (ConsumerRecord<String, String> record : records) {
					//
					threadPoolExecutor.execute(() -> {
						System.out.printf("topic = %s, offset = %d,partition=%s, key = %s, value = %s%n",
								record.topic(), record.offset(), record.partition(), record.key(), record.value());
						try {
							Thread.sleep(1000);// 模拟每条处理时间
						} catch (InterruptedException e) {
						}
					});
				}
			}
		} finally {
			/**
			 * This method waits up to {@code timeout} for the consumer to complete pending
			 * commits and leave the group. If auto-commit is enabled, this will commit the
			 * current offsets if possible within the timeout. If the consumer is unable to
			 * complete offset commits and gracefully leave the group before the timeout
			 * expires, the consumer is force closed
			 */
			consumer.close(Duration.ofMillis(30000));
			System.out.println("consumer closed...");
		}
	}

	public static void close() {
		closed = true;
	}
}

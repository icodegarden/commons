package io.github.icodegarden.commons.kafka.scene.corebusiness;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.github.icodegarden.commons.kafka.UnRetryableException;
import io.github.icodegarden.commons.kafka.reliability.ReliabilityHandler;
import io.github.icodegarden.commons.kafka.reliability.OrderedReliabilityConsumer;
import io.github.icodegarden.commons.kafka.reliability.ReliabilityConsumer;
import io.github.icodegarden.commons.kafka.reliability.ReliabilityProducer;
import io.github.icodegarden.commons.kafka.scene.corebusiness.CoreBusinessProducerTests.OrderDetail;

public class OrderedCoreBusinessConsumerTests {
	
	static ReliabilityProducer<Integer, OrderDetail> producer;

	public static void main(String[] args) throws Exception {
		Properties props1 = new Properties();
		props1.put("bootstrap.servers", "192.168.184.129:9092");
		props1.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
		props1.put("value.serializer", "io.github.icodegarden.commons.kafka.scene.corebusiness.CoreBusinessProducerTests$ObjectSerializer");

		producer = new ReliabilityProducer<>(props1);
		
		
		Properties props = new Properties();
		props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
		props.put("value.deserializer", "io.github.icodegarden.commons.kafka.scene.corebusiness.CoreBusinessProducerTests$ObjectDeserializer");
		props.put("bootstrap.servers", "192.168.184.129:9092");// 
		props.put("group.id", "xff");//
		props.put("client.id", "xff-0");//
		props.put("max.partition.fetch.bytes", 10485760);//10M 10倍
		
//		props.put(PropertiesConstants.RECORD_RELIABILITY_PROCESSOR.getKey(), CompletionBeforePollRecordReliabilityProcessor.class);

		ReliabilityConsumer<Integer, OrderDetail> consumer = new OrderedReliabilityConsumer<>(props, 200, recordReliabilityHandler);

		consumer.subscribe(Arrays.asList("test-corebusiness"));

		new Thread() {
			public void run() {
				try {
					System.in.read();
					consumer.close();
				} catch (IOException e) {
				}
			};
		}.start();

		consumer.consume(100);
		System.out.println("consumer exit.");
	}

	static ReliabilityHandler<Integer, OrderDetail> recordReliabilityHandler = new ReliabilityHandler<Integer, OrderDetail>() {
		@Override
		public boolean handle(ConsumerRecord<Integer, OrderDetail> record) throws UnRetryableException {
//			System.out.printf("topic = %s, offset = %d,partition=%s, key = %s, value = %s%n", record.topic(),
//					record.offset(), record.partition(), record.key(), record.value());
			
			System.out.println("User:"+record.value().getUserId()+", OrderNum"+record.value().getOrderNum());
			
			try {
				Thread.sleep(100);// 模拟每条处理时间
			} catch (Exception e) {
			}
			return true;
		}
		
		@Override
		public void onStoreFailed(ConsumerRecord<Integer, OrderDetail> failedRecord, Throwable e) {
			System.err.println("storeFailed..." + failedRecord);
		}

		@Override
		public boolean secondaryStore(ConsumerRecord<Integer, OrderDetail> failedRecord, Throwable e)
				throws UnRetryableException {
			if (new Random().nextInt(12) == 0) {
				throw new RuntimeException("secondaryStore failed");
			}
			if (new Random().nextInt(2) == 0) {
				return true;
			}
			return false;
		}

		@Override
		public boolean primaryStore(ConsumerRecord<Integer, OrderDetail> failedRecord, Throwable e)
				throws UnRetryableException {
			if (new Random().nextInt(12) == 0) {
				throw new RuntimeException("primaryStore failed");
			}
			if (new Random().nextInt(2) == 0) {
				OrderDetail value = failedRecord.value();
				int key = failedRecord.key();
				
				ProducerRecord<Integer, OrderDetail> record = new ProducerRecord<Integer, OrderDetail>("dead-queue", key, value);
				try {
					producer.sendSync(record);
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
				return true;
			}
			return false;
		}

	};

}

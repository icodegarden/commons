package io.github.icodegarden.commons.kafka.scene.corebusiness;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;

import io.github.icodegarden.commons.kafka.reliability.ReliabilityProducer;
import io.github.icodegarden.commons.kafka.scene.corebusiness.CoreBusinessProducerTests.OrderDetail;

public class OrderedCoreBusinessProducerTests {

	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.184.129:9092");
		props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
		props.put("value.serializer", "io.github.icodegarden.commons.kafka.scene.corebusiness.CoreBusinessProducerTests$ObjectSerializer");

		ReliabilityProducer<Integer, OrderDetail> producer = new ReliabilityProducer<>(props);

		OrderDetail orderDetail = new OrderDetail();

		/**
		 * 发10个用户，每个用户10个顺序消息。先发送完毕再起消费者，数据观测效果更佳
		 */
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				orderDetail.setOrderNum("orderNum-" + j);
				orderDetail.setUserId(i);
				try {
					producer.sendSync(new ProducerRecord<Integer, OrderDetail>("test-corebusiness", (int)orderDetail.getUserId(), orderDetail));
					System.out.println("send index:" + i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		producer.close(Duration.ofMillis(30000));
		// ？ 列出kafka所有可重试和不可重试异常
	}

}

package io.github.icodegarden.commons.rabbitmq;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 测试topic 匹配routing key 的功能 topic功能强大 "#"规则时相当于fanout 全匹配时相当于direct
 * 
 * @author Fangfang.Xu
 *
 */
public class TopicTest {

	private static final String EXCHANGE_NAME = "topic_logs";
	private static final String EXCHANGE_TYPE = "topic";

	@Test
	public void testSend() throws Exception {
		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("10.200.154.151", "root",
				"123456");
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

		String[] routingKeys = { "quick.orange.one", "quick.orange.rabbit", "lazy.orange.elephant" };
		for (;;) {
			Random random = new Random();
			int i = random.nextInt(3);
			String routingKey = routingKeys[i];
			String message = "topic  " + routingKey;

			channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
			Thread.sleep(2000);
		}
	}

	@Test
	public void testRecv() throws Exception {
		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("10.200.154.151", "root",
				"123456");
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

		// 第一个queue
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, EXCHANGE_NAME, "*.orange.*");
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" *.orange.* Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);

		// 第二个queue
		queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, EXCHANGE_NAME, "*.*.rabbit");
		channel.queueBind(queueName, EXCHANGE_NAME, "lazy.#");
		consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(
						" *.*.rabbit | lazy.#   Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);

		Thread.sleep(300000);
	}
}

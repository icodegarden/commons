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

public class RoutingTest {

	private static final String EXCHANGE_NAME = "direct_logs";
	private static final String EXCHANGE_TYPE = "direct";

	@Test
	public void testSendLog() throws Exception {
		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("10.200.154.151", "root",
				"123456");
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

		String[] severitys = { "info", "warn", "error" };
		String[] msgs = { "info-log", "warn-log", "error-log" };
		for (;;) {
			Random random = new Random();
			int i = random.nextInt(3);
			String severity = severitys[i];
			String message = msgs[i];

			channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
			System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
			Thread.sleep(1000);
		}
	}

	// 多个queue可以bind同一个 routing key,一个queue可以band多个routing key
	@Test
	public void testRecvLog() throws Exception {
		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("10.200.154.151", "root",
				"123456");
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

		// 处理error 的 queue
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, EXCHANGE_NAME, "error");
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.err.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);

		// 处理其他的queue
		queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, EXCHANGE_NAME, "info");
		channel.queueBind(queueName, EXCHANGE_NAME, "warn");
		channel.queueBind(queueName, EXCHANGE_NAME, "error");
		consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);

		Thread.sleep(300000);
	}
}

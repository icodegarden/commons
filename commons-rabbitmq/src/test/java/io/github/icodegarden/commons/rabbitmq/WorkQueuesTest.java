package io.github.icodegarden.commons.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 测试发送一个消息，只被其中一个消费者消费，且ack是手动，如果任务执行一半消费者down，消费将被再发送给其他服务的消费者
 * 
 * @author Fangfang.Xu
 *
 */
public class WorkQueuesTest {

	private final static String QUEUE_NAME = "hello";

	private ConnectionFactory createConnectionFactory() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("10.200.154.151");
		factory.setUsername("root");
		factory.setPassword("123456");
		return factory;
	}

	@Test
	public void testSend() throws java.io.IOException, TimeoutException {
		Connection connection = createConnectionFactory().newConnection();
		Channel channel = connection.createChannel();

		String message = getMessage(new String[] {});

		channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
		System.out.println(" [x] Sent '" + message + "'");
	}

	@Test
	public void testConsume() throws Exception {
		Connection connection = createConnectionFactory().newConnection();
		final Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);

		channel.basicQos(1); // accept only one unack-ed message at a time (see below)

		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");

				System.out.println(" [x] Received '" + message + "'");
				try {
					doWork(message);
				} finally {
					System.out.println(" [x] Done");
					channel.basicAck(envelope.getDeliveryTag(), false);// 手动ack
				}
			}
		};
		boolean autoAck = false;// 手动ack
		channel.basicConsume(QUEUE_NAME, autoAck, consumer);

		Thread.sleep(300000);
	}

	private static String getMessage(String[] strings) {
		if (strings.length < 1)
			return "Hello World!";
		return joinStrings(strings, " ");
	}

	private static String joinStrings(String[] strings, String delimiter) {
		int length = strings.length;
		if (length == 0)
			return "";
		StringBuilder words = new StringBuilder(strings[0]);
		for (int i = 1; i < length; i++) {
			words.append(delimiter).append(strings[i]);
		}
		return words.toString();
	}

	private static void doWork(String task) {
		for (char ch : task.toCharArray()) {
			if (ch == '.')
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}
}

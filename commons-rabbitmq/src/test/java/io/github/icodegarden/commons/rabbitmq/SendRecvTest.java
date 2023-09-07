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

//最简单的 发送-接收
public class SendRecvTest {

	private final static String QUEUE_NAME = "hello";

	private ConnectionFactory createConnectionFactory() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.184.129");
//		factory.setUsername("root");
//		factory.setPassword("123456");
		return factory;
	}

	@Test
	public void testSend() throws java.io.IOException, TimeoutException {
		ConnectionFactory factory = createConnectionFactory();
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		String message = "Hello World!";
		channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
		System.out.println(" [x] Sent '" + message + "'");

		channel.close();
		connection.close();
	}

	@Test
	public void testRecv() throws Exception {
		ConnectionFactory factory = createConnectionFactory();
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		// 持续接受
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + message + "'");
			}
		};
		channel.basicConsume(QUEUE_NAME, true, consumer);

		Thread.sleep(30000);
	}
}

package io.github.icodegarden.commons.rabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RPCTest {

	private static final String RPC_QUEUE_NAME = "rpc_queue";

	@Test
	public void testServer() throws Exception {
		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("192.168.1.106", "root",
				"123456");

		Connection connection = connectionFactory.newConnection();
		final Channel channel = connection.createChannel();

		channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

		channel.basicQos(3);// 同时接受3个消息

		System.out.println(" [x] Awaiting RPC requests");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
						.correlationId(properties.getCorrelationId()).build();
				String message = new String(body, "UTF-8");
				// 如果不使用异步线程，即使basicQos设置>1，handleDelivery 整个方法依次也只有一个消息可被处理
				new Thread() {
					public void run() {
						try {
							System.out.println(" recv (" + message + ")");
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} finally {
							try {
								channel.basicPublish("", properties.getReplyTo(), replyProps,
										(message + " 200").getBytes("UTF-8"));
								channel.basicAck(envelope.getDeliveryTag(), false);
								// RabbitMq consumer worker thread notifies the RPC
								// server owner thread
								synchronized (this) {
									this.notify();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
				}.start();
			}
		};
		channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

		// Wait and be prepared to consume the message from RPC client.
		while (true) {
			synchronized (consumer) {
				try {
					consumer.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void testClient() throws Exception {
		String requestQueueName = RPC_QUEUE_NAME;

		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("192.168.1.106", "root",
				"123456");
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		String replyQueueName = channel.queueDeclare().getQueue();

		class C {
			String corrId;
			String result;
		}

		List<C> cs = new ArrayList<>();

		channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				for (C c : cs) {
					if (c.corrId.equals(properties.getCorrelationId())) {
						c.result = new String(body, "UTF-8");
						synchronized (c) {
							c.notify();
						}
					}
				}
			}
		});

		for (;;) {
			for (int i = 0; i < 5; i++) {
				new Thread() {
					public void run() {
						try {
							final String corrId = UUID.randomUUID().toString();
							final String message = "msg :" + corrId;

							AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId)
									.replyTo(replyQueueName).build();

							C c = new C();
							c.corrId = corrId;
							cs.add(c);

							channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

							synchronized (c) {
								c.wait();
							}
							System.out.println("返回值=" + c.result);
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				}.start();
			}
			Thread.sleep(2000);
		}
	}
}

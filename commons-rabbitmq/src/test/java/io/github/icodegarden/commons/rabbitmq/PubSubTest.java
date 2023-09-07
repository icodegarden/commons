package io.github.icodegarden.commons.rabbitmq;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 测试 发布/订阅 一个发送，所有订阅的全部都收到 发布者只关心把消息发送到exchange 订阅者把各自的queue
 * bind到发布者的exchange上，注意定义的queue是唯一、自动删除、非持久化的
 * 
 * @author Fangfang.Xu
 *
 */
public class PubSubTest {

	String exchangeType = "fanout";// 扇出

	@Test
	public void testPub() throws Exception {
//		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("192.168.184.129", "root",
//				"123456");
		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("192.168.184.129");
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		String exchange = "pubsub-test";
		channel.exchangeDeclare(exchange, exchangeType);
		String msg = "pubsub-test";

		channel.basicPublish(exchange, "", null, msg.getBytes());

		channel.close();
		connection.close();
	}

	@Test
	public void testSub() throws Exception {
//		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("192.168.184.129", "root",
//				"123456");
		ConnectionFactory connectionFactory = ConnectionFactoryUtil.getConnectionFactory("192.168.184.129");
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();

		String exchange = "pubsub-test";
		channel.exchangeDeclare(exchange, exchangeType);// sub也应该定义该exchange，因为sub可能比pub先启动

		// 定义唯一的、自动删除、不持久化的queue declare a server-named exclusive, autodelete,
		// non-durable queue.
		String queuename = channel.queueDeclare().getQueue();
		channel.queueBind(queuename, exchange, "");// bind到exchange
		channel.basicConsume(queuename, true, new DefaultConsumer(channel) {// autoAck应该为true，pubsub是广播模式，不ack也没意义
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("接收到消息：" + new String(body));
			}
		});
		Thread.sleep(300000);
	}
}

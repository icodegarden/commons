package io.github.icodegarden.commons.kafka.scene.corebusiness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import io.github.icodegarden.commons.kafka.reliability.ReliabilityProducer;

public class CoreBusinessProducerTests {

	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.184.129:9092");
		props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
		props.put("value.serializer", "io.github.icodegarden.commons.kafka.scene.corebusiness.CoreBusinessProducerTests$ObjectSerializer");

		ReliabilityProducer<Integer, OrderDetail> producer = new ReliabilityProducer<>(props);

		OrderDetail orderDetail = new OrderDetail();

		for (int i = 0; i < 10; i++) {
			orderDetail.setOrderNum("orderNum-" + i);
			try {
				producer.sendSync(new ProducerRecord<Integer, OrderDetail>("test-corebusiness", i, orderDetail));
				System.out.println("send index:" + i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		producer.close(Duration.ofMillis(30000));
		// ？ 列出kafka所有可重试和不可重试异常
	}

	public static class ObjectSerializer implements Serializer<Serializable> {
		@Override
		public byte[] serialize(String topic, Serializable data) {
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);) {

				objectOutputStream.writeObject(data);
				return byteArrayOutputStream.toByteArray();
			} catch (IOException e) {
				throw new SerializationException(String.format("Error when serializing %s to byte[],topic:%s",
						data.getClass().getName(), topic));
			}
		}
	}

	public static class ObjectDeserializer implements Deserializer<Object> {
		@Override
		public Object deserialize(String topic, byte[] data) {
			try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
					ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);) {
				return objectInputStream.readObject();
			} catch (Exception e) {
				throw new SerializationException(
						String.format("Error when deserializing byte[] to OrderDetail,topic:%s", topic));
			}
		}
	}

	public static class OrderDetail implements Serializable {
		private static final long serialVersionUID = 1L;

		double amount = 100.5;
		long userId = 1;
		long goodsId = 2;
		String brand = "apple";
		String goodsType = "phone";
		int count = 1;
		ZonedDateTime createdAt = ZonedDateTime.now();
		String orderNum;

		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = amount;
		}

		public long getUserId() {
			return userId;
		}

		public void setUserId(long userId) {
			this.userId = userId;
		}

		public long getGoodsId() {
			return goodsId;
		}

		public void setGoodsId(long goodsId) {
			this.goodsId = goodsId;
		}

		public String getBrand() {
			return brand;
		}

		public void setBrand(String brand) {
			this.brand = brand;
		}

		public String getGoodsType() {
			return goodsType;
		}

		public void setGoodsType(String goodsType) {
			this.goodsType = goodsType;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public ZonedDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(ZonedDateTime createdAt) {
			this.createdAt = createdAt;
		}

		public String getOrderNum() {
			return orderNum;
		}

		public void setOrderNum(String orderNum) {
			this.orderNum = orderNum;
		}

		@Override
		public String toString() {
			return "OrderDetail [amount=" + amount + ", userId=" + userId + ", goodsId=" + goodsId + ", brand=" + brand
					+ ", goodsType=" + goodsType + ", count=" + count + ", createdAt=" + createdAt + ", orderNum="
					+ orderNum + "]";
		}

	}

}

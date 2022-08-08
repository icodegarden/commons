package io.github.icodegarden.commons.kafka;


import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Cluster;
import org.junit.jupiter.api.Test;

public class ProducerTest {
	/**
	 *1、给一个错误的ip模拟broker不可用
	 *2、设置 max.block.ms 3000
	 *3、进行send，由于metadata没有获取到将阻塞3000
	 *4、然后，async：放开阻塞无法感知是否成功 ，async callback:放开阻塞在callback里收到timeout异常，sync：timeout异常 
	 */
	@Test
	public void testStartupBrokerError_SendTimeout() throws Exception {
		Properties props = buildProducerProperties();
		props.put("bootstrap.servers", "1.1.1.1:9092"); 
		props.put("acks", "0");
		props.put("retries", 0);
		props.put("delivery.timeout.ms", 1500);//虽然这个值设置的比max.block.ms小，但由于阻塞，消息没有进入发送缓存区，不会触发这个超时时间
		props.put("linger.ms", 5);
		props.put("request.timeout.ms", 1000);
		props.put("max.block.ms", 3000);
		
		Producer<String, String> producer = new KafkaProducer<>(props);
		System.out.println("producer created...");
		for (int i = 0; i < 10; i++){
			long start = System.currentTimeMillis();
		    try {
		    	//async
		    	producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i)));
		    	//async callback
//				producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i)),(r,e)->{
//					System.err.println("callback:"+e);
//				});
				//sync
//				producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i))).get();
				System.out.println(String.format("send message %s success",i));
				Thread.sleep(3000);
			} catch (Exception e) {
				System.out.println("send error:"+e);
			}
		    long end = System.currentTimeMillis();
		    System.out.println("send use millis:"+(end - start));
		}
		producer.close();
	}
	
	/**
	 *1、使用正确的可用的broker地址
	 *2、设置 delivery.timeout.ms 5000
	 *3、进行send，由于能正确获取metadata所以不受max.block.ms影响，发送正常无阻塞
	 *4、停止broker继续发送，由于metadata已存在，依然不受max.block.ms影响，但受delivery.timeout.ms影响
	 *4、然后，async：不阻塞无法感知是否成功 ，async callback:不阻塞，5000后callback里收到timeout异常，sync：Future.get()时阻塞5000 timeout异常 
	 */
	@Test
	public void testStartupBrokerOk2Error_SendTimeout() throws Exception {
		Properties props = buildProducerProperties();
		props.put("bootstrap.servers", "192.168.229.132:9092"); 
		props.put("acks", "0");
		props.put("retries", 0);
		props.put("delivery.timeout.ms", 5000);
		props.put("linger.ms", 5);
		props.put("request.timeout.ms", 1000);
		props.put("max.block.ms", 3000);
		
		Producer<String, String> producer = new KafkaProducer<>(props);
		System.out.println("producer created...");
		for (int i = 0; i < 10000; i++){
			long start = System.currentTimeMillis();
		    try {
		    	//async
//		    	producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i)));
		    	//async callback
//				producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i)),(r,e)->{
//					System.err.println("callback:"+e);
//				});
				//sync
				producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i))).get();
				System.out.println(String.format("send message %s success",i));
				Thread.sleep(3000);
			} catch (Exception e) {
				System.out.println("send error:"+e);
			}
		    long end = System.currentTimeMillis();
		    System.out.println("send use millis:"+(end - start));
		}
		producer.close();
	}
	/**
	 * 测试发送时配置的各种限制
	 */
	@Test
	public void testSendLimit() throws Exception {
		Properties props = buildProducerProperties();
		props.put("bootstrap.servers", "192.168.229.132:9092"); 
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("delivery.timeout.ms", 5000);
		props.put("linger.ms", 50);
		props.put("request.timeout.ms", 1000);
		props.put("max.block.ms", 3000);
		/**
		 * 单个消息大小不能超过
		 */
		props.put("buffer.memory", 33554432);
		props.put("batch.size", 16384);
		/**
		 * 单个消息大小不能超过
		 */
		props.put("max.request.size", 100);
		
		Producer<String, String> producer = new KafkaProducer<>(props);
		System.out.println("producer created...");
		for (int i = 0; i < 10000; i++){
			long start = System.currentTimeMillis();
		    try {
		    	/**
		    	 * 使用了错误的分区 TimeoutException: Topic test not present in metadata after 3000 ms.
		    	 */
//				producer.send(new ProducerRecord<String, String>("test", 999, Integer.toString(i), Integer.toString(i))).get();
		    	/**
		    	 * 单个消息value序列化后的bytes 大于 max.request.size . RecordTooLargeException: The message is 115 bytes when serialized which is larger than the maximum request size you have configured with the max.request.size configuration.
		    	 */
				producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), 
						System.currentTimeMillis()+""+System.currentTimeMillis())).get();
				System.out.println(String.format("send message %s success",i));
				Thread.sleep(3000);
			} catch (Exception e) {
				System.out.println("send error:"+e);
			}
		    long end = System.currentTimeMillis();
		    System.out.println("send use millis:"+(end - start));
		}
		producer.close();
	}
	
	@Test
	public void testSendOk() throws Exception {
		Properties props = buildProducerProperties();
		props.put("bootstrap.servers", "192.168.229.134:9092"); 
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("delivery.timeout.ms", 5000);
		props.put("linger.ms", 5);
		props.put("request.timeout.ms", 1000);
		props.put("max.block.ms", 3000);
		
		Producer<String, String> producer = new KafkaProducer<>(props);
		System.out.println("producer created...");
		for (int i = 0; i < 10; i++){
		    try {
				RecordMetadata recordMetadata = producer
						.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i))).get();
				System.out.println(String.format("send message %s success,partition:"
						+recordMetadata.partition()+",offset:"+recordMetadata.offset(),i));
			} catch (Exception e) {
				System.out.println("send error:"+e);
			}
		}
		producer.close();
	}

	public static Properties buildProducerProperties() {
		Properties props = new Properties();
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");//Type: classDefault: Valid Values: Importance: high
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");//Type: classDefault: Valid Values: Importance: high
		props.put("acks", "all");//Type: stringDefault: 1Valid Values: [all, -1, 0, 1]Importance: high
		props.put("bootstrap.servers", "192.168.229.132:9092");//Type: listDefault: ""Valid Values: non-null stringImportance: high
		/**
用于send停留的缓存大小，如果消息send到缓存的速度大于消息被delivery到broker的速度，则缓存就会堆积起来，满了以后再send就会阻塞甚至阻塞超时，就需要考虑消息压缩、加大缓存、加分区等方法了
		*/
		props.put("buffer.memory", 33554432);//Type: longDefault: 33554432 Valid Values: [0,...]Importance: high
		/**压缩类型 none, gzip, snappy, lz4, or zstd . 在吞吐方面：LZ4>Snappy> zstd、GZIP；在压缩比方面：zstd>lz4>gzip>snappy*/
		props.put("compression.type", "none");//Type: stringDefault: noneValid Values: Importance: high
		props.put("retries", 0);//Type: intDefault: 2147483647Valid Values: [0,...,2147483647]Importance: high
		/**
限制批量发送数量,但是也不一定等到满了，这和另外一个参数linger.ms有关
		 */
		props.put("batch.size", 16384);//Type: intDefault: 16384Valid Values: [0,...]Importance: medium
//		props.put("client.dns.lookup", "default");//Type: stringDefault: defaultValid Values: [default, use_all_dns_ips, resolve_canonical_bootstrap_servers_only]Importance: medium
//		props.put("client.id", "");//Type: stringDefault: ""Valid Values: Importance: medium
		/** Close idle connections after the number of milliseconds specified by this config.*/
		props.put("connections.max.idle.ms", 540000);//Type: longDefault: 540000Valid Values: Importance: medium
		/**调用send() 返回后报告成功或失败的时间上限。它相当于限制了重试次数，此配置的值应大于或等于request.timeout.ms和linger.ms的总和*/
		props.put("delivery.timeout.ms", 120000);//Type: intDefault: 120000Valid Values: [0,...]Importance: medium
		/**
		 * 设置(吞吐量和延时性能)
producer是按照batch进行发送的，但是还要看linger.ms的值，默认是0，表示不做停留。这种情况下，可能有的batch中没有包含足够多的produce请求就被发送出去了，造成了大量的小batch，给网络IO带来的极大的压力。
商业环境推荐：为了减少了网络IO，提升了整体的TPS。假设设置linger.ms=5，表示producer请求可能会延时5ms才会被发送
		 */
		props.put("linger.ms", 5);//Type: longDefault: 0Valid Values: [0,...]Importance: medium
		/**该配置控制 KafkaProducer.send() 和 KafkaProducer.partitionsFor() 将阻塞多长时间，它发生在buffer.memory满了或metadata不可用（比如producer刚启动但是连不上broker），send超时后则会放开阻塞，callback或Future.get()将会收到TimeoutException*/
		props.put("max.block.ms", 60000);//Type: longDefault: 60000 Valid Values: [0,...]Importance: medium
		/**限制批量发送的bytes发小，这跟batch.size哪个先达就发送*/
		props.put("max.request.size", 1048576);//Type: intDefault: 1048576 Valid Values: [0,...]Importance: medium
		/**send时如果不指定partition参数，则使用这里设置的class来计算partition*/
		props.put("partitioner.class", "io.github.icodegarden.commons.kafka.ProducerTest$KeyPartitionerTest");//Type: classDefault: org.apache.kafka.clients.producer.internals.DefaultPartitioner Valid Values: Importance: medium
//		props.put("receive.buffer.bytes", 32768);//Type: intDefault: 32768 Valid Values: [-1,...]Importance: medium
		/**发送请求超时时间，超时则进行重试直到用尽后再报失败；这个值应该配置大于replica.lag.time.max.ms（acks为all发送时，消息复制到同步副本的超时时间）*/
		props.put("request.timeout.ms", 30000);//Type: intDefault: 30000 Valid Values: [0,...]Importance: medium
		/**重连broker失败后最大等待多少时间再连，20%浮动*/
		props.put("reconnect.backoff.max.ms", 1000);//Type: longDefault: 1000Valid Values: [0,...]Importance: low
		/**重连broker失败后等待多少时间再连*/
		props.put("reconnect.backoff.ms", 50);//Type: longDefault: 50Valid Values: [0,...]Importance: low
		/**重试失败后等待多少时间*/
		props.put("retry.backoff.ms", 100);//Type: longDefault: 100Valid Values: [0,...]Importance: low
//		props.put("send.buffer.bytes", );//Type: intDefault: 131072Valid Values: [-1,...]Importance: medium 。The size of the TCP send buffer (SO_SNDBUF) to use when sending data. If the value is -1, the OS default will be used.
		/**限制客户端在单个连接上能够发送的未响应请求的个数。设置此值是1表示kafka broker在响应请求之前client不能再向同一个broker发送请求。注意：设置此参数是为了避免消息乱序*/
		props.put("max.in.flight.requests.per.connection", 5);//Type: intDefault: 5Valid Values: [1,...]Importance: low
		/**强制更新metadata的时间间隔。即使我们没有看到任何partition leadership改变*/
		props.put("metadata.max.age.ms", 300000);//Type: longDefault: 300000 Valid Values: [0,...]Importance: low

		/**事务生产。
		 * 要使用事务生产者，必须设置transactional.id配置属性。如果设置了transactional.id，则会自动启用等幂，同时生产者会配置等幂依赖于哪个。此外，事务中包含的主题应配置为具有持久性。特别是，replication.factor至少应为3，这些主题的min.insync.replicas应设置为2。最后，为了从端到端实现事务性保证，还必须将使用者配置为只读提交的消息。
		 * transactional.id的目的是在单个生产者实例的多个会话之间启用事务恢复。它通常从分区的、有状态的应用程序中的shard标识符派生。因此，对于在分区应用程序中运行的每个生产者实例，它应该是唯一的。所有新的事务性api都被阻塞，并在失败时抛出异常
		 */
//		props.put("transactional.id", null);//Type: stringDefault: nullValid Values: non-empty stringImportance: low
//		props.put("transaction.timeout.ms", 60000);//Type: intDefault: 60000Valid Values: Importance: low

		/**
		 * 幂等生产。
		 * 若要启用等幂，必须将enable.idempotence配置设置为true。如果设置，retries配置将默认为Integer.MAX_VALUE，acks配置将默认为all。对于等幂生产者没有API更改，因此无需修改现有应用程序即可利用此功能。
		 * 为了利用等幂生产者，必须避免应用程序级的重新发送，因为它们不能消除重复。因此，如果应用程序启用了等幂，建议不设置retries config，因为它将默认为Integer.MAX_VALUE。此外，如果发送（ProducerRecord）返回一个错误，即使是无限次重试（例如，如果消息在发送前在缓冲区中过期），则建议关闭生产者并检查最后生成的消息的内容，以确保它不重复。最后，生产者只能保证在单个会话中发送的消息具有幂等性。
		 */
//		props.put("enable.idempotence", );//Type: booleanDefault: falseValid Values: Importance: low
//		props.put("interceptor.classes", );//Type: listDefault: ""Valid Values: non-null stringImportance: low
		 
//		props.put("ssl.key.password", );//Type: passwordDefault: nullValid Values: Importance: high
//		props.put("ssl.keystore.location", );//Type: passwordDefault: nullValid Values: Importance: high
//		props.put("ssl.keystore.password", );//Type: passwordDefault: nullValid Values: Importance: high
//		props.put("ssl.truststore.location", );//Type: passwordDefault: nullValid Values: Importance: high
//		props.put("ssl.truststore.password", );//Type: passwordDefault: nullValid Values: Importance: high
//		props.put("ssl.enabled.protocols", );//Type: listDefault: TLSv1.2,TLSv1.1,TLSv1Valid Values: Importance: medium .The list of protocols enabled for SSL connections.
//		props.put("ssl.keystore.type", );//Type: stringDefault: JKSValid Values: Importance: medium
//		props.put("ssl.protocol", );//Type: stringDefault: TLSValid Values: Importance: medium
//		props.put("ssl.provider", );//Type: stringDefault: nullValid Values: Importance: medium
//		props.put("ssl.truststore.type", );//Type: stringDefault: JKSValid Values: Importance: medium
//		props.put("ssl.cipher.suites", );//Type: listDefault: nullValid Values: Importance: low
//		props.put("ssl.endpoint.identification.algorithm", );//Type: stringDefault: httpsValid Values: Importance: low
//		props.put("ssl.keymanager.algorithm", );//Type: stringDefault: SunX509Valid Values: Importance: low
//		props.put("ssl.secure.random.implementation", );//Type: stringDefault: nullValid Values: Importance: low
//		props.put("ssl.trustmanager.algorithm", );//Type: stringDefault: PKIXValid Values: Importance: low
		 
//		props.put("sasl.client.callback.handler.class", );//Type: classDefault: nullValid Values: Importance: medium
//		props.put("sasl.jaas.config", );//Type: passwordDefault: nullValid Values: Importance: medium
//		props.put("sasl.kerberos.service.name", );//Type: stringDefault: nullValid Values: Importance: medium
//		props.put("sasl.login.callback.handler.class", );//Type: classDefault: nullValid Values: Importance: medium
//		props.put("sasl.login.class", );//Type: classDefault: nullValid Values: Importance: medium
//		props.put("sasl.mechanism", );//Type: stringDefault: GSSAPIValid Values: Importance: medium
//		props.put("sasl.kerberos.kinit.cmd", );//Type: stringDefault: /usr/bin/kinitValid Values: Importance: low
//		props.put("sasl.kerberos.min.time.before.relogin", );//Type: longDefault: 60000Valid Values: Importance: low
//		props.put("sasl.kerberos.ticket.renew.jitter", );//Type: doubleDefault: 0.05Valid Values: Importance: low
//		props.put("sasl.kerberos.ticket.renew.window.factor", );//Type: doubleDefault: 0.8Valid Values: Importance: low
//		props.put("sasl.login.refresh.buffer.seconds", );//Type: shortDefault: 300Valid Values: [0,...,3600]Importance: low
//		props.put("sasl.login.refresh.min.period.seconds", );//Type: shortDefault: 60Valid Values: [0,...,900]Importance: low
//		props.put("sasl.login.refresh.window.factor", );//Type: doubleDefault: 0.8Valid Values: [0.5,...,1.0]Importance: low
//		props.put("sasl.login.refresh.window.jitter", );//Type: doubleDefault: 0.05Valid Values: [0.0,...,0.25]Importance: low
		 
		/**PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL*/
//		props.put("security.protocol", );//Type: stringDefault: PLAINTEXTValid Values: Importance: medium
//		props.put("security.providers", );//Type: stringDefault: nullValid Values: Importance: low
		 
//		props.put("metric.reporters", "");//Type: listDefault: ""Valid Values: non-null stringImportance: low
//		props.put("metrics.num.samples", 2);//Type: intDefault: 2Valid Values: [1,...]Importance: low
//		props.put("metrics.recording.level", "INFO");//Type: stringDefault: INFOValid Values: [INFO, DEBUG]Importance: low
//		props.put("metrics.sample.window.ms", 30000);//Type: longDefault: 30000Valid Values: [0,...]Importance: low
		return props;
	}
	
	public static class KeyPartitionerTest implements Partitioner {
		@Override
		public void configure(Map<String, ?> configs) {
		}

		@Override
		public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
			Integer numPartitions = cluster.partitionCountForTopic(topic);
			try {
				return Math.abs(Integer.parseInt((String) key) % numPartitions);
			} catch (Exception e) {
				return Math.abs(key.hashCode() % numPartitions);
			}
		}

		@Override
		public void close() {
		}
	}
	
}

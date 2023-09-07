package io.github.icodegarden.commons.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;

public class ConsumerTest {
	
	@Test
	public void testConsumeOk() throws Exception {
		 Properties props = buildConsumerProperties();
		 
		 props.put("bootstrap.servers", "192.168.184.129:9092");//Type: listDefault: ""Valid Values: non-null stringImportance: high
		 /**向broker拉取消息的最小bytes，broker上的消息小于该设置则阻塞，直到超时；设置1表示尽快返回*/
	     props.put("fetch.min.bytes", 1);//Type: intDefault: 1Valid Values: [0,...]Importance: high
	     props.put("fetch.max.bytes", 52428800);//Type: intDefault: 52428800 Valid Values: [0,...]Importance: medium
	     /**fetch时不满足bytes等限制要求时的最大等待时间*/
	     props.put("fetch.max.wait.ms", 500);//Type: intDefault: 500Valid Values: [0,...]Importance: low
	     props.put("group.id", "xff");//Type: stringDefault: nullValid Values: Importance: high
	     /**与zk的心跳间隔。心跳用于确保消费者的会话保持活动状态，session建立和断开会触发consumer再均衡。该值的设置必须低于session.timeout.ms，但通常不应高于该值的1/3。它可以调整得更低，以控制正常再平衡的预期时间。*/
	     props.put("heartbeat.interval.ms", 3000);//Type: intDefault: 3000Valid Values: Importance: high
	     props.put("session.timeout.ms", 10000);//Type: intDefault: 10000 Valid Values: Importance: high
	     /**限制poll()时从每个partition拉取的最大bytes。如果从一个partition拉取时第一个消息bytes就大于该值，那么也能返回且只返回这一个消息。注意broker上的单个消息最大大小由message.max.bytes (broker config) or max.message.bytes (topic config). 限制*/
	     props.put("max.partition.fetch.bytes", 1048576);//Type: intDefault: 1048576 Valid Values: [0,...]Importance: high
	     props.put("enable.auto.commit", true);//Type: booleanDefault: trueValid Values: Importance: medium
	     props.put("auto.commit.interval.ms", 5000);//Type: intDefault: 5000 Valid Values: [0,...]Importance: low
	     /**poll()之间的最大间隔时间。如果在此超时过期之前未调用poll（），则认为consumer不可用，触发再均衡*/
	     props.put("max.poll.interval.ms", 300000);//Type: intDefault: 300000 Valid Values: [1,...]Importance: medium
	     props.put("max.poll.records", 500);//Type: intDefault: 500Valid Values: [1,...]Importance: medium
	     /**consumer分区均衡策略*/
	     props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RoundRobinAssignor");//Type: listDefault: class org.apache.kafka.clients.consumer.RangeAssignor Valid Values: non-null stringImportance: medium
	     
	     KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
	     consumer.subscribe(Arrays.asList("test"));
	     try {
			while (true) {
			     ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			     for (ConsumerRecord<String, String> record : records){
			         System.out.printf("topic = %s, offset = %d,partition=%s, key = %s, value = %s%n", record.topic(),
			        		 record.offset(),record.partition(), record.key(), record.value());
			     }
			 }
		} finally {
			consumer.close();
		}
	}

	public static Properties buildConsumerProperties() {
		Properties props = new Properties();
	     props.put("bootstrap.servers", "192.168.229.132:9092");//Type: listDefault: ""Valid Values: non-null stringImportance: high
	     props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");//Type: classDefault: Valid Values: Importance: high
	     props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");//Type: classDefault: Valid Values: Importance: high
	     /**向broker拉取消息的最小bytes，broker上的消息小于该设置则阻塞，直到超时；设置1表示尽快返回*/
	     props.put("fetch.min.bytes", 1);//Type: intDefault: 1Valid Values: [0,...]Importance: high
	     props.put("fetch.max.bytes", 52428800);//Type: intDefault: 52428800 Valid Values: [0,...]Importance: medium
	     /**fetch时不满足bytes等限制要求时的最大等待时间*/
	     props.put("fetch.max.wait.ms", 500);//Type: intDefault: 500Valid Values: [0,...]Importance: low
	     /**poll()之间的最大间隔时间。如果在此超时过期之前未调用poll（），则认为consumer不可用，触发再均衡*/
	     props.put("max.poll.interval.ms", 300000);//Type: intDefault: 300000 Valid Values: [1,...]Importance: medium
	     props.put("max.poll.records", 500);//Type: intDefault: 500Valid Values: [1,...]Importance: medium
	     props.put("group.id", "xff");//Type: stringDefault: nullValid Values: Importance: high
	     /**与zk的心跳间隔。心跳用于确保消费者的会话保持活动状态，session建立和断开会触发consumer再均衡。该值的设置必须低于session.timeout.ms，但通常不应高于该值的1/3。它可以调整得更低，以控制正常再平衡的预期时间。*/
	     props.put("heartbeat.interval.ms", 3000);//Type: intDefault: 3000Valid Values: Importance: high
	     props.put("session.timeout.ms", 10000);//Type: intDefault: 10000 Valid Values: Importance: high
	     /**限制poll()时从每个partition拉取的最大bytes。如果从一个partition拉取时第一个消息bytes就大于该值，那么也能返回且只返回这一个消息。注意broker上的单个消息最大大小由message.max.bytes (broker config) or max.message.bytes (topic config). 限制*/
	     props.put("max.partition.fetch.bytes", 1048576);//Type: intDefault: 1048576 Valid Values: [0,...]Importance: high
	     /**是否允许在subscribing or assigning时自动创建topic。true的前提是broker配置了auto.create.topics.enable=true*/
//	     props.put("allow.auto.create.topics", true);//Type: boolean Default: trueValid Values: Importance: medium
	     /**在没有偏移量可提交（比如第一次启动）或请求的偏移量在broker上不存在时如何处理，例如latest从末尾开始读...*/
	     props.put("auto.offset.reset", "latest");//Type: string Default: latest Valid Values: [latest, earliest, none]Importance: medium
//	     props.put("client.dns.lookup", "default");//Type: stringDefault: default Valid Values: [default, use_all_dns_ips, resolve_canonical_bootstrap_servers_only]Importance: medium
	     props.put("connections.max.idle.ms", 540000);//Type: longDefault: 540000 Valid Values: Importance: medium
	     /**对所有consumer的api不传timeout参数时起作用*/
	     props.put("default.api.timeout.ms", 60000);//Type: intDefault: 60000 Valid Values: [0,...]Importance: medium
	     props.put("enable.auto.commit", true);//Type: booleanDefault: trueValid Values: Importance: medium
	     props.put("auto.commit.interval.ms", 5000);//Type: intDefault: 5000 Valid Values: [0,...]Importance: low
	     
	     /**订阅时当表达式匹配到kafka内置的topic时，是否排除掉他们*/
//	     props.put("exclude.internal.topics", true);//Type: booleanDefault: trueValid Values: Importance: medium
//	     props.put("group.instance.id", );//Type: stringDefault: nullValid Values: Importance: medium
//	     props.put("isolation.level", "read_uncommitted");//Type: stringDefault: read_uncommitted Valid Values: [read_committed, read_uncommitted]Importance: medium
	     
	     /**consumer分区均衡策略*/
	     props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RoundRobinAssignor");//Type: listDefault: class org.apache.kafka.clients.consumer.RangeAssignor Valid Values: non-null stringImportance: medium
	     props.put("receive.buffer.bytes", 65536);//Type: intDefault: 65536Valid Values: [-1,...]Importance: medium
	     props.put("send.buffer.bytes", 131072);//Type: intDefault: 131072 Valid Values: [-1,...]Importance: medium
//	     props.put("request.timeout.ms", 30000);//Type: intDefault: 30000 Valid Values: [0,...]Importance: medium
//	     props.put("security.protocol", "PLAINTEXT");//Type: stringDefault: PLAINTEXTValid Values: Importance: medium .   PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL.
//	     props.put("check.crcs", true);//Type: booleanDefault: trueValid Values: Importance: low
//	     props.put("client.id", "");//Type: stringDefault: ""Valid Values: Importance: low
//	     props.put("client.rack", "");//Type: stringDefault: ""Valid Values: Importance: low
//	     props.put("interceptor.classes", "");//Type: listDefault: ""Valid Values: non-null stringImportance: low
//	     props.put("metadata.max.age.ms", 300000);//Type: longDefault: 300000 Valid Values: [0,...]Importance: low
//	     props.put("reconnect.backoff.max.ms", 1000);//Type: longDefault: 1000Valid Values: [0,...]Importance: low
//	     props.put("reconnect.backoff.ms", 50);//Type: longDefault: 50Valid Values: [0,...]Importance: low
//	     props.put("retry.backoff.ms", 100);//Type: longDefault: 100Valid Values: [0,...]Importance: low
//	     props.put("security.providers", );//Type: stringDefault: nullValid Values: Importance: low

	     //ssl configs
	     //sasl configs
	     //metrics configs
		return props;
	}
}

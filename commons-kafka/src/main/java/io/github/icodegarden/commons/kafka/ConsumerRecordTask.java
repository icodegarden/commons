package io.github.icodegarden.commons.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 * @param <V>
 */
public interface ConsumerRecordTask<K, V> extends Runnable {

	ConsumerRecord<K, V> getRecord();

}

package io.github.icodegarden.commons.kafka.reliability;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import io.github.icodegarden.commons.lang.TimeoutableCloseable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ReliabilityProcessor<K, V> extends TimeoutableCloseable {

	/**
	 * @param records
	 */
	void handleReliability(ConsumerRecords<K, V> records);

	/**
	 * @return now processing not history total
	 */
	long processingCount();

}

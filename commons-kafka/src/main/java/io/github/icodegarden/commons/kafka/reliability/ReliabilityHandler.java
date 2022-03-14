package io.github.icodegarden.commons.kafka.reliability;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import io.github.icodegarden.commons.kafka.UnRetryableException;

/**
 * retry after handle failed store after retry failed trigger
 * {@link #onStoreFailed()} after store failed
 * 
 * @author Fangfang.Xu
 *
 */
public interface ReliabilityHandler<K, V> {

	boolean handle(ConsumerRecord<K, V> record) throws UnRetryableException;

	default int handleRetries() {
		return 2;
	}

	default long handleRetryBackoffMillis() {
		return 1000;
	}

	boolean primaryStore(ConsumerRecord<K, V> failedRecord, Throwable handleCause) throws UnRetryableException;

	boolean secondaryStore(ConsumerRecord<K, V> failedRecord, Throwable primaryStoreCause) throws UnRetryableException;

	default int storeRetries() {
		return 2;
	}

	default long storeRetryBackoffMillis() {
		return 1000;
	}

	void onStoreFailed(ConsumerRecord<K, V> failedRecord, Throwable secondaryStoreCause);
}

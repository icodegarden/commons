package io.github.icodegarden.commons.kafka;

import java.util.Queue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这个类应该迁移到lang?
 * @author Fangfang.Xu
 *
 */
public class DelayedRetryableReference<V> implements Delayed {

	private static final Logger log = LoggerFactory.getLogger(DelayedRetryableReference.class);

	private static final int MAX_RETRY = 30;
	private static final long DELAY_BACKOFF_MILLIS = 30000;
	private static final long DELAY_MAX_BACKOFF_MILLIS = DELAY_BACKOFF_MILLIS * 10;

	private V v;
	private Queue<DelayedRetryableReference<V>> queue;
	/**
	 * 已使用过方法 enQueue 的标识
	 */
	private boolean queued;

	private int retried;// 重试进队列次数
	private long delayMillis;// 延迟时间
	private long delayAt;// 需要延迟到什么时候

	public DelayedRetryableReference(V v, Queue<DelayedRetryableReference<V>> queue) {
		this(v, queue, Math.min(30000, DELAY_BACKOFF_MILLIS));
	}

	public DelayedRetryableReference(V v, Queue<DelayedRetryableReference<V>> queue, long delayMillis) {
		this.v = v;
		this.queue = queue;
		this.delayMillis = delayMillis;
	}

	@Override
	public int compareTo(Delayed o) {
		DelayedRetryableReference<V> other = (DelayedRetryableReference) o;
		if (this.delayMillis > other.delayMillis) {
			return 1;
		}
		if (this.delayMillis < other.delayMillis) {
			return -1;
		}
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return delayAt - System.currentTimeMillis();
	}

	public boolean enQueue() {
		if (retried >= MAX_RETRY) {// 超时最大重试次数不再进队列
			log.warn("{} can not enqueue, has retried > {}", v, MAX_RETRY);
			return false;
		}
		// delay is assigned at first time
		if (queued) {
			if (delayMillis < DELAY_MAX_BACKOFF_MILLIS) {
				delayMillis += DELAY_BACKOFF_MILLIS;
			}
			retried++;
		}
		// updateDelayAt
		delayAt = System.currentTimeMillis() + delayMillis;
		queued = true;
		return queue.offer(this);
	}

	public final V get() {
		return v;
	}

	public int getRetried() {
		return retried;
	}

	public long getDelayMillis() {
		return delayMillis;
	}

}
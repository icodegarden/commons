package io.github.icodegarden.commons.lang.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.github.icodegarden.commons.lang.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class BatchDeliverTask<T> extends AlwaysRunCloseableThread {

	/**
	 * 如同kafka Producer的batch.size
	 */
	private int batchSize = 100;
	/**
	 * 如同kafka Producer的linger.ms
	 */
	private int lingerMs = 10;

	private Consumer<Collection<T>> consumer;

	private final ThreadPoolExecutor threadPool;

	private BlockingQueue<T> queue = new LinkedBlockingQueue<>();

	public BatchDeliverTask(Consumer<Collection<T>> consumer, int parallel) {
		this(BatchDeliverTask.class.getSimpleName(), consumer, parallel);
	}

	public BatchDeliverTask(String name, Consumer<Collection<T>> consumer, int parallel) {
		super(name);
		this.consumer = consumer;
		this.threadPool = ThreadUtils.newFixedThreadPool(parallel, 0, getName(),
				new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setLingerMs(int lingerMs) {
		this.lingerMs = lingerMs;
	}

	public void produce(T msg) {
		queue.offer(msg);
	}

	public void produce(Collection<T> msgs) {
		queue.addAll(msgs);// 底层调用offer
	}

	protected void doRun(long loop) {
		try {
			List<T> batch = new ArrayList<>(batchSize);

			/**
			 * 先进行一次阻塞，避免无元素时不停循环
			 */
			if (queue.isEmpty()) {
				T msg = queue.take();
				batch.add(msg);
			}

			queue.drainTo(batch, batchSize - batch.size());

			if (batch.size() < batchSize) {
				/*
				 * 若还不满则尝试最多阻塞Nms
				 */
				T msg = queue.poll(lingerMs, TimeUnit.MILLISECONDS);
				if (msg != null) {
					batch.add(msg);

					if (batch.size() < batchSize) {
						/*
						 * 若还有空间
						 */
						queue.drainTo(batch, batchSize - batch.size());
					}
				}
			}

			if (!batch.isEmpty()) {
				threadPool.execute(() -> {
					try {
						consumer.accept(batch);
					} catch (Exception e) {
						log.error("Batch Consume error", e);
					}
				});

//				batch.clear();
			}
		} catch (InterruptedException ignore) {
			log.warn("{} Interrupted", getName());
		}
	};

	protected void doClose() {
		List<T> batch = new ArrayList<>(batchSize);

		while (!queue.isEmpty()) {
			queue.drainTo(batch, batchSize - batch.size());

			try {
				consumer.accept(batch);
			} catch (Exception e) {
				log.error("Batch Consume error", e);
			}
			batch.clear();
		}
	};
}
package io.github.icodegarden.commons.exchange;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.exchange.exception.AllInstanceFailedExchangeException;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.ExchangeException.ExchangeFailedInstance;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoSwitchableExchangeException;
import io.github.icodegarden.commons.exchange.exception.PartInstanceFailedExchangeException;
import io.github.icodegarden.commons.exchange.exception.RequesterRejectedExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.commons.lang.util.CollectionUtils;

/**
 * 并行的对选择的所有实例进行交互<br>
 * 
 * @author Fangfang.Xu
 */
public class ParallelExchanger implements Exchanger<ParallelExchangeResult> {

	private static final Logger log = LoggerFactory.getLogger(ParallelExchanger.class);

	private Protocol protocol;
	private final Collection<MetricsInstance> defaultCandidates;
	private final Config defaultConfig;
	private final ExecutorService executorService;

	/**
	 * 使用无界线程池
	 * 
	 * @param defaultCandidates   默认实例
	 * @param maxCandidateOfShard
	 * @param maxShards
	 */
	public ParallelExchanger(Protocol protocol, Collection<MetricsInstance> defaultCandidates) {
		this(protocol, defaultCandidates, new Config());
	}

	/**
	 * 
	 * @param defaultCandidates 默认实例
	 * @param defaultConfig
	 */
	public ParallelExchanger(Protocol protocol, Collection<MetricsInstance> defaultCandidates, Config defaultConfig) {
		this.protocol = protocol;
		this.defaultCandidates = defaultCandidates;
		this.defaultConfig = defaultConfig;

		this.executorService = new ThreadPoolExecutor(defaultConfig.maxShards, defaultConfig.maxParallelThreads, 0,
				NANOSECONDS, new SynchronousQueue<Runnable>(),
				new NamedThreadFactory(ParallelExchanger.class.getSimpleName()), new ThreadPoolExecutor.AbortPolicy());
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * 使用默认实例，最多maxShards个分片实例</br>
	 * <h1>对合格的候选实例并行进行交互，交互失败的实例则换实例</h1>
	 * 如果只有部分实例成功，则抛出{@link PartInstanceFailedExchangeException}
	 * <h1>使用{@link CandidatesSwitchableExchanger}交互</h1>
	 */
	@Override
	public ParallelExchangeResult exchange(Object body, int timeout) throws ExchangeException {
		return exchange(body, timeout, defaultCandidates);
	}

	public ParallelExchangeResult exchange(Object body, int timeout, Collection<MetricsInstance> candidates)
			throws ExchangeException {
		return exchange(body, timeout, candidates, defaultConfig);
	}

	/**
	 * 使用给定的实例、Config</br>
	 * <h1>对合格的候选实例并行进行交互，交互失败的实例则换实例</h1>
	 * 如果只有部分实例成功，则抛出{@link PartInstanceFailedExchangeException}
	 * <h1>使用{@link CandidatesSwitchableExchanger}交互</h1>
	 */
	public ParallelExchangeResult exchange(Object body, int timeout, Collection<MetricsInstance> candidates,
			Config config) throws ExchangeException {
		/**
		 * 过滤掉超载的，得出真实的可并行实例 最多maxShards个分片实例
		 */
		List<MetricsInstance> parallelInstances = candidates.stream().filter(instance -> !instance.isOverload())
				.limit(config.maxShards).collect(Collectors.toList());
		if (parallelInstances.isEmpty()) {
			throw new NoQualifiedInstanceExchangeException(candidates);
		}
		/**
		 * 当只有1个实例时，不需要使用并行方式
		 */
		if (parallelInstances.size() == 1) {
			CandidatesSwitchableExchanger exchanger = new CandidatesSwitchableExchanger(protocol, parallelInstances,
					true);
			if (log.isDebugEnabled()) {
				log.debug("parallel exchange shard is 0, shardTotal is 1, cause candidates size is 1");
			}
			ParallelShardObject parallelShardObject = new ParallelShardObject(body, 0, 1);
			ShardExchangeResult result = exchanger.exchange(parallelShardObject, timeout);
			return new ParallelExchangeResult(Arrays.asList(result));
		}

		final int shardTotal = parallelInstances.size();
//		/**
//		 * 并行任务的超时时间=timeout/分片数，但最小不低于
//		 */
//		final int shardTimeout = Math.max(timeout / shardTotal, 1000);
		/**
		 * 不做修改
		 */
		final int shardTimeout = timeout;

		List<ShardExchangeResult> results = new LinkedList<ShardExchangeResult>();
		List<ExchangeException> exchangeExceptions = new LinkedList<ExchangeException>();
		CountDownLatch countDownLatch = new CountDownLatch(shardTotal);
		for (int shard = 0; shard < shardTotal; shard++) {
			/**
			 * 每个分片实例数
			 */
			List<MetricsInstance> candidatesOfShard = CollectionUtils.nextElements(parallelInstances, shard,
					config.maxCandidatesOfShard);
			final int shardFinal = shard;
			try {
				executorService.execute(() -> {
					try {
						CandidatesSwitchableExchanger exchanger = new CandidatesSwitchableExchanger(protocol,
								candidatesOfShard, false);
						if (log.isDebugEnabled()) {
							log.debug("parallel exchange shard:{}, shardTotal:{}", shardFinal, shardTotal);
						}
						ParallelShardObject parallelShardObject = new ParallelShardObject(body, shardFinal, shardTotal);
						ShardExchangeResult result = (ShardExchangeResult) exchanger.exchange(parallelShardObject,
								shardTimeout);
						results.add(result);
					} catch (ExchangeException e) {
						exchangeExceptions.add(e);
					} catch (Exception e) {// 不会出，担保
						log.error("ex on parallel exchangeInstances", e);
						exchangeExceptions
								.add(new NoSwitchableExchangeException(candidatesOfShard, Collections.emptyList()));
					} finally {
						countDownLatch.countDown();
					}
				});
			} catch (RejectedExecutionException e) {
				countDownLatch.countDown();
				exchangeExceptions
						.add(new RequesterRejectedExchangeException("Thread Pool Rejected", candidatesOfShard));
			}
		}
		try {
			/**
			 * 担保最多等待timeout+1800s
			 */
			countDownLatch.await(timeout + 1800000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignore) {
		}
		/**
		 * 没有成功的分片
		 */
		if (exchangeExceptions.size() == shardTotal) {
			List<ExchangeFailedInstance> exchangeFailedInstances = exchangeExceptions.stream()
					.map(ExchangeException::getExchangedInstances).flatMap(Collection<ExchangeFailedInstance>::stream)
					.collect(Collectors.toList());
			throw new AllInstanceFailedExchangeException(candidates, exchangeFailedInstances);
		}
		/**
		 * 部分成功
		 */
		if (results.size() != shardTotal) {
			List<ExchangeFailedInstance> exchangeFailedInstances = exchangeExceptions.stream()
					.map(ExchangeException::getExchangedInstances).flatMap(Collection<ExchangeFailedInstance>::stream)
					.collect(Collectors.toList());

			List<MetricsInstance> successInstances = results.stream()
					.map(exchangeResult -> exchangeResult.successResult().instance()).collect(Collectors.toList());
			throw new PartInstanceFailedExchangeException(candidates, exchangeFailedInstances, successInstances);
		}
		/**
		 * 全部成功<br>
		 * 最终的返回数据size与分片数相同，若某个实例最终执行了2个或以上分片，则返回结果中该实例会出现2次以上
		 */
		return new ParallelExchangeResult(results);
	}

	public static class Config {
		/**
		 * 每个分片的最多候选实例数
		 */
		private int maxCandidatesOfShard = 3;
		/**
		 * 最多分片数
		 */
		private int maxShards = 64;
		/**
		 * 最多并行线程数
		 */
		private int maxParallelThreads = 10000;

		public Config() {
		}

		public Config(int maxCandidatesOfShard, int maxShards, int maxParallelThreads) {
			/**
			 * maxParallelThreads不能小于maxShards
			 */
			maxParallelThreads = Math.max(maxParallelThreads, maxShards);

			this.maxCandidatesOfShard = maxCandidatesOfShard;
			this.maxShards = maxShards;
			this.maxParallelThreads = maxParallelThreads;
		}

		public int getMaxCandidatesOfShard() {
			return maxCandidatesOfShard;
		}

		public int getMaxShards() {
			return maxShards;
		}

		public int getMaxParallelThreads() {
			return maxParallelThreads;
		}
	}
}

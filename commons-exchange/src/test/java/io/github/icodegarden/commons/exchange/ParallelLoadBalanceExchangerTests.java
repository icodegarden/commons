package io.github.icodegarden.commons.exchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.exchange.exception.AllInstanceFailedExchangeException;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.DefaultMetricsInstance;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.exchange.nio.NioProtocol;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.Dimension;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;
import io.github.icodegarden.commons.lang.registry.DefaultRegisteredInstance;
import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.pool.NioClientPool;
import io.github.icodegarden.commons.nio.pool.NioClientSupplier;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ParallelLoadBalanceExchangerTests {
	NioProtocol protocol = new NioProtocol(
			NioClientPool.newPool(CandidatesSwitchableExchanger.class.getSimpleName(), NioClientSupplier.DEFAULT));

	InstanceLoadBalance instanceLoadBalance = mock(InstanceLoadBalance.class);

	/**
	 * 负载均衡发现0个
	 * 
	 * @throws Exception
	 */
	@Test
	void exchange_NoQualifiedInstanceExchangeException_onLoadBalance0() throws Exception {
		doReturn(new LinkedList<>()).when(instanceLoadBalance).selectCandidates("worker", Integer.MAX_VALUE);

		LoadBalanceExchanger loadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol, instanceLoadBalance,
				"worker");

		assertThatExceptionOfType(NoQualifiedInstanceExchangeException.class)
				.isThrownBy(() -> loadBalanceExchanger.exchange(new Object(), 3000));
	}

	/**
	 * 负载均衡发现1个，但是isOverload 是true
	 * 
	 * @throws Exception
	 */
	@Test
	void exchange_NoQualifiedInstanceExchangeException_onLoadBalance1Overload() throws Exception {
		MetricsInstance loadBalancedInstance = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 6)));
		Queue<MetricsInstance> queue = new LinkedList<MetricsInstance>();
		queue.add(loadBalancedInstance);
		doReturn(queue).when(instanceLoadBalance).selectCandidates("worker", Integer.MAX_VALUE);

		LoadBalanceExchanger loadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol, instanceLoadBalance,
				"worker");

		assertThatExceptionOfType(NoQualifiedInstanceExchangeException.class)
				.isThrownBy(() -> loadBalanceExchanger.exchange(new Object(), 3000));
	}

	/**
	 * 负载均衡发现2个，第1个通讯失败，第2个isOverload 是true<br>
	 * 但实例交互失败
	 * 
	 * @throws Exception
	 */
	@Test
	void exchange_AllInstanceFailedExchangeException_0() throws Exception {
		MetricsInstance loadBalancedInstance1 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 0)));

		MetricsInstance loadBalancedInstance2 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker2", "1.1.1.2", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 6)));

		Queue<MetricsInstance> queue = new LinkedList<MetricsInstance>();
		queue.add(loadBalancedInstance1);
		queue.add(loadBalancedInstance2);
		doReturn(queue).when(instanceLoadBalance).selectCandidates("worker", Integer.MAX_VALUE);

		LoadBalanceExchanger loadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol, instanceLoadBalance,
				"worker");

		assertThatExceptionOfType(AllInstanceFailedExchangeException.class)
				.isThrownBy(() -> loadBalanceExchanger.exchange(new Object(), 3000));
	}

	/**
	 * 负载均衡发现2个，2个通讯失败<br>
	 * 但实例交互失败
	 * 
	 * @throws Exception
	 */
	@Test
	void exchange_AllInstanceFailedExchangeException_1() throws Exception {
		MetricsInstance loadBalancedInstance1 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 0)));

		MetricsInstance loadBalancedInstance2 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker2", "1.1.1.2", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 0)));

		Queue<MetricsInstance> queue = new LinkedList<MetricsInstance>();
		queue.add(loadBalancedInstance1);
		queue.add(loadBalancedInstance2);
		doReturn(queue).when(instanceLoadBalance).selectCandidates("worker", Integer.MAX_VALUE);

		LoadBalanceExchanger loadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol, instanceLoadBalance,
				"worker");

		assertThatExceptionOfType(AllInstanceFailedExchangeException.class)
				.isThrownBy(() -> loadBalanceExchanger.exchange(new Object(), 3000));
	}

	/**
	 * 负载均衡发现2个，第1个通讯失败，第2个通讯可以成功<br>
	 * 
	 * @throws Exception
	 */
	@Test
	void exchange_OK_0() throws Exception {
		MetricsInstance loadBalancedInstance1 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 0)));

		MetricsInstance loadBalancedInstance2 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker2", "1.1.1.2", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 0)));

		Queue<MetricsInstance> queue = new LinkedList<MetricsInstance>();
		queue.add(loadBalancedInstance1);
		queue.add(loadBalancedInstance2);
		doReturn(queue).when(instanceLoadBalance).selectCandidates("worker", Integer.MAX_VALUE);

		NioClient nioClient2 = mock(NioClient.class);
		NioProtocol protocol = new NioProtocol(NioClientPool.newPool("new", (ip, port) -> {
			if (ip.equals("1.1.1.2")) {
				// mock 成功的交互结果
				InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(true, null, null);
				doReturn(exchangeResult).when(nioClient2).request(any(), anyInt());
				return nioClient2;
			}
			// mock 失败的交互结果
			NioClient nioClient = mock(NioClient.class);
			InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(false, null,
					ExchangeFailedReason.clientConnectFailed("failed", null));
			doReturn(exchangeResult).when(nioClient).request(any(), anyInt());
			return nioClient;
		}));
		ParallelLoadBalanceExchanger loadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol,
				instanceLoadBalance, "worker");

		ParallelExchangeResult result = loadBalanceExchanger.exchange(new Object(), 3000);
		assertThat(result.getShardExchangeResults().size()).isEqualTo(2);// 虽然只有1个实例通讯成功，但是分片是2，结果的2个实例其实都是同一个
		assertThat(result.getShardExchangeResults().get(0).successResult().instance()).isEqualTo(loadBalancedInstance2);// 都是实例2成功的
		assertThat(result.getShardExchangeResults().get(1).successResult().instance()).isEqualTo(loadBalancedInstance2);// 都是实例2成功的
		verify(nioClient2, times(2)).request(any(), anyInt());// client2 触发2次
	}

	/**
	 * 负载均衡发现2个，2个都成功<br>
	 * 
	 * @throws Exception
	 */
	@Test
	void exchange_OK_1() throws Exception {
		MetricsInstance loadBalancedInstance1 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker1", "1.1.1.1", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 0)));

		MetricsInstance loadBalancedInstance2 = new DefaultMetricsInstance(
				new DefaultRegisteredInstance("worker", "worker2", "1.1.1.2", 10000),
				new Metrics(new Dimension(DimensionName.Jobs, 3, 0)));

		Queue<MetricsInstance> queue = new LinkedList<MetricsInstance>();
		queue.add(loadBalancedInstance1);
		queue.add(loadBalancedInstance2);
		doReturn(queue).when(instanceLoadBalance).selectCandidates("worker", Integer.MAX_VALUE);

		NioClient nioClient1 = mock(NioClient.class, "c1");
		NioClient nioClient2 = mock(NioClient.class, "c2");
		NioProtocol protocol = new NioProtocol(NioClientPool.newPool("new", (ip, port) -> {
			if (ip.equals("1.1.1.2")) {
				// mock 成功的交互结果
				InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(true, null, null);
				doReturn(exchangeResult).when(nioClient2).request(any(), anyInt());
				return nioClient2;
			}
			// mock 成功的交互结果
			InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(true, null, null);
			doReturn(exchangeResult).when(nioClient1).request(any(), anyInt());
			return nioClient1;
		}));
		ParallelLoadBalanceExchanger loadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol,
				instanceLoadBalance, "worker");

		ParallelExchangeResult result = loadBalanceExchanger.exchange(new Object(), 3000);
		assertThat(result.getShardExchangeResults().size()).isEqualTo(2);// 2个实例都成功
		verify(nioClient1, times(1)).request(any(), anyInt());// client1 触发1次
		verify(nioClient2, times(1)).request(any(), anyInt());// client2 触发1次
	}
}

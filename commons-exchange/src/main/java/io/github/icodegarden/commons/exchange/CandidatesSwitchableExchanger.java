package io.github.icodegarden.commons.exchange;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.exchange.exception.AllInstanceFailedExchangeException;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.ExchangeException.ExchangeFailedInstance;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoSwitchableExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.exception.remote.ClientInvalidRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.ConnectFailedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.ServerErrorRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.TimeoutRemoteException;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 实例失败时自动切换，努力达到成功<br>
 * 
 * @author Fangfang.Xu
 */
public class CandidatesSwitchableExchanger implements Exchanger<ShardExchangeResult> {

	private static final Logger log = LoggerFactory.getLogger(CandidatesSwitchableExchanger.class);

	private Protocol protocol;
	private final Collection<MetricsInstance> candidates;
	private final boolean filter;

	public CandidatesSwitchableExchanger(Protocol protocol, Collection<MetricsInstance> candidates, boolean filter) {
		this.protocol = protocol;
		this.candidates = candidates;
		this.filter = filter;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * <h1>选择未超载的实例，按顺序选择实例进行交互，交互失败则换实例，直到有一个成功或全部失败</h1> 但这些情况时不会再切换实例：
	 * {@link ExchangeFailedReason#KEYWORD_REQUEST_TIMEOUT},
	 * {@link ExchangeFailedReason#KEYWORD_SERVER_EXCEPTION},
	 * {@link ExchangeFailedReason#KEYWORD_CLIENT_INVALID} <br>
	 * 
	 * @param body
	 * @throws ExchangeException 所有的失败都是该异常抛出
	 * @return 成功时才返回
	 */
	@Override
	public ShardExchangeResult exchange(Object body, int timeout) throws ExchangeException {
		Collection<MetricsInstance> instances = candidates;
		if (filter) {
			/**
			 * 过滤掉超载的
			 */
			instances = candidates.stream().filter(instance -> !instance.isOverload()).collect(Collectors.toList());
		}
		if (instances.isEmpty()) {
			throw new NoQualifiedInstanceExchangeException(candidates);
		}

		return exchangeInstances(body, timeout, candidates, instances);
	}

	private ShardExchangeResult exchangeInstances(Object body, int timeout, Collection<MetricsInstance> candidates,
			Collection<MetricsInstance> instances) throws ExchangeException {
		List<ExchangeFailedInstance> exchangedInstances = new LinkedList<ExchangeFailedInstance>();
		List<InstanceExchangeResult> results = new LinkedList<InstanceExchangeResult>();

		for (MetricsInstance instance : instances) {
			InstanceExchangeResult result = exchangeInstance(instance, body, timeout);
			results.add(result);
			if (result.isSuccess()) {
				/**
				 * 只要有1个实例成功，就终止
				 */
				return new ShardExchangeResult(results);
			}

			ExchangeFailedInstance ExchangedInstance = new ExchangeFailedInstance(instance, result.exchangedTimes(),
					result.failedReason());
			exchangedInstances.add(ExchangedInstance);

			if (ExchangeFailedReason.KEYWORD_REQUEST_TIMEOUT.equals(result.failedReason().getKeyword())
					|| ExchangeFailedReason.KEYWORD_CLIENT_INVALID.equals(result.failedReason().getKeyword())
					|| ExchangeFailedReason.KEYWORD_SERVER_EXCEPTION.equals(result.failedReason().getKeyword())) {
				throw new NoSwitchableExchangeException(candidates, exchangedInstances);
			}
		}

		throw new AllInstanceFailedExchangeException(candidates, exchangedInstances);
	}

	/**
	 * 不会抛出异常
	 * 
	 * @param instance
	 * @param body
	 * @param exchangedInstances
	 * @return NotNull
	 */
	private InstanceExchangeResult exchangeInstance(MetricsInstance instance, Object body, int timeout) {
		RegisteredInstance registeredInstance = instance.getAvailable();

		/**
		 * 由于任务有提供重试配置，这里不对相同实例使用重试机制
		 */
		ExchangeFailedReason exchangeFailedReason = null;
		try {
			ProtocolParams params = new ProtocolParams(registeredInstance.getScheme(), registeredInstance.getIp(),
					registeredInstance.getPort(), body, timeout);
			
			Object object = protocol.exchange(params);
			if (object instanceof InstanceExchangeResult) {
				InstanceExchangeResult result = (InstanceExchangeResult)object;
				if (result.isSuccess()) {
					return InstanceExchangeResult.setInstance(instance, result);
				}
				exchangeFailedReason = result.failedReason();
			} else {
				return new InstanceExchangeResult.Default(true, instance, 1, object, exchangeFailedReason);
			}
		} catch (ConnectFailedRemoteException e) {
			log.error("ex on create Nio Client", e);
			return InstanceExchangeResult.clientWithoutExchange(false, instance,
					ExchangeFailedReason.clientConnectFailed(e.getMessage(), e));
		} catch (TimeoutRemoteException e) {
			exchangeFailedReason = ExchangeFailedReason.requestTimeout(e.getMessage() + ", timeout:" + timeout, e);
		} catch (ClientInvalidRemoteException e) {
			exchangeFailedReason = ExchangeFailedReason.clientInvalid(e.getMessage(), e);
		} catch (ServerErrorRemoteException e) {
			exchangeFailedReason = ExchangeFailedReason.serverException(e.getMessage(), e);
		} catch (Exception e) {
			/**
			 * 其他的都可以视为clientException
			 */
			log.error("ex on exchange with instance:{}", instance, e);
			exchangeFailedReason = ExchangeFailedReason.clientException(e.getMessage(), e);
		}

		return new InstanceExchangeResult.Default(false, instance, 1, null, exchangeFailedReason);
	}
}

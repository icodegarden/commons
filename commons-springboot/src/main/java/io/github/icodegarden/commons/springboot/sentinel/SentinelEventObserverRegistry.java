package io.github.icodegarden.commons.springboot.sentinel;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.util.Assert;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStateChangeObserver;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SentinelEventObserverRegistry {

	private SentinelEventObserverRegistry() {
	}

	private final Map<String, BlockExceptionObserver> observerMap = new ConcurrentSkipListMap<>();

	public void addBlockExceptionObserver(String name, BlockExceptionObserver observer) {
		Assert.notNull(name, "name cannot be null");
		Assert.notNull(observer, "observer cannot be null");
		observerMap.put(name, observer);
	}

	public boolean removeBlockExceptionObserver(String name) {
		Assert.notNull(name, "name cannot be null");
		return observerMap.remove(name) != null;
	}

//	public Map<String, BlockExceptionObserver> getBlockExceptionObservers() {
//		return observerMap;
//	}

	public void notifyBlockException(BlockException e) {
		for (BlockExceptionObserver ob : observerMap.values()) {
			ob.onBlockException(e);
		}
	}

	public void addCircuitBreakerObserver(String name, CircuitBreakerStateChangeObserver observer) {
		EventObserverRegistry.getInstance().addStateChangeObserver(name, observer);
	}

	public boolean removeCircuitBreakerObserver(String name) {
		return EventObserverRegistry.getInstance().removeStateChangeObserver(name);
	}

	public static SentinelEventObserverRegistry getInstance() {
		return InstanceHolder.instance;
	}

	private static class InstanceHolder {
		private static SentinelEventObserverRegistry instance = new SentinelEventObserverRegistry();
	}

}
package io.github.icodegarden.commons.springboot.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.context.ApplicationEventPublisher;

import io.github.icodegarden.commons.lang.Delegateable;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuple4;
import io.github.icodegarden.commons.springboot.event.RemoveCacheEvent;
import io.github.icodegarden.wing.Cacher;
import lombok.Getter;

/**
 * <h1>支持spring事务的Cacher</h1>
 * 
 * 例如 自动在事务提交后删除缓存
 * 
 * @author Fangfang.Xu
 *
 */
public class SpringApplicationCacher implements Cacher {

	@Getter
	private final Cacher cacher;

	private final ApplicationEventPublisher applicationEventPublisher;

	public SpringApplicationCacher(Cacher cacher, ApplicationEventPublisher applicationEventPublisher) {
		this.cacher = cacher;
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	@Override
	public Delegateable getDelegator() {
		return cacher;
	}

	@Override
	public <V> V get(String key) {
		return cacher.get(key);
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		return cacher.get(keys);
	}

	@Override
	public <V> V fromSupplier(String key, Supplier<V> supplier, int expireSeconds) {
		return cacher.fromSupplier(key, supplier, expireSeconds);
	}

	@Override
	public <V> V getElseSupplier(String key, Supplier<V> supplier, int expireSeconds) {
		return cacher.getElseSupplier(key, supplier, expireSeconds);
	}

	@Override
	public <V> V getThenPredicateElseSupplier(String key, Predicate<V> predicate, Supplier<V> supplier,
			int expireSeconds) {
		return cacher.getThenPredicateElseSupplier(key, predicate, supplier, expireSeconds);
	}

	@Override
	public <V> Map<String, V> getElseSupplier(Collection<Tuple3<String, Supplier<V>, Integer>> kvts) {
		return cacher.getElseSupplier(kvts);
	}

	@Override
	public <V> Map<String, V> getThenPredicateElseSupplier(
			Collection<Tuple4<String, Predicate<V>, Supplier<V>, Integer>> kvts) {
		return cacher.getThenPredicateElseSupplier(kvts);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		return cacher.set(key, v, expireSeconds);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		return cacher.set(kvts);
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		RemoveCacheEvent event = new RemoveCacheEvent(key);
		applicationEventPublisher.publishEvent(event);

		return null;
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		RemoveCacheEvent event = new RemoveCacheEvent(keys);
		applicationEventPublisher.publishEvent(event);

		return null;
	}

}

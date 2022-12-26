package io.github.icodegarden.commons.springboot.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.springboot.event.RemoveCacheEvent;
import io.github.icodegarden.wing.Cacher;
import lombok.Getter;

/**
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
	public <V> V get(String key) {
		return cacher.get(key);
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		return cacher.get(keys);
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

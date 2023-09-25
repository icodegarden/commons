package io.github.icodegarden.commons.springboot.event;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class RemoveCacheEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	private final Collection<String> cacheKeys;
	private final Long delayMillis;

	public RemoveCacheEvent(String cacheKey) {
		this(cacheKey, null);
	}

	public RemoveCacheEvent(Collection<String> cacheKeys) {
		this(cacheKeys, null);
	}

	public RemoveCacheEvent(String cacheKey, Long delayMillis) {
		super(cacheKey);
		this.cacheKeys = Arrays.asList(cacheKey);
		this.delayMillis = delayMillis;
	}

	public RemoveCacheEvent(Collection<String> cacheKeys, Long delayMillis) {
		super(cacheKeys);
		this.cacheKeys = cacheKeys;
		this.delayMillis = delayMillis;
	}

}
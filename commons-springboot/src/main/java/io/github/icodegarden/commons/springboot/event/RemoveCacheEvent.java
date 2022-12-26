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

	public RemoveCacheEvent(String cacheKey) {
		super(cacheKey);
		this.cacheKeys = Arrays.asList(cacheKey);
	}

	public RemoveCacheEvent(Collection<String> cacheKeys) {
		super(cacheKeys);
		this.cacheKeys = cacheKeys;
	}
}
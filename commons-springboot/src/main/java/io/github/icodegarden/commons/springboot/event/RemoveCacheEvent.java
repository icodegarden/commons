package io.github.icodegarden.commons.springboot.event;

import java.util.Arrays;
import java.util.List;

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

	private final List<String> cacheKeys;

	public RemoveCacheEvent(String cacheKey) {
		super(cacheKey);
		this.cacheKeys = Arrays.asList(cacheKey);
	}

	public RemoveCacheEvent(List<String> cacheKeys) {
		super(cacheKeys);
		this.cacheKeys = cacheKeys;
	}
}
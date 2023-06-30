package io.github.icodegarden.commons.lang.util;

import java.util.concurrent.atomic.AtomicReference;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ReactorUtils {

	public static <T> T block(Mono<T> mono) {
		AtomicReference<Throwable> referenceE = new AtomicReference<>();
		AtomicReference<T> reference = new AtomicReference<>();

		mono.doOnError(e -> referenceE.set(e))//
				.subscribeOn(Schedulers.immediate())//
				.subscribe(next -> {
					reference.set(next);
				});
		if (referenceE.get() != null) {
			throw new RuntimeException(referenceE.get());
		}
		return reference.get();
	}
}
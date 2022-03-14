package io.github.icodegarden.commons.kafka;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.result.Result1;
import io.github.icodegarden.commons.lang.result.Results;

/**
 *
 * @author Fangfang.Xu
 *
 * @param <V>
 */
public class RetryableExecutor<V> {

	private static final Logger log = LoggerFactory.getLogger(RetryableExecutor.class);

	public Result1<Exception> execute(Callable<V> executor, int retries, long backoffMillis) {
		return execute(executor, retries, backoffMillis, 0);
	}

	/**
	 * safe exec, not throw ex
	 */
	public Result1<Exception> execute(Callable<V> executor, int retries, long backoffMillis, boolean retry) {
		int currentLoop = 0;
		if (retry) {
			if (retries < 1) {
				return Results.of(false, new IllegalArgumentException("retries can not < 1 on retry arg is true"));
			}
			currentLoop = 1;
		}
		return execute(executor, retries, backoffMillis, currentLoop);
	}

	/**
	 * safe exec, not throw ex execute when currentLoop == 0 || currentLoop <=
	 * retries
	 */
	private Result1<Exception> execute(Callable<V> executor, int retries, long backoffMillis, int currentLoop) {
		Exception cause = null;
		while (currentLoop == 0 || currentLoop <= retries) {
			try {
				if (currentLoop != 0 && backoffMillis > 0) {
					Thread.sleep(backoffMillis);
					if (log.isDebugEnabled()) {
						log.debug("retry loop:{}", currentLoop);
					}
				}
				V v = executor.call();
				if (v instanceof Boolean) {
					if ((Boolean) v) {
						return Results.of(true, null);
					}
				} else {
					return Results.of(false, null);
				}
			} catch (UnRetryableException e) {
				cause = e;
				log.error("ex on execute, out of reties:{}", retries, cause);
				break;
			} catch (Exception e) {
				cause = e;
				log.error("ex on execute, out of reties:{}", retries, cause);
			}
			currentLoop++;
		}
		return Results.of(false, cause);
	}

}

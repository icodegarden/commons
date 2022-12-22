package io.github.icodegarden.commons.lang.schedule;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class GracefullyShutdownSchedule extends AbstractSchedule implements GracefullyShutdown {

	@Deprecated
	public boolean start(long initialDelayMillis, long scheduleMillis) {
		return scheduleWithFixedDelay(initialDelayMillis, scheduleMillis);
	}

	@Override
	public String shutdownName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void shutdown() {
		close();
	}
}
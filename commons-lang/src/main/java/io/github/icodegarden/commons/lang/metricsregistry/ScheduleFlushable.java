package io.github.icodegarden.commons.lang.metricsregistry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ScheduleFlushable {

	void enableScheduleFlush(int scheduleMillis);

	void disableScheduleFlush();
	
}

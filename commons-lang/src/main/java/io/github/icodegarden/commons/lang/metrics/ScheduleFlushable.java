package io.github.icodegarden.commons.lang.metrics;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ScheduleFlushable {

	void enableScheduleFlush(int scheduleMillis);

	void disableScheduleFlush();
	
}

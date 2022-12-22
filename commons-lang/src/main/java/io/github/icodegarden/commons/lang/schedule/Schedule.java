package io.github.icodegarden.commons.lang.schedule;

import java.io.Closeable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Schedule extends Closeable {

	boolean scheduleWithFixedDelay(long initialDelayMillis, long scheduleMillis);

	boolean scheduleAtFixedRate(long initialDelayMillis, long scheduleMillis);

	boolean scheduleWithCron(String cron);

	boolean isClosed();
	
	void close();
}

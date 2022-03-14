package io.github.icodegarden.commons.nio.task;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.health.Heartbeat;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class HeartbeatTimerTask {
	private static Logger log = LoggerFactory.getLogger(HeartbeatTimerTask.class);

	public static final long DEFAULT_INTERVAL_MILLIS = 60000;
	private long heartbeatIntervalMillis;

	public static final HeartbeatTimerTask DEFAULT = new HeartbeatTimerTask(DEFAULT_INTERVAL_MILLIS);

	public HeartbeatTimerTask(long heartbeatIntervalMillis) {
		this.heartbeatIntervalMillis = heartbeatIntervalMillis;
	}

	public ScheduleCancelableRunnable register(Heartbeat heartbeat) {
		ScheduleCancelableRunnable scheduleCancelableRunnable = new ScheduleCancelableRunnable(
				"HeartbeatTimerTask-" + heartbeat.toString(), TimerTaskThreadPools.SCHEDULED_THREADPOOLS) {
			@Override
			public void run() {
				try {
					heartbeat.send();
				} catch (Throwable e) {
					log.error("heartbeat:{} send beat occur ex", heartbeat, e);
				}
			}
		};
		scheduleCancelableRunnable.scheduleWithFixedDelay(0, heartbeatIntervalMillis, TimeUnit.MILLISECONDS);
		return scheduleCancelableRunnable;
	}
}

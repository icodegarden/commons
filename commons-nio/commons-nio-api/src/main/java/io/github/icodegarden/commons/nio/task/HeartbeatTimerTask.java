package io.github.icodegarden.commons.nio.task;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.health.Heartbeat;

/**
 * 客户端主动发起
 * 
 * @author Fangfang.Xu
 *
 */
public class HeartbeatTimerTask {
	private static Logger log = LoggerFactory.getLogger(HeartbeatTimerTask.class);

	public static final long DEFAULT_INTERVAL_MILLIS = 60000;
	private long heartbeatIntervalMillis;
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = TimerTaskThreadPools.newScheduledThreadPool(2,
			HeartbeatTimerTask.class.getSimpleName());

	public static final HeartbeatTimerTask DEFAULT = new HeartbeatTimerTask(DEFAULT_INTERVAL_MILLIS);

	public HeartbeatTimerTask(long heartbeatIntervalMillis) {
		this.heartbeatIntervalMillis = heartbeatIntervalMillis;
	}

	public ScheduleCancelableRunnable register(Heartbeat heartbeat) {
		ScheduleCancelableRunnable scheduleCancelableRunnable = new ScheduleCancelableRunnable(
				"HeartbeatTimerTask-" + heartbeat.toString(), scheduledThreadPoolExecutor) {
			@Override
			public void run() {
				try {
					heartbeat.send();
				} catch (Throwable e) {
					log.error("heartbeat:{} send beat occur ex", heartbeat, e);
				}
			}
		};
//		scheduleCancelableRunnable.scheduleWithFixedDelay(0, heartbeatIntervalMillis, TimeUnit.MILLISECONDS);
		/**
		 * 初始delay heartbeatIntervalMillis而不是0
		 */
		scheduleCancelableRunnable.scheduleWithFixedDelay(heartbeatIntervalMillis, heartbeatIntervalMillis,
				TimeUnit.MILLISECONDS);
		return scheduleCancelableRunnable;
	}
}

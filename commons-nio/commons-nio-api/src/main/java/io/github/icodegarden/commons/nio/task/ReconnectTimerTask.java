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
public class ReconnectTimerTask {
	private static Logger log = LoggerFactory.getLogger(ReconnectTimerTask.class);

	private long heartbeatIntervalMillis;

	public static final ReconnectTimerTask DEFAULT = new ReconnectTimerTask(HeartbeatTimerTask.DEFAULT_INTERVAL_MILLIS);

	public ReconnectTimerTask(long heartbeatIntervalMillis) {
		this.heartbeatIntervalMillis = heartbeatIntervalMillis;
	}

	/**
	 * <p>
	 * 以heartbeatIntervalMillis的间隔进行检查
	 * <p>
	 * 当最近一次收到的时间超过了heartbeatIntervalMillis*3，则自动重连
	 * 
	 * @param heartbeat
	 */
	public ScheduleCancelableRunnable register(Heartbeat heartbeat) {
		ScheduleCancelableRunnable scheduleCancelableRunnable = new ScheduleCancelableRunnable(
				"ReconnectTimerTask-" + heartbeat.toString(), TimerTaskThreadPools.SCHEDULED_THREADPOOLS) {
			@Override
			public void run() {
				long lastReceive = heartbeat.lastReceive();
				if (heartbeat.isClosed()) {
					try {
						if (log.isInfoEnabled()) {
							log.info("client heartbeat:{} was closed,reconnect...", heartbeat);
						}
						heartbeat.reconnect();
					} catch (Throwable e) {
						log.error("reconnect failed", e);
					}
				} else if ((System.currentTimeMillis() - lastReceive) >= (heartbeatIntervalMillis * 3)) {
					try {
						if (log.isInfoEnabled()) {
							log.info("client heartbeat:{} lastReceive was timeout:{},reconnect...", heartbeat,
									heartbeatIntervalMillis * 3);
						}
						heartbeat.reconnect();
					} catch (Throwable e) {
						log.error("reconnect failed", e);
					}
				}
			}
		};
		scheduleCancelableRunnable.scheduleWithFixedDelay(heartbeatIntervalMillis, heartbeatIntervalMillis,
				TimeUnit.MILLISECONDS);
		return scheduleCancelableRunnable;
	}
}

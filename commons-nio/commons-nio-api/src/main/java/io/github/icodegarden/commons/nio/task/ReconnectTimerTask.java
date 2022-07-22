package io.github.icodegarden.commons.nio.task;

import java.io.IOException;
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
public class ReconnectTimerTask {
	private static Logger log = LoggerFactory.getLogger(ReconnectTimerTask.class);

	private long heartbeatIntervalMillis;
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = TimerTaskThreadPools
			.newScheduledThreadPool(1/* 检测是轻量的，C端不会有很多client */, ReconnectTimerTask.class.getSimpleName());

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
				"ReconnectTimerTask-" + heartbeat.toString(), scheduledThreadPoolExecutor) {
			@Override
			public void run() {
				boolean shouldReconnect = false;
				long lastReceive = heartbeat.lastReceive();
				if (heartbeat.isClosed()) {
					if (log.isInfoEnabled()) {
						log.info("client heartbeat:{} was closed,reconnect...", heartbeat);
					}
					shouldReconnect = true;
				} else if ((System.currentTimeMillis() - lastReceive) >= (heartbeatIntervalMillis * 3)) {
					if (log.isInfoEnabled()) {
						log.info("client heartbeat:{} lastReceive was timeout:{},reconnect...", heartbeat,
								heartbeatIntervalMillis * 3);
					}
					shouldReconnect = true;
				}

				if (shouldReconnect) {
					try {
						heartbeat.reconnect();
					} catch (Throwable e) {
						/**
						 * 只尝试1次重连，失败则关闭client<br>
						 * 这里只需要只需close不需要cancel任务，cancel任务会在close client时自动触发
						 */
						log.error("reconnect failed, will close client.", e);
						try {
							heartbeat.close();
						} catch (IOException ignore) {
							log.error("WARN ex on close client", ignore);
						}
					}
				}
			}
		};
		scheduleCancelableRunnable.scheduleWithFixedDelay(heartbeatIntervalMillis, heartbeatIntervalMillis,
				TimeUnit.MILLISECONDS);
		return scheduleCancelableRunnable;
	}

	public void shutdown() {
		scheduledThreadPoolExecutor.shutdown();
	}
}

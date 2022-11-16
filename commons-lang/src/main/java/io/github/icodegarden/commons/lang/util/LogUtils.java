package io.github.icodegarden.commons.lang.util;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class LogUtils {

	public static void debugIfEnabled(org.slf4j.Logger log, Runnable runnable) {
		if (log.isDebugEnabled()) {
			runnable.run();
		}
	}

	public static void infoIfEnabled(org.slf4j.Logger log, Runnable runnable) {
		if (log.isInfoEnabled()) {
			runnable.run();
		}
	}

	public static void warnIfEnabled(org.slf4j.Logger log, Runnable runnable) {
		if (log.isWarnEnabled()) {
			runnable.run();
		}
	}
}
package io.github.icodegarden.commons.lang.endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface GracefullyShutdown {

	String shutdownName();

	void shutdown() throws Throwable;

	/**
	 * shutdown的顺序，越小越优先，可以为负数
	 * 
	 * @return
	 */
	default int shutdownOrder() {
		return 0;
	}

	public static class Registry {
		private static final Logger log = LoggerFactory.getLogger(Registry.class);

		private Map<String/* name */, GracefullyShutdown> gracefullyShutdownMap = new HashMap<String, GracefullyShutdown>();

		private static final Registry SINGLETON = new Registry();

		public static Registry singleton() {
			return SINGLETON;
		}

		/**
		 * 覆盖式
		 * 
		 * @param name
		 * @param gracefullyShutdown
		 */
		public void register(GracefullyShutdown gracefullyShutdown) {
			gracefullyShutdownMap.put(gracefullyShutdown.shutdownName(), gracefullyShutdown);
		}

		public boolean contains(String name) {
			return gracefullyShutdownMap.containsKey(name);
		}

		public Collection<String> names() {
			return new HashSet<String>(gracefullyShutdownMap.keySet());
		}

		/**
		 * 遇到失败的跳过
		 */
		public void shutdownRegistered() {
			if (gracefullyShutdownMap.isEmpty()) {
				return;
			}

			List<GracefullyShutdown> gracefullyShutdowns = new ArrayList<GracefullyShutdown>(
					gracefullyShutdownMap.values());
			gracefullyShutdowns.sort(Comparator.comparingInt(GracefullyShutdown::shutdownOrder));

			for (GracefullyShutdown gracefullyShutdown : gracefullyShutdowns) {
				if (log.isInfoEnabled()) {
					log.info("gracefully shutdown name:[{}] of class:[{}]", gracefullyShutdown.shutdownName(),
							gracefullyShutdown.getClass().getSimpleName());
				}
				try {
					gracefullyShutdown.shutdown();
				} catch (Throwable e) {
					log.error("ex on gracefully shutdown name:[{}] of class:[{}]", gracefullyShutdown.shutdownName(),
							gracefullyShutdown.getClass().getSimpleName(), e);
				}
			}

			log.info("gracefully shutdown completed.");
		}
	}

}

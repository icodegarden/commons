package io.github.icodegarden.commons.lang.endpoint;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CloseableGracefullyShutdown implements GracefullyShutdown {
	private static final Logger log = LoggerFactory.getLogger(CloseableGracefullyShutdown.class);

	private final Closeable closeable;
	private final String name;
	private final int order;

	public CloseableGracefullyShutdown(Closeable closeable, String name, int order) {
		this.closeable = closeable;
		this.name = name;
		this.order = order;
	}

	@Override
	public void shutdown() {
		try {
			closeable.close();
		} catch (Exception e) {
			log.error("WARN ex on gracefully shutdown name:[{}] of class:[{}] close", name,
					closeable.getClass().getSimpleName(), e);
		}
	}

	@Override
	public String shutdownName() {
		return name;
	}

	@Override
	public int shutdownOrder() {
		return order;
	}
}
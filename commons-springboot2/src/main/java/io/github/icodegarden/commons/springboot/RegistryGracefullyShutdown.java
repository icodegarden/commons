package io.github.icodegarden.commons.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RegistryGracefullyShutdown implements GracefullyShutdown {

	private static final Logger log = LoggerFactory.getLogger(RegistryGracefullyShutdown.class);

	private int gracefullyShutdownOrder = Integer.MIN_VALUE;// 优先级最高

	private final io.github.icodegarden.commons.lang.concurrent.registry.Registry registry;

	public RegistryGracefullyShutdown(io.github.icodegarden.commons.lang.concurrent.registry.Registry registry) {
		this.registry = registry;
	}

	public void setGracefullyShutdownOrder(int gracefullyShutdownOrder) {
		this.gracefullyShutdownOrder = gracefullyShutdownOrder;
	}

	@Override
	public String shutdownName() {
		return "icodegarden-Registry";
	}

	@Override
	public void shutdown() {
		log.info("do icodegarden Registry graceful shutdown...");
		registry.close();
	}

	@Override
	public int shutdownOrder() {
		return gracefullyShutdownOrder;
	}
}
package io.github.icodegarden.commons.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ServiceRegistryGracefullyShutdown implements GracefullyShutdown {

	private static final Logger log = LoggerFactory.getLogger(ServiceRegistryGracefullyShutdown.class);

	private int gracefullyShutdownOrder = Integer.MIN_VALUE;//优先级最高

	private final ServiceRegistry serviceRegistry;
	private final Registration registration;

	public ServiceRegistryGracefullyShutdown(ServiceRegistry serviceRegistry, Registration registration) {
		this.serviceRegistry = serviceRegistry;
		this.registration = registration;
	}

	public void setGracefullyShutdownOrder(int gracefullyShutdownOrder) {
		this.gracefullyShutdownOrder = gracefullyShutdownOrder;
	}

	@Override
	public String shutdownName() {
		return "springcloud-serviceRegistry";
	}

	@Override
	public void shutdown() {
		log.info("do springcloud serviceRegistry graceful shutdown...");
		serviceRegistry.deregister(registration);
		serviceRegistry.close();
	}

	@Override
	public int shutdownOrder() {
		return gracefullyShutdownOrder;
	}
}
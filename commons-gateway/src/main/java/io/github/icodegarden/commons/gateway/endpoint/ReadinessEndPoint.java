package io.github.icodegarden.commons.gateway.endpoint;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.cloud.gateway.route.RouteLocator;

import io.github.icodegarden.commons.gateway.properties.CommonsGatewayPropertiesConstants;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import lombok.extern.slf4j.Slf4j;

/**
 * 单独设置一个readiness的接口，网关一旦接收到shutdown命令或容器触发shutdown等，spring Lifecycle 就对
 * readiness接口就调整为响应非200，并阻塞等待超过readiness探测次数的时间（之后新的请求便不再进来），最后等待剩余请求处理完毕就可以下线（spring
 * gateway支持server.shutdown: graceful，自动等待请求处理完）
 * 
 * @author Fangfang.Xu
 *
 */
@Endpoint(id = "readiness", enableByDefault = true)
@Slf4j
public class ReadinessEndPoint implements GracefullyShutdown {

	private volatile boolean closed;

	@Autowired
	private RouteLocator routeLocator;
	@Value("${" + CommonsGatewayPropertiesConstants.READINESS_SHUTDOWN_BLOCKMS + ":30000}")
	private int readinessShutdownBlockMs;

	@PostConstruct
	void init() {
		GracefullyShutdown.Registry.singleton().register(this);
	}

	@ReadOperation
	public String readiness() throws IllegalStateException {
		if (closed) {
			throw new IllegalStateException("Server Closed");
		}

		this.routeLocator.getRoutes().subscribe();

		return ReadinessState.ACCEPTING_TRAFFIC.name();
	}

	@Override
	public String shutdownName() {
		return "gateway-readiness";
	}

	@Override
	public void shutdown() {
		closed = true;
		try {
			log.info("readiness shutdown block ms:{}", readinessShutdownBlockMs);
			Thread.sleep(readinessShutdownBlockMs);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 优先级最高
	 */
	@Override
	public int shutdownOrder() {
		return Integer.MIN_VALUE;
	}
}

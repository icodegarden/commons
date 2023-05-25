package io.github.icodegarden.commons.springboot.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.springboot.properties.CommonsEndpointProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * <h1>该类主要对网关使用</h1>
 * 
 * 单独设置一个readiness的接口（单独设置的原因是spring
 * health一般liveness要用，如果和health合一起可能导致liveness检测不过被直接销毁，此时readiness配单独接口，否则可以都配health），
 * 网关一旦接收到shutdown命令或容器触发shutdown等，spring Lifecycle 就对
 * readiness接口就调整为响应非200，并阻塞等待超过readiness探测次数的时间（之后新的请求便不再进来），最后等待剩余请求处理完毕就可以下线
 * （ProcessingRequestCountWebFilter负责，也可以spring gateway支持server.shutdown:
 * graceful，自动等待请求处理完） <br>
 * 
 * @author Fangfang.Xu
 *
 */
@Endpoint(id = "readiness", enableByDefault = true)
@Slf4j
public class ReadinessEndpoint implements GracefullyShutdown, ApplicationListener<ApplicationReadyEvent> {

	private volatile boolean closed;

	@Autowired
	private CommonsEndpointProperties commonsEndpointProperties;

	private ApplicationReadyEvent event;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.event = event;
	}

	@ReadOperation
	public ReadinessState readiness() throws IllegalStateException {
		if (closed) {
			throw new IllegalStateException("Server Closed");
		}
		if (event == null) {
			throw new IllegalStateException("Server NotReady");
		}

		return ReadinessState.ACCEPTING_TRAFFIC;
	}

	@Override
	public String shutdownName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void shutdown() {
		if (isClosed()) {
			return;
		}
		closed = true;
		try {
			Long shutdownWaitMs = commonsEndpointProperties.getReadiness().getShutdownWaitMs();
			log.info("readiness shutdown wait ms:{}", shutdownWaitMs);
			Thread.sleep(shutdownWaitMs);
		} catch (InterruptedException e) {
		}
		log.info("readiness shutdown wait end");
	}
	
	public boolean isClosed() {
		return closed;
	}

	/**
	 * 优先级最高
	 */
	@Override
	public int shutdownOrder() {
		return Integer.MIN_VALUE;
	}
}

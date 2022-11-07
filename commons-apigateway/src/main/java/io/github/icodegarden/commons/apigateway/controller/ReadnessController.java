package io.github.icodegarden.commons.apigateway.controller;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.apigateway.properties.CommonsGatewayPropertiesConstants;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import lombok.extern.slf4j.Slf4j;
import reactor.core.CorePublisher;
import reactor.core.publisher.Mono;

/**
 * 单独设置一个readness的接口，网关一旦接收到shutdown命令或容器触发shutdown等，spring Lifecycle 就对
 * readness接口就调整为响应非200，并阻塞等待超过readness探测次数的时间（之后新的请求便不再进来），最后等待剩余请求处理完毕就可以下线（spring
 * gateway支持server.shutdown: graceful，自动等待请求处理完）
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
@Slf4j
public class ReadnessController implements GracefullyShutdown {

	private volatile boolean closed;

	@Autowired
	private RouteLocator routeLocator;
	@Value("${" + CommonsGatewayPropertiesConstants.READNESS_SHUTDOWN_BLOCKMS + ":30000}")
	private int readnessShutdownBlockMs;

	@PostConstruct
	void init() {
		GracefullyShutdown.Registry.singleton().register(this);
	}

	@GetMapping("readness")
	public ResponseEntity<CorePublisher<String>> readness() {
		if (closed) {
			return ResponseEntity.status(403).body(Mono.just("server closed"));
		}

		this.routeLocator.getRoutes().subscribe();

		return ResponseEntity.ok(Mono.just(""));
	}

	@Override
	public String shutdownName() {
		return "gateway-readness";
	}

	@Override
	public void shutdown() {
		closed = true;
		try {
			log.info("readness shutdown block ms:{}", readnessShutdownBlockMs);
			Thread.sleep(readnessShutdownBlockMs);
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

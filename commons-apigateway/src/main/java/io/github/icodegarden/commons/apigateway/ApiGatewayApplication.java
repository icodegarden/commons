package io.github.icodegarden.commons.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.ReactorNetty;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.commons.apigateway",
		"io.github.icodegarden.commons.springboot.configuration" })
@Slf4j
public class ApiGatewayApplication {

	public static void main(String[] args) throws Exception {
		int availableProcessors = Runtime.getRuntime().availableProcessors();

		String ioSelectCount = System.getProperty(ReactorNetty.IO_SELECT_COUNT);// 默认无
		log.info("found config IO_SELECT_COUNT is {}", ioSelectCount);
		if (!StringUtils.hasText(ioSelectCount)) {
			ioSelectCount = availableProcessors == 1 ? "1" : "2";
		}
		log.info("use IO_SELECT_COUNT:{}", ioSelectCount);
		System.setProperty(ReactorNetty.IO_SELECT_COUNT, ioSelectCount);

		String ioWorkerCount = System.getProperty(ReactorNetty.IO_WORKER_COUNT);// 默认等于cpu线程数，但最少4
		log.info("found config IO_WORKER_COUNT is {}", ioWorkerCount);
		if (!StringUtils.hasText(ioWorkerCount)) {
			ioWorkerCount = "500";// 网关不是计算密集型，数量大比较好，配置成等于pool.max-connections
		}
		log.info("use IO_WORKER_COUNT:{}", ioWorkerCount);
		System.setProperty(ReactorNetty.IO_WORKER_COUNT, ioWorkerCount);

//        String poolLeasingStrategy = System.getProperty(ReactorNetty.POOL_LEASING_STRATEGY);

		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}
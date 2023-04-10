package io.github.icodegarden.commons.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.ReactorNetty;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication
@Slf4j
public class CommonsGatewayApplication {

	/**
	 * 适用于项目自身不需要被spring扫描
	 */
	public static void main(String[] args) throws Exception {
		initServerConfig(args);

		SpringApplication.run(CommonsGatewayApplication.class, args);
	}

	/**
	 * 适用于项目自身需要被spring扫描
	 */
	public static ConfigurableApplicationContext run(Class<?> primarySource, String[] args) {
		initServerConfig(args);

		return SpringApplication.run(new Class[] { CommonsGatewayApplication.class, primarySource }, args);
	}

	public static void initServerConfig(String[] args) {
		int availableProcessors = Runtime.getRuntime().availableProcessors();

		//--------------------------------------------------------------------
		String ioSelectCount = System.getProperty(ReactorNetty.IO_SELECT_COUNT);// 默认无
		log.info("found config IO_SELECT_COUNT is {}", ioSelectCount);
		if (!StringUtils.hasText(ioSelectCount)) {
			ioSelectCount = availableProcessors == 1 ? "1" : "2";
		}
		log.info("use IO_SELECT_COUNT:{}", ioSelectCount);
		System.setProperty(ReactorNetty.IO_SELECT_COUNT, ioSelectCount);

		//--------------------------------------------------------------------
		String ioWorkerCount = System.getProperty(ReactorNetty.IO_WORKER_COUNT);// 默认等于cpu线程数，但最少4
		log.info("found config IO_WORKER_COUNT is {}", ioWorkerCount);
		if (!StringUtils.hasText(ioWorkerCount)) {
			ioWorkerCount = "500";// 网关不是计算密集型，数量大比较好，配置成等于xxx.httpclient.pool.max-connections
		}
		log.info("use IO_WORKER_COUNT:{}", ioWorkerCount);
		System.setProperty(ReactorNetty.IO_WORKER_COUNT, ioWorkerCount);

		//--------------------------------------------------------------------		
		String poolMaxConnections = System.getProperty(ReactorNetty.POOL_MAX_CONNECTIONS);// 默认等于2*cpu线程数，但最少16
		log.info("found config POOL_MAX_CONNECTIONS is {}", poolMaxConnections);
		if (!StringUtils.hasText(poolMaxConnections)) {
			poolMaxConnections = "500";
		}
		log.info("use POOL_MAX_CONNECTIONS:{}", poolMaxConnections);
		System.setProperty(ReactorNetty.POOL_MAX_CONNECTIONS, poolMaxConnections);
		
		//--------------------------------------------------------------------
		/**
		 * 由server.netty.xxx配置
		 */
//		String poolMaxIdleTime = System.getProperty(ReactorNetty.POOL_MAX_IDLE_TIME);// 默认无配置
//		log.info("found config POOL_MAX_IDLE_TIME is {}", poolMaxIdleTime);
//		if (!StringUtils.hasText(poolMaxIdleTime)) {
//			poolMaxIdleTime = "";
//		}
//		log.info("use POOL_MAX_IDLE_TIME:{}", poolMaxIdleTime);
//		System.setProperty(ReactorNetty.POOL_MAX_IDLE_TIME, poolMaxIdleTime);
		
		//--------------------------------------------------------------------
		/**
		 * 由server.netty.xxx配置
		 */
//		String poolMaxLifeTime = System.getProperty(ReactorNetty.POOL_MAX_LIFE_TIME);// 默认无配置
//		log.info("found config POOL_MAX_LIFE_TIME is {}", poolMaxLifeTime);
//		if (!StringUtils.hasText(poolMaxLifeTime)) {
//			poolMaxLifeTime = "";
//		}
//		log.info("use POOL_MAX_LIFE_TIME:{}", poolMaxLifeTime);
//		System.setProperty(ReactorNetty.POOL_MAX_LIFE_TIME, poolMaxLifeTime);
		
//        String poolLeasingStrategy = System.getProperty(ReactorNetty.POOL_LEASING_STRATEGY);

	}
}

package io.github.icodegarden.commons.springboot.configuration;

import java.sql.Connection;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.springboot.GracefullyShutdownLifecycle;
import io.github.icodegarden.commons.springboot.SpringContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用的bean
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@Slf4j
public class CommonsBeanAutoConfiguration {

	@Autowired(required = false)
	private DataSource dataSource;

	/**
	 * 无损上线
	 */
	@PostConstruct
	private void init() {
		/**
		 * 利用getConnection促使连接池初始化完成
		 */
		if (dataSource != null) {
			log.info("commons beans init DataSource pool of getConnection, datasource:{}", dataSource);
			try (Connection connection = dataSource.getConnection();) {
				// do nothing
			} catch (Exception e) {
				log.warn("ex on init DataSource pool of getConnection", e);
			}
		}
	}

	@Bean
	public SpringContext springContext() {
		log.info("commons init bean of SpringContext");
		return new SpringContext();
	}

	/**
	 * 无损下线
	 */
	@ConditionalOnProperty(value = "commons.bean.lifecycle.gracefullyShutdown.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public SmartLifecycle gracefullyShutdownLifecycle() {
		log.info("commons init bean of GracefullyShutdownLifecycle");
		return new GracefullyShutdownLifecycle();
	}

}

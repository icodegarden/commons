package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.lang.endpoint.GracefullyStartup;
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
	private List<GracefullyStartup> gracefullyStartups;
	@Autowired(required = false)
	private List<GracefullyShutdown> gracefullyShutdowns;

	@PostConstruct
	private void init() throws Throwable {
		/**
		 * 无损上线
		 */
		if (!CollectionUtils.isEmpty(gracefullyStartups)) {
			for (GracefullyStartup gracefullyStartup : gracefullyStartups) {
				gracefullyStartup.start();
			}
		}

		/**
		 * 无损下线注册
		 */
		if (!CollectionUtils.isEmpty(gracefullyShutdowns)) {
			gracefullyShutdowns.forEach(gracefullyShutdown -> {
				GracefullyShutdown.Registry.singleton().register(gracefullyShutdown);
			});
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

package io.github.icodegarden.commons.springboot.autoconfigure;

import java.sql.Connection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
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
	@Autowired(required = false)
	private List<GracefullyShutdown> gracefullyShutdowns;

	@PostConstruct
	private void init() throws Exception{
		/**
		 * 无损上线,利用getConnection促使连接池初始化完成
		 */
		if (dataSource != null) {
			log.info("commons beans init DataSource pool of getConnection, datasource:{}", dataSource);
			try (Connection connection = dataSource.getConnection();) {
				// do nothing
			} catch (Exception e) {
				log.warn("ex on init DataSource pool of getConnection", e);
			}
			
			String shardingSphereDataSourceClassStr ="org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource";
			if(ClassUtils.isPresent(shardingSphereDataSourceClassStr,null)) {
				Class<?> shardingSphereDataSourceClass = ClassUtils.forName(shardingSphereDataSourceClassStr, null);
				if( ClassUtils.isAssignableValue(shardingSphereDataSourceClass, dataSource)) {
//					DataSourceUtils.
					//TODO
				}
			}
		}

		/**
		 * 自动注册
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

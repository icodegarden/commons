package io.github.icodegarden.commons.springboot;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.springframework.util.ClassUtils;

import io.github.icodegarden.commons.lang.endpoint.GracefullyStartup;
import io.github.icodegarden.commons.shardingsphere.util.DataSourceUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class DataSourceGracefullyStartup implements GracefullyStartup {

	private final DataSource dataSource;

	public DataSourceGracefullyStartup(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void start() throws Throwable {
		/**
		 * 无损上线,利用getConnection促使连接池初始化完成
		 */
		if (dataSource != null) {
			log.info("commons beans init DataSource pool of getConnection, datasource:{}", dataSource);

			if (ClassUtils.isPresent(
					"io.github.icodegarden.commons.shardingsphere.builder.DataSourceOnlyApiShardingSphereBuilder",
					null)) {
				Class<?> shardingSphereDataSourceClass = ClassUtils.forName(
						"org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource", null);
				if (ClassUtils.isAssignableValue(shardingSphereDataSourceClass, dataSource)) {
					Map<String, DataSource> dataSourceMap = DataSourceUtils
							.dataSourceMap((ShardingSphereDataSource) dataSource);

					dataSourceMap.values().forEach(dataSource -> {
						initGetConnection(dataSource);
					});
				}
			} else {
				initGetConnection(dataSource);
			}
		}
	}

	private void initGetConnection(DataSource dataSource) {
		try (Connection connection = dataSource.getConnection();) {
			// do nothing
		} catch (Exception e) {
			log.warn("ex on init DataSource pool of getConnection", e);
		}
	}

}

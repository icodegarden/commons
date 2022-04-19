package io.github.icodegarden.commons.shardingsphere.util;

import java.io.File;
import java.net.URL;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 这个测试既可以是sharding yaml，也可以是springboot
 * @author Fangfang.Xu
 *
 */
class DataSourceUtilsTests {

	private DataSource dataSource() throws Exception {
		URL url = YamlShardingSphereDataSourceFactory.class.getClassLoader().getResource("shardingsphere-jdbc.yml");
		String file = url.getFile();
		DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(file));
		return dataSource;
	}

	@Test
	void firstDataSource() throws Exception {
		DataSource firstDataSource = DataSourceUtils.firstDataSource((ShardingSphereDataSource) dataSource());
		Assertions.assertThat(firstDataSource).isNotNull();
	}
}

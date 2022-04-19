package io.github.icodegarden.commons.shardingsphere.springboot;
//package io.github.icodegarden.beecomb.master.shardingsphere;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.sql.SQLException;
//
//import javax.sql.DataSource;
//
//import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * 使用springboot方式时不需要
// * @author Fangfang.Xu
// *
// */
//@Configuration
//public class YamlConfiguration {
//
//	@Bean
//	public DataSource dataSource() throws SQLException, IOException {
//		URL url = YamlShardingSphereDataSourceFactory.class.getClassLoader().getResource("shardingsphere-jdbc.yml");
//		String file = url.getFile();
//		DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(file));
//		return dataSource;
//	}
//}

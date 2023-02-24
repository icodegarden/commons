package io.github.icodegarden.commons.test.web;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.ReactiveTransactionManager;

import io.github.icodegarden.commons.test.web.MyReactiveTransactionManager.ExtendDataSourceTransactionManager;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication
public class TestWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestWebApplication.class, args);
	}

//	@Bean
//	public TransactionManager reactiveTransactionManager() {
////		ConnectionFactoryUtils.getConnection(connectionFactory);
//		ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder().initialSize(10).maxSize(10).build();
//		ConnectionPool connectionPool = new ConnectionPool(configuration);
//		return new R2dbcTransactionManager(connectionPool);
//	}
	
//	@Bean
//	public ConnectionFactory connectionFactory() {
//		ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder().build();
//		return new ConnectionPool(configuration);
//	}
//	
//	@Bean
//	public R2dbcTransactionManager connectionFactoryTransactionManager(ConnectionFactory connectionFactory) {
//		return new R2dbcTransactionManager(connectionFactory);
//	}
	
	@Bean
	public ReactiveTransactionManager myReactiveTransactionManager(DataSource dataSource) {
		ExtendDataSourceTransactionManager dataSourceTransactionManager = new ExtendDataSourceTransactionManager(dataSource);
		return new MyReactiveTransactionManager(dataSourceTransactionManager);
	}
}
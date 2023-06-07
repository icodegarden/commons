package io.github.icodegarden.commons.test.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication
public class TestWebFluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestWebFluxApplication.class, args);
	}

//	@Bean
//	public ReactiveTransactionManager myReactiveTransactionManager(DataSource dataSource) {
//		ExtendDataSourceTransactionManager dataSourceTransactionManager = new ExtendDataSourceTransactionManager(dataSource);
//		return new MyReactiveTransactionManager(dataSourceTransactionManager);
//	}

	//-----------------------------------------------------------------------------------------------
	
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
}
package io.github.icodegarden.commons.mybatis;

import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MybatisTestUtils {

	private static SqlSessionFactory sqlSessionFactory;
	private static SqlSessionTemplate sqlSessionTemplate;
	static {
		/**
		 * mybatis-config.xml方式
		 */
		try {
			String resource = "mybatis-config.xml";
			InputStream inputStream = Resources.getResourceAsStream(resource);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		/**
		 * 编码方式，可以使用自定义连接池
		 */
//		DataSource dataSource = TestsDataSourceDependent.DATASOURCE;
//		TransactionFactory transactionFactory = new JdbcTransactionFactory();
//		Environment environment = new Environment("development", transactionFactory, dataSource);
//		Configuration configuration = new Configuration(environment);
//		configuration.addMapper(MysqlMybatisDistributedLockMapper.class);
//		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

		/**
		 * 要使用SqlSessionTemplate作为代理，他实现了动态代理会自动close
		 * session，否则测试高并发获取锁时会导致session不会自动close出问题
		 */
		sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
	}

	public static <T> T getMapper(Class<T> cla) {
//		SqlSession session = sqlSessionFactory.openSession(true);
//		return session.getMapper(cla);

		return sqlSessionTemplate.getMapper(cla);
	}
}

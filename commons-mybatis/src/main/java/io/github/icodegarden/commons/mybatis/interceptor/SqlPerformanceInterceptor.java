package io.github.icodegarden.commons.mybatis.interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;

@Intercepts({
		@Signature(type = StatementHandler.class, method = "query", args = { Statement.class, ResultHandler.class }),
		@Signature(type = StatementHandler.class, method = "update", args = { Statement.class }),
		@Signature(type = StatementHandler.class, method = "batch", args = { Statement.class }) })
public class SqlPerformanceInterceptor implements Interceptor {

	private static final Log log = LogFactory.getLog(SqlPerformanceInterceptor.class);
	private static String DruidPooledPreparedStatement = "com.alibaba.druid.pool.DruidPooledPreparedStatement";
	private static String T4CPreparedStatement = "oracle.jdbc.driver.T4CPreparedStatement";
	private static String OraclePreparedStatementWrapper = "oracle.jdbc.driver.OraclePreparedStatementWrapper";
	private static String ShardingSpherePreparedStatement = "org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement";
	/**
	 * 预估sql长度，跟生成完整sql的性能有关
	 */
	private int estimatedSqlLength = 256;
	/**
	 * 超过这个毫秒数的属于非健康sql
	 */
	private long unhealthMillis = -1;
	/**
	 * SQL 是否格式化
	 */
	private boolean format = false;
	/**
	 * 当使用sharding时是否只关注第一条sql，如分页查询时
	 */
	private boolean firstSqlOnSharding = true;
	/**
	 * 非健康sql默认处理方式是打印err
	 */
	private Consumer<String> unhealthSqlConsumer = sql -> {
		System.err.println(sql);
	};
	private Method oracleGetOriginalSqlMethod;
	private Method druidGetSQLMethod;

	private Method shardingSphereGetSQLMethod;
	private Method shardingSphereGetParamtersMethod;
	private Field shardingSphereSqlField;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		long start = System.currentTimeMillis();

		Statement statement;
		Object firstArg = invocation.getArgs()[0];
		if (Proxy.isProxyClass(firstArg.getClass())) {
			statement = (Statement) SystemMetaObject.forObject(firstArg).getValue("h.statement");
		} else {
			statement = (Statement) firstArg;
		}
		MetaObject stmtMetaObj = SystemMetaObject.forObject(statement);
		try {
			statement = (Statement) stmtMetaObj.getValue("stmt.statement");
		} catch (Exception e) {
			// do nothing
		}
		if (stmtMetaObj.hasGetter("delegate")) {
			// Hikari
			try {
				statement = (Statement) stmtMetaObj.getValue("delegate");
			} catch (Exception ignored) {

			}
		}

		List<Object> paramters = extractParamters(statement);

		Object result = invocation.proceed();
		long timing = System.currentTimeMillis() - start;

		if (timing > unhealthMillis) {
			StringBuilder formatSql;
			try {
				String originalSql = resolveOriginalSql(statement, paramters);

				// 格式化 SQL 打印执行结果
				Object target = PluginUtils.realTarget(invocation.getTarget());
				MetaObject metaObject = SystemMetaObject.forObject(target);
				MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
				formatSql = new StringBuilder(estimatedSqlLength).append(" Time：").append(timing).append(" ms - ID：")
						.append(ms.getId());
				if (format) {
					formatSql.append(StringPool.NEWLINE).append("Execute SQL：")
							.append(SqlUtils.sqlFormat(originalSql, format)).append(StringPool.NEWLINE);
				} else {
					formatSql.append("Execute SQL：").append(SqlUtils.sqlFormat(originalSql, format));
				}

				try {
					unhealthSqlConsumer.accept(formatSql.toString());
				} catch (Exception e) {
					log.error("WARN ex on consume unhealth sql", e);
				}
			} catch (Exception e) {
				log.error("WARN ex on handle unhealth sql", e);
			}
		}
		return result;
	}

	private String resolveOriginalSql(Statement statement, List<Object> paramters) {
		String originalSql = null;
		String stmtClassName = statement.getClass().getName();
		if (DruidPooledPreparedStatement.equals(stmtClassName)) {
			try {
				if (druidGetSQLMethod == null) {
					Class<?> clazz = Class.forName(DruidPooledPreparedStatement);
					druidGetSQLMethod = clazz.getMethod("getSql");
				}
				Object stmtSql = druidGetSQLMethod.invoke(statement);
				if (stmtSql instanceof String) {
					originalSql = (String) stmtSql;
				}
			} catch (Exception e) {
				log.error("", e);
			}
		} else if (T4CPreparedStatement.equals(stmtClassName) || OraclePreparedStatementWrapper.equals(stmtClassName)) {
			try {
				if (oracleGetOriginalSqlMethod != null) {
					Object stmtSql = oracleGetOriginalSqlMethod.invoke(statement);
					if (stmtSql instanceof String) {
						originalSql = (String) stmtSql;
					}
				} else {
					Class<?> clazz = Class.forName(stmtClassName);
					oracleGetOriginalSqlMethod = getMethodRegular(clazz, "getOriginalSql");
					if (oracleGetOriginalSqlMethod != null) {
						// OraclePreparedStatementWrapper is not a public class, need set this.
						oracleGetOriginalSqlMethod.setAccessible(true);
						if (null != oracleGetOriginalSqlMethod) {
							Object stmtSql = oracleGetOriginalSqlMethod.invoke(statement);
							if (stmtSql instanceof String) {
								originalSql = (String) stmtSql;
							}
						}
					}
				}
			} catch (Exception e) {
				// ignore
			}
		} else if (ShardingSpherePreparedStatement.equals(stmtClassName)) {
			try {
//				if (shardingSphereGetSQLMethod == null) {
//					Class<?> clazz = Class.forName(ShardingSpherePreparedStatement);
//					shardingSphereGetSQLMethod = clazz.getMethod("getRoutedStatements");
//				}
//				/**
//				 * 当分页等查询时，sharding需要向多个库（表）发送sql，例如2个库时 statements 就会相应的有2个
//				 */
//				Collection<PreparedStatement> statements = (Collection<PreparedStatement>) shardingSphereGetSQLMethod
//						.invoke(statement);
//				if (firstSqlOnSharding) {
//					originalSql = statements.stream().findFirst().map(s -> {
//						String sql = s.toString();
//						return resolveRealSql(sql) + "\n!sharding-first";
//					}).get().toString();
//				} else {
//					originalSql = statements.stream().map(s -> {
//						String sql = s.toString();
//						return resolveRealSql(sql);
//					}).collect(Collectors.joining("\n!sharding-" + statements.size()));// 多条sql换行
//				}
//				return originalSql;

				if (shardingSphereSqlField == null) {
					Class<?> clazz = Class.forName(ShardingSpherePreparedStatement);
					shardingSphereSqlField = clazz.getDeclaredField("sql");
					shardingSphereSqlField.setAccessible(true);
				}
				String sql = (String) shardingSphereSqlField.get(statement);
				if (paramters != null) {
					for (Object paramter : paramters) {
						sql = sql.replaceFirst("\\?"/* 该参数是个正则，不能直接?号 */, paramter != null ? paramter.toString() : "null");
					}
				}
				return sql;
			} catch (Exception e) {
				log.error("", e);
			}
		}
		if (originalSql == null) {
			originalSql = statement.toString();
		}
		return resolveRealSql(originalSql);
	}

	private List<Object> extractParamters(Statement statement) {
		String stmtClassName = statement.getClass().getName();

		if (ShardingSpherePreparedStatement.equals(stmtClassName)) {
			try {
				if (shardingSphereGetParamtersMethod == null) {
					Class<?> clazz = Class.forName(ShardingSpherePreparedStatement);
					shardingSphereGetParamtersMethod = clazz.getMethod("getParameters");
				}
				List<Object> paramters = (List<Object>) shardingSphereGetParamtersMethod.invoke(statement);
				return new ArrayList<Object>(paramters);//要赋值一份新的，否则引用会发生变化
			} catch (Exception e) {
				log.error("WARN ex on extractParamters", e);
			}
		}
		return null;
	}

	private String resolveRealSql(String originalSql) {
		originalSql = originalSql.replaceAll("[\\s]+", StringPool.SPACE);
		int index = indexOfSqlStart(originalSql);
		if (index > 0) {
			originalSql = originalSql.substring(index);
		}
		return originalSql;
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	@Deprecated
	@Override
	public void setProperties(Properties prop) {
//		String format = prop.getProperty("format");
//		if (StringUtils.isNotEmpty(format)) {
//			this.format = Boolean.valueOf(format);
//		}
	}

	/**
	 * 获取此方法名的具体 Method
	 *
	 * @param clazz      class 对象
	 * @param methodName 方法名
	 * @return 方法
	 */
	public Method getMethodRegular(Class<?> clazz, String methodName) {
		if (Object.class.equals(clazz)) {
			return null;
		}
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return getMethodRegular(clazz.getSuperclass(), methodName);
	}

	/**
	 * 获取sql语句开头部分
	 *
	 * @param sql ignore
	 * @return ignore
	 */
	private int indexOfSqlStart(String sql) {
		String upperCaseSql = sql.toUpperCase();
		Set<Integer> set = new HashSet<>();
		set.add(upperCaseSql.indexOf("SELECT "));
		set.add(upperCaseSql.indexOf("UPDATE "));
		set.add(upperCaseSql.indexOf("INSERT "));
		set.add(upperCaseSql.indexOf("DELETE "));
		set.remove(-1);
		if (CollectionUtils.isEmpty(set)) {
			return -1;
		}
		List<Integer> list = new ArrayList<>(set);
		list.sort(Comparator.naturalOrder());
		return list.get(0);
	}

	public void setEstimatedSqlLength(int estimatedSqlLength) {
		this.estimatedSqlLength = estimatedSqlLength;
	}

	public long getUnhealthMillis() {
		return unhealthMillis;
	}

	public void setUnhealthMillis(long unhealthMillis) {
		this.unhealthMillis = unhealthMillis;
	}

	public boolean isFormat() {
		return format;
	}

	public void setFormat(boolean format) {
		this.format = format;
	}

	public void setUnhealthSqlConsumer(Consumer<String> unhealthSqlConsumer) {
		this.unhealthSqlConsumer = unhealthSqlConsumer;
	}

	public boolean isFirstSqlOnSharding() {
		return firstSqlOnSharding;
	}

	public void setFirstSqlOnSharding(boolean firstSqlOnSharding) {
		this.firstSqlOnSharding = firstSqlOnSharding;
	}

	// -------------------------------------

}
package io.github.icodegarden.commons.mybatis.interceptor;

import java.lang.reflect.Proxy;
import java.util.Properties;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

class PluginUtils {
	public static final String DELEGATE_BOUNDSQL_SQL = "delegate.boundSql.sql";

	/**
	 * 获得真正的处理对象,可能多层代理.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T realTarget(Object target) {
		if (Proxy.isProxyClass(target.getClass())) {
			MetaObject metaObject = SystemMetaObject.forObject(target);
			return realTarget(metaObject.getValue("h.target"));
		}
		return (T) target;
	}

	/**
	 * 根据 key 获取 Properties 的值
	 */
	public static String getProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		return StringUtils.isEmpty(value) ? null : value;
	}
}
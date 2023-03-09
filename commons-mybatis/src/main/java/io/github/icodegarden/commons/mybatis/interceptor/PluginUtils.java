package io.github.icodegarden.commons.mybatis.interceptor;

import java.lang.reflect.Proxy;
import java.util.Properties;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

class PluginUtils {
	public static final String DELEGATE_BOUNDSQL_SQL = "delegate.boundSql.sql";

	/**
	 * 获得真正的处理对象,可能多层代理.
	 * @return Nullable
	 */
	@SuppressWarnings("unchecked")
	public static <T> T realTarget(Object t) {
		int counter = 0;
		int max = 10;
		Object target = t;
		while (counter++ < max && Proxy.isProxyClass(target.getClass())) {
			MetaObject metaObject = SystemMetaObject.forObject(target);
			target = metaObject.getValue("h.target");
		}
		
		if(counter == max) {
			return null;
		}
		
		return (T) target;

//		if (Proxy.isProxyClass(t.getClass())) {
//			MetaObject metaObject = SystemMetaObject.forObject(t);
//			return realTarget(metaObject.getValue("h.target"));
//		}
//		return (T) t;
	}

	/**
	 * 根据 key 获取 Properties 的值
	 */
	public static String getProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		return StringUtils.isEmpty(value) ? null : value;
	}
}
package io.github.icodegarden.commons.lang;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.util.ThreadUtils;

/**
 * 只关注给定的names<br>
 * 定时获取实时数据，listNamedObjects返回的是缓存数据<br>
 * 允许外部通过add remove增删元素，目的是为了保障数据近实时性<br>
 * listNamedObjects的name 要在 构造参数names 中<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class NamesCachedObjectReader implements NamedObjectReader<NamedObject>, Closeable {
	private static final Logger log = LoggerFactory.getLogger(NamesCachedObjectReader.class);

	private static final long DEFAULT_CACHE_REFRESH_INTERVAL_MILLIS = 10000;
	/**
	 * 每个对象独占1线程
	 */
	private final ScheduledThreadPoolExecutor scheduleRefreshCacheThreadPool = ThreadUtils
			.newSingleScheduledThreadPool("NamesCachedObjectReader-refreshCache");

	private final Map<String, List<? extends NamedObject>> name_objects;
	private final Collection<String> names;
	private final NamedObjectReader<? extends NamedObject> delegator;

	public NamesCachedObjectReader(Collection<String> names, NamedObjectReader<? extends NamedObject> delegator) {
		this(names, delegator, DEFAULT_CACHE_REFRESH_INTERVAL_MILLIS);
	}

	public NamesCachedObjectReader(Collection<String> names, NamedObjectReader<? extends NamedObject> delegator,
			long cacheRefreshIntervalMillis) {
		if (names == null || names.isEmpty()) {
			throw new IllegalArgumentException("names must not empty");
		}
		if (delegator == null) {
			throw new IllegalArgumentException("delegator must not null");
		}

		this.names = names;
		this.delegator = delegator;

		name_objects = new HashMap<String, List<? extends NamedObject>>(names.size(), 1);
		for (String name : names) {
			name_objects.put(name, new LinkedList<NamedObject>());// 允许添加元素
		}

		scheduleRefreshCacheThreadPool.scheduleWithFixedDelay(() -> {
			try {
				refreshCache();
			} catch (Exception e) {
				log.error("WARNING ex on refreshCache", e);
			}
		}, 0, cacheRefreshIntervalMillis, TimeUnit.MILLISECONDS);
	}

	void refreshCache() {
		for (String name : this.names) {
			try {
				List<? extends NamedObject> listNamedObjects = delegator.listNamedObjects(name);
				if (!listNamedObjects.isEmpty()) {
					name_objects.put(name, listNamedObjects);// 全新替换
				}
			} catch (Exception e) {
				log.error("WARNING ex on refreshCache, name:{}", name, e);
			}
		}
	}

	/**
	 * copy一份保障返回结果线程安全
	 */
	@Override
	public List<NamedObject> listNamedObjects(String name) {
		if (!names.contains(name)) {
			throw new IllegalArgumentException(
					String.format("name [%s] not supported, support names were [%s]", name, names));
		}
		List<? extends NamedObject> list = name_objects.get(name);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return new ArrayList<NamedObject>(list);
	}

	public boolean addObject(NamedObject object) {
		List<NamedObject> list = (List) name_objects.get(object.name());

		if (list != null) {
			return list.add(object);
		}
		return false;
	}

	public boolean removeObject(NamedObject object) {
		List<? extends NamedObject> list = name_objects.get(object.name());
		if (list != null) {
			return list.remove(object);
		}
		return false;
	}
	
	/**
	 * 停止调度
	 */
	@Override
	public void close() throws IOException {
		scheduleRefreshCacheThreadPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		scheduleRefreshCacheThreadPool.shutdown();
	}
}

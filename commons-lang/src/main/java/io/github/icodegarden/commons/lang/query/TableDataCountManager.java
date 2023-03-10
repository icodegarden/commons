package io.github.icodegarden.commons.lang.query;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;

/**
 * 允许任意组合 Collector 和 Storage<br>
 * 
 * 使用 {@link #allowCount(String)} 前需要先调用 {@link #start()}开启调度，否则出异常<br>
 * 
 * 如需停止需要调用{@link #stop()}<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class TableDataCountManager {
	private static final Logger log = LoggerFactory.getLogger(TableDataCountManager.class);

	public static final int DEFAULT_ALLOW_COUNT_THRESHOLD = 500000;

	private Map<String/* tableName */, Long/* total */> cache;

	private final TableDataCountCollector tableCountCollector;
	private final TableDataCountStorage tableCountStorage;
	private long allowCountThreshold;

	private ScheduledFuture<?> future;

	public TableDataCountManager(TableDataCountCollector tableCountCollector, TableDataCountStorage tableCountStorage) {
		this(tableCountCollector, tableCountStorage, DEFAULT_ALLOW_COUNT_THRESHOLD);
	}

	public TableDataCountManager(TableDataCountCollector tableCountCollector, TableDataCountStorage tableCountStorage,
			long allowCountThreshold) {
		this.tableCountCollector = tableCountCollector;
		this.tableCountStorage = tableCountStorage;
		this.allowCountThreshold = allowCountThreshold;
	}

	public boolean allowCount(String tableName) {
		if (future == null) {
			throw new IllegalStateException("schedule is not start, please start it before this.");
		}
		refreshCacheIfNull();

		Long count = cache.get(tableName);
		if (count == null) {
			return true;
		}
		return count < allowCountThreshold;
	}

	public void start(long initialDelayMillis, long scheduleMillis) {
		ScheduledThreadPoolExecutor scheduledThreadPool = ThreadPoolUtils
				.newSingleScheduledThreadPool("TableDataCount-Schedule");
		this.future = scheduledThreadPool.scheduleWithFixedDelay(() -> {
			try {
				doSchedule();
			} catch (Exception e) {
				log.error("WARN ex on refreshCache", e);
			}
		}, initialDelayMillis, scheduleMillis, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		future.cancel(false);
		future = null;
	}

	/**
	 * 缓存中的表缺少，则补充<br>
	 * 缓存中的count已超过，不再处理；否则更新count值
	 */
	private void doSchedule() {
		List<String> tables = tableCountCollector.listTables();

		refreshCacheIfNull();

		for (String table : tables) {
			Long tableCount = cache.get(table);
			if (tableCount == null) {
				// 新增，可能并发
				try {
					create(table);
				} catch (io.github.icodegarden.commons.lang.exception.DuplicateKeyException ignore) {
					log.info("duplicate table name:{} on table", table);
				}
				tableCount = 0L;
			}

			if (tableCount < allowCountThreshold) {
				long count = tableCountCollector.countTable(table);
				tableCountStorage.updateCount(table, count);

				cache.put(table, count);
			}
		}
	}

	private void refreshCacheIfNull() {
		if (cache == null) {
			List<TableDataCountPO> list = tableCountStorage.findAll();
			cache = list.stream()
					.collect(Collectors.toMap(TableDataCountPO::getTableName, TableDataCountPO::getDataCount));
		}
	}

	private void create(String tableName) {
		TableDataCountPO po = new TableDataCountPO();
		po.setTableName(tableName);
		po.setUpdatedAt(SystemUtils.now());
		po.setDataCount(0L);
		tableCountStorage.add(po);
	}

}

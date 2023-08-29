package io.github.icodegarden.commons.hbase.dao;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.hbase.HBaseEnv;
import io.github.icodegarden.commons.hbase.HBaseEnv.VersionFrom;
import io.github.icodegarden.commons.hbase.query.HBaseQuery;
import io.github.icodegarden.commons.lang.Validateable;
import io.github.icodegarden.commons.lang.dao.Dao;
import io.github.icodegarden.commons.lang.query.NextQuerySupportArrayList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportPage;
import io.github.icodegarden.commons.lang.util.PageHelperUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class HBaseDao<PO, U, Q extends HBaseQuery<W>, W, DO> implements Dao<PO, U, Q, W, DO, String> {
	private static final Logger log = LoggerFactory.getLogger(HBaseDao.class);

	private static final Charset CHARSET = Charset.forName("utf-8");

	protected String tableName;
	protected byte[] tableNameBytes;
	protected String familyName;
	protected byte[] familyNameBytes;

	protected final HBaseEnv hbaseEnv;
	protected final Connection connection;

	public HBaseDao(HBaseEnv hbaseEnv, String tableName, String familyName) {
		this.hbaseEnv = hbaseEnv;
		connection = hbaseEnv.getConnection();

		this.tableName = tableName;
		this.tableNameBytes = tableName.getBytes(CHARSET);
		this.familyName = familyName;
		this.familyNameBytes = familyName.getBytes(CHARSET);
	}

	@Override
	public void add(PO po) {
		validate(po);

		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			Put put = buildPutOnAdd(po);
			table.put(put);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void addBatch(Collection<PO> pos) {
		if (CollectionUtils.isEmpty(pos)) {
			return;
		}
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			List<Put> puts = pos.stream().map(po -> {
				validate(po);
				Put put = buildPutOnAdd(po);
				return put;
			}).collect(Collectors.toList());

			table.put(puts);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int update(U update) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			Put put = buildPutOnUpdate(update);
			table.put(put);
			return 1;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int updateBatch(Collection<U> updates) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			List<Put> puts = updates.stream().map(update -> buildPutOnUpdate(update)).collect(Collectors.toList());
			table.put(puts);
			return updates.size();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	protected abstract Put buildPutOnAdd(PO po);

	protected abstract Put buildPutOnUpdate(U update);

	@Override
	public NextQuerySupportPage<DO> findAll(Q query) {
		if (log.isInfoEnabled()) {
			log.info("find all query:{}, table:{}", query, tableNameBytes);
		}
		query.validate();

		NextQuerySupportList<DO> nextQuerySupportList = null;

		long total = 0;
		boolean isCount = PageHelperUtils.isCount();
		/**
		 * 是否count
		 */
		if (isCount) {
			total = count(query);
			if (total == 0) {
				nextQuerySupportList = NextQuerySupportArrayList.empty();
			} else {
				nextQuerySupportList = doFindAll(query);
			}
		} else {
			nextQuerySupportList = doFindAll(query);
			total = nextQuerySupportList.size();
		}

		NextQuerySupportPage<DO> page = new NextQuerySupportPage<DO>(query.getPage(), query.getSize(), total, isCount,
				query.getOrderBy(), nextQuerySupportList);
		return page;
	}

	private NextQuerySupportList<DO> doFindAll(HBaseQuery<W> query) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			Scan scan = new Scan();
			scan.setCaching(1000);// default 100

			List<Filter> filters = queryFilters(query);
			if (filters != null && !filters.isEmpty()) {
				scan.setFilter(new FilterList(filters));
			}

			if (query.getSearchAfter() != null) {
				scan.withStartRow(Bytes.toBytes(query.getSearchAfter()), false);
			}
			if (query.getSearchBefore() != null) {
				scan.withStopRow(Bytes.toBytes(query.getSearchBefore()), true);
			}
			if (query.getOrderBy() != null && query.getOrderBy().contains("desc")) {
				scan.setReversed(true);// default order by rowkey asc
			}
			scan.setLimit(query.getSize() + 1);// more 1

			/**
			 * 此时需要多查1行
			 */
			if (willFindFirstUnwanted(query, scan)) {
				scan.setLimit(query.getSize() + 2);// more 2
			}

			List<byte[]> queryColumns = queryColumns(query.getWith());
			for (byte[] column : queryColumns) {
				scan.addColumn(familyNameBytes, column);
			}

			ResultScanner scanner = table.getScanner(scan);

			List<DO> list = new LinkedList<>();
			AtomicReference<Boolean> hasNextPage = new AtomicReference<Boolean>(false);
			boolean ignoredFirst = false;
			int i = 0;
			for (Result result : scanner) {
				if (!ignoredFirst && willFindFirstUnwanted(query, scan)) {
					/**
					 * 要忽略startRow
					 */
					ignoredFirst = true;
					continue;
				}
				if (i != query.getSize()) {// not last one
					DO obj = extractResult(result);
					list.add(obj);
				} else {// more 1,last
					hasNextPage.set(true);
					break;
				}
				i++;
			}
			scanner.close();

			NextQuerySupportList<DO> nextQuerySupportList = NextQuerySupportArrayList.newSupportSearchAfter(list,
					last -> hasNextPage.get() ? extractSearchAfter(last) : null);

			return nextQuerySupportList;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * <h1>是否会导致查询出第一行不想要的数据</h1> 阿里云在 withStartRow +
	 * reversed时，结果和原生有区别。原生是正确的，而阿里会包含withStartRow
	 * 
	 * @param query
	 * @param scan
	 * @return
	 */
	private boolean willFindFirstUnwanted(HBaseQuery<W> query, Scan scan) {
		return hbaseEnv.getVersionFrom() == VersionFrom.AliyunLindorm && scan.isReversed()
				&& query.getSearchAfter() != null;
	}

	@Override
	public long count(Q query) {
		query.validate();

		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			Scan scan = new Scan();
			scan.setCaching(1000);// default 100

			List<Filter> filters = queryFilters(query);
			if (filters != null && !filters.isEmpty()) {
				scan.setFilter(new FilterList(filters));
			}

//			if (query.getSearchAfter() != null) {
//				scan.withStartRow(Bytes.toBytes(query.getSearchAfter()), false);
//			}
//			if (query.getSearchBefore() != null) {
//				scan.withStopRow(Bytes.toBytes(query.getSearchBefore()), true);
//			}
//			if (query.getSort() != null && query.getSort().contains("desc")) {
//				scan.setReversed(true);// default order by rowkey asc
//			}
//			scan.setLimit(query.getSize() + 1);// more 1	
//			
//			/**
//			 * 此时需要多查1行
//			 */
//			if(willFindFirstUnwanted(query, scan)) {
//				scan.setLimit(query.getSize() + 2);// more 2
//			}
//
//			List<byte[]> queryColumns = queryColumns(query.getWith());
//			for (byte[] column : queryColumns) {
//				scan.addColumn(FAMILY_NAME, column);
//			}
//
//			ResultScanner scanner = table.getScanner(scan);
//
//			List<DO> list = new LinkedList<>();
//			AtomicReference<Boolean> hasNextPage = new AtomicReference<Boolean>(false);
//			boolean ignoredFirst = false;
//			int i = 0;
//			for (Result result : scanner) {
//				if(!ignoredFirst && willFindFirstUnwanted(query, scan)) {
//					/**
//					 * 要忽略startRow
//					 */
//					ignoredFirst = true;
//					continue;
//				}
//				if (i != query.getSize()) {// not last one
//					DO obj = extractResult(result);
//					list.add(obj);
//				} else {// more 1,last
//					hasNextPage.set(true);
//					break;
//				}
//				i++;
//			}
//			NextQuerySupportList<DO> nextQuerySupportList = NextQuerySupportList.newSupportSearchAfter(list,
//					last -> hasNextPage.get() ? last.getId() : null);
//
//			scanner.close();
//			return nextQuerySupportList;

			AggregationClient aggregationClient = hbaseEnv.getAggregationClient();
			return aggregationClient.rowCount(table, new LongColumnInterpreter(), scan);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public DO findOne(String id, W with) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			Get get = new Get(Bytes.toBytes(id));

			List<byte[]> queryColumns = queryColumns(with);
			for (byte[] column : queryColumns) {
				get.addColumn(familyNameBytes, column);
			}

			Result result = table.get(get);

			if (result.getRow() == null) {
				return null;
			}

			DO obj = extractResult(result);

			return obj;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<DO> findByIds(List<String> ids, W with) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			List<Get> gets = ids.stream().map(id -> {
				Get get = new Get(Bytes.toBytes(id));

				List<byte[]> queryColumns = queryColumns(with);
				for (byte[] column : queryColumns) {
					get.addColumn(familyNameBytes, column);
				}
				return get;
			}).collect(Collectors.toList());

			Result[] results = table.get(gets);
			List<DO> list = Arrays.asList(results).stream().map(result -> extractResult(result))
					.collect(Collectors.toList());

			return list;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	protected abstract List<Filter> queryFilters(HBaseQuery<W> query);

	protected abstract List<byte[]> queryColumns(W with);

	protected abstract DO extractResult(Result result);

	/**
	 * @return Nullable null表示不支持SearchAfter
	 */
	protected abstract String extractSearchAfter(DO obj);

	@Override
	public int delete(String id) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			Delete delete = new Delete(Bytes.toBytes(id));
			table.delete(delete);
			return 1;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int deleteBatch(Collection<String> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return 0;
		}
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableNameBytes))) {
			List<Delete> deletes = ids.stream().map(id -> new Delete(Bytes.toBytes(id))).collect(Collectors.toList());
			table.delete(deletes);
			return ids.size();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public int deleteByQuery(Q query) {
		throw new UnsupportedOperationException();
	}

	private void validate(Object obj) {
		if (obj instanceof Validateable) {
			((Validateable) obj).validate();
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String getFamilyName() {
		return familyName;
	}
}

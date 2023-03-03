package io.github.icodegarden.commons.hbase.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import io.github.icodegarden.commons.hbase.HBaseEnv;
import io.github.icodegarden.commons.hbase.query.HBaseQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DemoHBaseDao extends HBaseDao<DemoPO, DemoPO, HBaseQuery<Object>, Object, DemoPO> {

	public DemoHBaseDao(HBaseEnv hbaseEnv, String tableName, String familyName) {
		super(hbaseEnv, tableName, familyName);
	}

	@Override
	protected Put buildPutOnAdd(DemoPO po) {
		Put put = new Put(Bytes.toBytes(po.getId()));
		if (po.getVin() != null) {
			put.addColumn(familyNameBytes, Bytes.toBytes("vin"), Bytes.toBytes(po.getVin()));
		}
		// 省略其他字段
		return put;
	}

	@Override
	protected Put buildPutOnUpdate(DemoPO update) {
		Put put = new Put(Bytes.toBytes(update.getId()));
		if (update.getVin() != null) {
			put.addColumn(familyNameBytes, Bytes.toBytes("vin"), Bytes.toBytes(update.getVin()));
		}
		// 省略其他字段
		return put;
	}

	@Override
	protected List<Filter> queryFilters(HBaseQuery<Object> query) {
		String vin = "";
		Boolean b = true;

		List<Filter> filters = new LinkedList<Filter>();
		if (vin != null) {
			SingleColumnValueFilter filter = new SingleColumnValueFilter(familyNameBytes, Bytes.toBytes("vin"),
					CompareOperator.EQUAL, Bytes.toBytes(vin));
			filter.setFilterIfMissing(true);// filter null
			filters.add(filter);
		}
		if (b != null) {
			SingleColumnValueFilter filter = new SingleColumnValueFilter(familyNameBytes, Bytes.toBytes("is_test"),
					CompareOperator.EQUAL, Bytes.toBytes(b));
			filter.setFilterIfMissing(true);// filter nullb
			filters.add(filter);
		}
		/**
		 * 可以返回null
		 */
		return filters;
	}

	private static List<byte[]> baseQueryColumns = new ArrayList<byte[]>();
	static {
		baseQueryColumns.add(Bytes.toBytes("is_allow_dispatch"));
		baseQueryColumns.add(Bytes.toBytes("is_corrected"));
		baseQueryColumns.add(Bytes.toBytes("is_dispatched"));
		baseQueryColumns.add(Bytes.toBytes("is_ordered"));
		baseQueryColumns.add(Bytes.toBytes("is_sharded"));
		baseQueryColumns.add(Bytes.toBytes("is_validated"));
		baseQueryColumns.add(Bytes.toBytes("data_mob_id"));
		baseQueryColumns.add(Bytes.toBytes("data_shard_count"));
		baseQueryColumns.add(Bytes.toBytes("data_size"));
		baseQueryColumns.add(Bytes.toBytes("data_type"));
		baseQueryColumns.add(Bytes.toBytes("data_source"));
		baseQueryColumns.add(Bytes.toBytes("dispatched_shard"));
		baseQueryColumns.add(Bytes.toBytes("dispatch_topic"));
		baseQueryColumns.add(Bytes.toBytes("order_key"));
		baseQueryColumns.add(Bytes.toBytes("parent_id"));
		baseQueryColumns.add(Bytes.toBytes("serialize_type"));
	}
	private static Map<String, byte[]> withQueryColumns = new HashMap<String, byte[]>(8, 1);
	static {
		withQueryColumns.put("created_at", Bytes.toBytes("created_at"));
		withQueryColumns.put("created_by", Bytes.toBytes("created_by"));
		withQueryColumns.put("updated_at", Bytes.toBytes("updated_at"));
		withQueryColumns.put("updated_by", Bytes.toBytes("updated_by"));
		withQueryColumns.put("data_url", Bytes.toBytes("data_url"));
		withQueryColumns.put("data", Bytes.toBytes("data"));
	}

	@Override
	protected List<byte[]> queryColumns(Object with) {
		List<byte[]> selectColumns = new LinkedList<>(baseQueryColumns);

		if (with != null) {
//			if (with.isCreatedAt()) {
//				selectColumns.add(withQueryColumns.get("created_at"));
//			}
		}
		return selectColumns;
	}

	@Override
	protected DemoPO extractResult(Result result) {
		DemoPO po = new DemoPO();
		po.setId(Bytes.toString(result.getRow()));

//		byte[] is_allow_dispatch = result.getValue(familyNameBytes, Bytes.toBytes("is_allow_dispatch"));
//		if (is_allow_dispatch != null) {
//			po.setAllowDispatch(Bytes.toBoolean(is_allow_dispatch));
//		}
//		byte[] created_at = result.getValue(familyNameBytes, Bytes.toBytes("created_at"));
//		if (created_at != null) {
//			dataMainDO.setCreatedAt(
//					LocalDateTime.parse(Bytes.toString(created_at), SystemUtils.STANDARD_DATETIME_FORMATTER));
//		}
//		byte[] created_by = result.getValue(familyNameBytes, Bytes.toBytes("created_by"));
//		if (created_by != null) {
//			dataMainDO.setCreatedBy(Bytes.toString(created_by));
//		}
//		byte[] data_size = result.getValue(familyNameBytes, Bytes.toBytes("data_size"));
//		if (data_size != null) {
//			dataMainDO.setDataSize(Bytes.toInt(data_size));
//		}
		return po;
	}

	@Override
	protected String extractSearchAfter(DemoPO obj) {
		return obj.getId();
	}

}

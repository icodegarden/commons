package io.github.icodegarden.commons.exchange;

import java.io.Serializable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ParallelShardObject implements Serializable {
	private static final long serialVersionUID = -7335121609731956617L;

	private Object obj;

	private int shard;

	private int shardTotal;

	/**
	 * kryo
	 */
	ParallelShardObject() {
	}

	public ParallelShardObject(Object obj, int shard, int shardTotal) {
		this.obj = obj;
		this.shard = shard;
		this.shardTotal = shardTotal;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public int getShard() {
		return shard;
	}

	public void setShard(int shard) {
		this.shard = shard;
	}

	public int getShardTotal() {
		return shardTotal;
	}

	public void setShardTotal(int shardTotal) {
		this.shardTotal = shardTotal;
	}

	@Override
	public String toString() {
		return "ParallelShardObject [obj=" + obj + ", shard=" + shard + ", shardTotal=" + shardTotal + "]";
	}

}

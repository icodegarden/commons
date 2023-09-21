package io.github.icodegarden.commons.lang;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ShardObject {

	int getShard();

	void setShard(int shard);

	int getShardTotal();

	void setShardTotal(int shardTotal);

}
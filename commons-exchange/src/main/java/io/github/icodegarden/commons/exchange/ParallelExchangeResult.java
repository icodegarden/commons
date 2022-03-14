package io.github.icodegarden.commons.exchange;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ParallelExchangeResult implements ExchangeResult {
	private static final long serialVersionUID = 8255692236629153404L;

	private final List<ShardExchangeResult> shardExchangeResults;

	public ParallelExchangeResult(List<ShardExchangeResult> shardExchangeResults) {
		if (shardExchangeResults == null || shardExchangeResults.isEmpty()) {
			throw new IllegalArgumentException("shardExchangeResults must not empty");
		}

		this.shardExchangeResults = shardExchangeResults;
	}

	/**
	 * 并行任务返回体是null
	 */
	@Override
	public Object response() {
		return null;
	}

	public List<ShardExchangeResult> getShardExchangeResults() {
		return shardExchangeResults;
	}
}
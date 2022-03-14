package io.github.icodegarden.commons.exchange;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ShardExchangeResult implements ExchangeResult {
	private static final long serialVersionUID = 4984166637403823242L;
	
	private InstanceExchangeResult successResult;
	private final List<InstanceExchangeResult> failedResults;

	public ShardExchangeResult(List<InstanceExchangeResult> instanceExchangeResults) {
		if (instanceExchangeResults == null || instanceExchangeResults.isEmpty()) {
			throw new IllegalArgumentException("instanceExchangeResults must not empty");
		}

		failedResults = new LinkedList<InstanceExchangeResult>();
		for (InstanceExchangeResult instanceExchangeResult : instanceExchangeResults) {
			if (instanceExchangeResult.isSuccess()) {
				if (successResult != null) {
					throw new IllegalArgumentException("more than one success in instanceExchangeResults");
				}
				successResult = instanceExchangeResult;
			} else {
				failedResults.add(instanceExchangeResult);
			}
		}

		if (successResult == null) {
			throw new IllegalArgumentException("must one success in instanceExchangeResults");
		}
	}

	/**
	 * 有且只有一个成功的
	 * 
	 * @return
	 */
	public InstanceExchangeResult successResult() {
		return successResult;
	}

	/**
	 * 
	 * @return 所有失败的，有失败的原因是候选实例失败时切换
	 */
	public List<InstanceExchangeResult> failedResults() {
		return failedResults;
	}

	@Override
	public Object response() {
		return successResult.response();
	}

}
package io.github.icodegarden.commons.exchange;

import io.github.icodegarden.commons.exchange.exception.ExchangeException;

/**
 * 
 * 
 * @author Fangfang.Xu
 *
 */
public interface Exchanger<ER extends ExchangeResult> {

	/**
	 * 
	 * @param body
	 * @throws ExchangeException 所有的失败（包括处理结果的失败）都以该异常抛出
	 * @return 成功时才返回
	 */
	ER exchange(Object body, int timeout) throws ExchangeException;

}

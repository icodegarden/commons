package io.github.icodegarden.commons.exchange;

import java.io.Serializable;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 该对象表示成功的结果
 * 
 * @author Fangfang.Xu
 *
 */
public interface ExchangeResult extends Serializable {

	@Nullable
	Object response();
	
}

package io.github.icodegarden.commons.springboot.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BlockExceptionObserver {

	void onBlockException(BlockException e);
}

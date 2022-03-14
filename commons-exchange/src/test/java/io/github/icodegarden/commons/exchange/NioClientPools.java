package io.github.icodegarden.commons.exchange;

import io.github.icodegarden.commons.exchange.CandidatesSwitchableExchanger;
import io.github.icodegarden.commons.nio.pool.NioClientPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class NioClientPools {

	public static final NioClientPool DEFAULT_NIO_CLIENT_POOL = NioClientPool
			.newPool(CandidatesSwitchableExchanger.class.getSimpleName(), NioClientSuppliers.DEFAULT);

}
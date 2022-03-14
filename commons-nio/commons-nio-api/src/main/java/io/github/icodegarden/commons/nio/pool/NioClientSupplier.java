package io.github.icodegarden.commons.nio.pool;

import io.github.icodegarden.commons.nio.NioClient;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@FunctionalInterface
public interface NioClientSupplier {

	NioClient get(String ip,int port);
}

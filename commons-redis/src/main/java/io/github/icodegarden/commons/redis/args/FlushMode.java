package io.github.icodegarden.commons.redis.args;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public enum FlushMode {

	/**
	 * flushes synchronously
	 */
	SYNC,

	/**
	 * flushes asynchronously
	 */
	ASYNC;

//    public final byte[] bytes;
//
//    FlushMode() {
//        bytes = name().getBytes(StandardCharsets.US_ASCII);
//    }
//
//    @Override
//    public byte[] getBytes() {
//        return bytes;
//    }
}
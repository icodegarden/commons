//package io.github.icodegarden.commons.redis;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public interface Unsubscribe {
//	boolean isSubscribed();
//
//	/**
//	 * unsubscribe all
//	 */
//	void unsubscribe();
//
//	/**
//	 * 不可以传(byte[])null,否则入参是[null]而不是null
//	 * 
//	 * @param channels Notnull
//	 */
//	void unsubscribe(byte[]... channels);
//}
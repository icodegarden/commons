package io.github.icodegarden.commons.lang.algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5 hash算法
 * 
 * @author Fangfang.Xu
 *
 */
public class MD5Function implements HashFunction {

	/**
	 * 这个对象不是线程安全的，需要独占
	 */
	private MessageDigest instance;

	public MD5Function() {
		try {
			instance = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("NOT SUPPORT MD5", e);
		}
	}

	@Override
	public int hash(Object obj) {
		String s = obj.toString();
		
		instance.reset();
		instance.update(s.getBytes());
		byte[] digest = instance.digest();

		int h = 0;
		for (int i = 0; i < 4; i++) {
			h <<= 8;
			h |= ((int) digest[i]) & 0xFF;
		}
		return h;
	}
}
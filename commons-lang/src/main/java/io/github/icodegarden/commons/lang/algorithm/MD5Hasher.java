package io.github.icodegarden.commons.lang.algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5 hash算法
 * 
 * @author Fangfang.Xu
 *
 */
public class MD5Hasher implements Hasher {

	/**
	 * 这个对象不是线程安全的，需要独占
	 */
	private MessageDigest instance;

	public MD5Hasher() {
		try {
			instance = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("NOT SUPPORT MD5", e);
		}
	}

	@Override
	public long hash(String s) {
		instance.reset();
		instance.update(s.getBytes());
		byte[] digest = instance.digest();

		long h = 0;
		for (int i = 0; i < 4; i++) {
			h <<= 8;
			h |= ((int) digest[i]) & 0xFF;
		}
		return h;
	}
}
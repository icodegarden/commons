package io.github.icodegarden.commons.lang.util;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AESUtils {

	private static final String KEY_ALGORITHM = "AES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

	/**
	 * 指定随机字符串（密码）生成密钥
	 *
	 * @param randomKey 加解密的密码
	 * @throws Exception
	 */
	private static byte[] getSecretKey(String randomKey) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM); // 秘钥生成器，指定秘钥算法

		// 初始化此密钥生成器，指定AES的秘钥长度为128
		if (randomKey == null) { // 不指定密码
			keyGenerator.init(128);
		} else { // 指定密码
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(randomKey.getBytes());
			keyGenerator.init(128, random);
		}

		SecretKey secretKey = keyGenerator.generateKey(); // 生成密钥
		return secretKey.getEncoded();
	}

	/**
	 * 加密
	 *
	 * @param data 待加密数据
	 * @param key  密钥
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	private static byte[] encrypt(byte[] data, Key key) throws Exception {
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

//	/**
//	 * 加密
//	 *
//	 * @param data 待加密数据
//	 * @param key  二进制密钥
//	 * @return byte[] 加密数据
//	 * @throws Exception
//	 */
//	private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
//		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
//	}

//	/**
//	 * 加密
//	 *
//	 * @param data            待加密数据
//	 * @param key             二进制密钥
//	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
//	 * @return byte[] 加密数据
//	 * @throws Exception
//	 */
//	private static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
//		Key k = toKey(key);
//		return encrypt(data, k, cipherAlgorithm);
//	}

	/**
	 * 加密
	 *
	 * @param data            待加密数据
	 * @param key             密钥
	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	private static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm); // 获取算法
		cipher.init(Cipher.ENCRYPT_MODE, key); // 设置加密模式，并指定秘钥
		return cipher.doFinal(data); // 加密数据
	}

//	/**
//	 * 解密
//	 *
//	 * @param data 待解密数据
//	 * @param key  二进制密钥
//	 * @return byte[] 解密数据
//	 * @throws Exception
//	 */
//	private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
//		return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
//	}

	/**
	 * 解密
	 *
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	private static byte[] decrypt(byte[] data, Key key) throws Exception {
		return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

//	/**
//	 * 解密
//	 *
//	 * @param data            待解密数据
//	 * @param key             二进制密钥
//	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
//	 * @return byte[] 解密数据
//	 * @throws Exception
//	 */
//	private static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
//		Key k = toKey(key);
//		return decrypt(data, k, cipherAlgorithm);
//	}

	/**
	 * 解密
	 *
	 * @param data            待解密数据
	 * @param key             密钥
	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	private static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm); // 获取算法
		cipher.init(Cipher.DECRYPT_MODE, key); // 设置解密模式，并指定秘钥
		return cipher.doFinal(data); // 解密数据
	}

	/**
	 * 转换密钥
	 *
	 * @param secretKey 二进制密钥
	 * @return 密钥
	 */
	private static Key toKey(byte[] secretKey) {
		return new SecretKeySpec(secretKey, KEY_ALGORITHM); // 生成密钥
	}

	public static String encrypt(String data, String aeskey) {
		try {
			byte[] secretKey = getSecretKey(aeskey);
			Key key = toKey(secretKey);

			byte[] encryptData = encrypt(data.getBytes("utf-8"), key);
			String encryptHex = Hex.encodeHexString(encryptData); // 把密文转为16进制

			return encryptHex;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static String decrypt(String encryptHex, String aeskey) {
		try {
			byte[] secretKey2 = getSecretKey(aeskey);
			Key key2 = toKey(secretKey2);

			byte[] strBytes = decrypt(Hex.decodeHex(encryptHex), key2);

			return new String(strBytes, "utf-8");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}

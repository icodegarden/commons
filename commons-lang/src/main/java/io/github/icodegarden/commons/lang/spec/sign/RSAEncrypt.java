package io.github.icodegarden.commons.lang.spec.sign;//package openapi.sign;
//import org.apache.commons.codec.binary.Base64;
//import javax.crypto.Cipher;
//import java.security.KeyFactory;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.interfaces.RSAPrivateKey;
//import java.security.interfaces.RSAPublicKey;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.HashMap;
//import java.util.Map;
//
//public class RSAEncrypt {
//	private static Map<Integer, String> keyMap = new HashMap<Integer, String>(){
//		{
//			put(0, 
//					"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA12FJdRGD6VEc80fO4D1C\n" + 
//					"Msv30qzSLEvxToffPOSKH6C5jY4yjs51j4RLSg8zgFMyF+G3rc+6V4MZe0RIOER7\n" + 
//					"sHZlDEGDHUISSuW/hL+aA2z1LdNlSgdVSqgz0ozkZGRpczc2asTly1SZOqKxqDv4\n" + 
//					"Yt2iS/pDetmxB97I0PqnMSvJcvqaGDIiIeodzaKvFFR3VwzQd0ebQL8DueJpMNK4\n" + 
//					"SElnoq4A5sWXnSHv3xJcb2x1JHQn6ayh6JkPZo0C1xjhj1WKSHSrdnPK5QXb3mSN\n" + 
//					"/Km4u1tSeeF51slrNwKePdisvBQ2tf98PDW/g7zfpYvsmYpWiqWiBmG8EZ0PuKAm\n" + 
//					"4wIDAQAB");
//			put(1, 
//					"MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDXYUl1EYPpURzz\n" + 
//					"R87gPUIyy/fSrNIsS/FOh9885IofoLmNjjKOznWPhEtKDzOAUzIX4betz7pXgxl7\n" + 
//					"REg4RHuwdmUMQYMdQhJK5b+Ev5oDbPUt02VKB1VKqDPSjORkZGlzNzZqxOXLVJk6\n" + 
//					"orGoO/hi3aJL+kN62bEH3sjQ+qcxK8ly+poYMiIh6h3Noq8UVHdXDNB3R5tAvwO5\n" + 
//					"4mkw0rhISWeirgDmxZedIe/fElxvbHUkdCfprKHomQ9mjQLXGOGPVYpIdKt2c8rl\n" + 
//					"BdveZI38qbi7W1J54XnWyWs3Ap492Ky8FDa1/3w8Nb+DvN+li+yZilaKpaIGYbwR\n" + 
//					"nQ+4oCbjAgMBAAECggEBALxqS0yyieduIhlhVEUu0d6GOMKAlUxTE0JT8qLee0a3\n" + 
//					"4BtJrkjnrEhkv0iGi9NS9JuXxouzEZzE/2GqllNC9cGlkThrhzvtNPt4iVakFyOw\n" + 
//					"CbyRFibwy1iIVO14e8SnYuh8AIrevPhnhbSZ5zj5XY1/vh8hTC8IiaoM5ziKTgDg\n" + 
//					"ZcQAmewgw/RUUjDKQJKTkzsLnhCYAAqjcsuG+duOnTlAcJUgO0cEcmdf2gZrLezB\n" + 
//					"Xlj5Crz0xFTNmBypn5EboeLlIxN564hyMqCDX0o3yvG0AWLE3uwRLZDiaSTxFneF\n" + 
//					"rVA5rZP4QASMuX1PTSYh1rpyR0aCi5Sud57/kMNVomECgYEA+UuyKCpmVNMILvXO\n" + 
//					"J0lPeakvDecAcQn4VEkWachYSt/fGy06i+Uv+KsBGfzN+n0qak4Fqa645kmNHjqj\n" + 
//					"Y0+4rA/0RjTbeAgbhuETUTbdb4McB6oRPUZnrNPrKlGDMx0wvicyy9jXbOxUZDm3\n" + 
//					"d5goUlFYwYzs484z3h0d6e2/IHcCgYEA3SwYSJPj6e8qNGgAUbl2KtHcbvAgzAD/\n" + 
//					"0Nzmn0BumZimaQLn0aLVbnROX93r6oddH5lwfqr6TMmZvEsSc71WTPktenpiI+Y8\n" + 
//					"teJgvn9YTOyeMv44xKBfhSpgZYzF1CWVvvAndbK1+4Awz4A1HqfRmp6+NfxNcYKq\n" + 
//					"8gYuvS+s0/UCgYB3KSWJaWrslMrOl5Po13PztVjWjlp5rchTkXPzMF5zu7rLJL1d\n" + 
//					"bTmW5TZrk5lpaDbEBXNTfj7HZNTdZC4BJHQjWqTqgg19vHJb8G3pXg5OZR8a0sHW\n" + 
//					"xjZ8N/eqjLfn0MYhvl7bbVTbrcNQ/P2jXsm3P0brjU8WPNQyETzhOlEnVQKBgQCV\n" + 
//					"20u02WfC2Mb67UDSHtx/FOTL0i4Wq9KpPROR8cE+lxyh6FMUSsVcarez4xT3OrKR\n" + 
//					"N5DucoDXyH0/BIrDGxW/X9I9u3CFrHmUL4ena3dz35stJ+1k5NjzzIu2ItTj8ndt\n" + 
//					"ASFrcSQ2/VuVLORRGho+LrLot0Pt1kBnbmL60rPChQKBgQCrADLnJHgiZPWvWHVs\n" + 
//					"IhpDQy0GMhyJQuM2MrHNG+kCA8zCJChOxQObjD9uAytSqTayI5HBkFPEd7+szPUY\n" + 
//					"1cpWBmhrJQe0UyZEP5/4q3zfCxUoWVOSqUc3HaEN/lokmvCe5na+b4lwJAshdgwa\n" + 
//					"Ucse2Y1UVVZfqMuBr5MyHnCioQ==" );
//		}
//	};  //用于封装随机产生的公钥与私钥
//	public static void main(String[] args) throws Exception {
//		//生成公钥和私钥
////		genKeyPair();
//		//加密字符串
//		String message = "abc=1&d=sss";
//		System.out.println("随机生成的公钥为:" + keyMap.get(0));
//		System.out.println("随机生成的私钥为:" + keyMap.get(1));
////		String messageEn = encryptByPub(message,keyMap.get(0));
////		System.out.println(message + "\t加密后的字符串为:" + messageEn);
////		String messageDe = decryptByPrivate(messageEn,keyMap.get(1));
////		System.out.println("还原后的字符串为:" + messageDe);
//		
//		String messageEn = encryptByPrivate(message,keyMap.get(1));
//		System.out.println(message + "\t加密后的字符串为:" + messageEn);
//		String messageDe = decryptByPub(messageEn,keyMap.get(0));
//		System.out.println("还原后的字符串为:" + messageDe);
//		
//		messageEn = encryptByPub(message,keyMap.get(0));
//		System.out.println(message + "\t加密后的字符串为:" + messageEn);
//	}
//
//	/** 
//	 * 随机生成密钥对 
//	 * @throws NoSuchAlgorithmException 
//	 */  
//	public static void genKeyPair() throws NoSuchAlgorithmException {  
//		// KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象  
//		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");  
//		// 初始化密钥对生成器，密钥大小为96-1024位  
//		keyPairGen.initialize(1024,new SecureRandom());  
//		// 生成一个密钥对，保存在keyPair中  
//		KeyPair keyPair = keyPairGen.generateKeyPair();  
//		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥  
//		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥  
//		String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));  
//		// 得到私钥字符串  
//		String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));  
//		// 将公钥和私钥保存到Map
//		keyMap.put(0,publicKeyString);  //0表示公钥
//		keyMap.put(1,privateKeyString);  //1表示私钥
//	}  
//	/** 
//	 * RSA公钥加密 
//	 *  
//	 * @param str 
//	 *            加密字符串
//	 * @param publicKey 
//	 *            公钥 
//	 * @return 密文 
//	 * @throws Exception 
//	 *             加密过程中的异常信息 
//	 */  
//	public static String encryptByPub( String str, String publicKey ) throws Exception{
//		//base64编码的公钥
//		byte[] decoded = Base64.decodeBase64(publicKey);
//		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
//		//RSA加密
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
//		String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
//		return outStr;
//	}
//
//	/** 
//	 * RSA私钥解密
//	 *  
//	 * @param str 
//	 *            加密字符串
//	 * @param privateKey 
//	 *            私钥 
//	 * @return 铭文
//	 * @throws Exception 
//	 *             解密过程中的异常信息 
//	 */  
//	public static String decryptByPrivate(String str, String privateKey) throws Exception{
//		//64位解码加密后的字符串
//		byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
//		//base64编码的私钥
//		byte[] decoded = Base64.decodeBase64(privateKey);  
//        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));  
//		//RSA解密
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.DECRYPT_MODE, priKey);
//		String outStr = new String(cipher.doFinal(inputByte));
//		return outStr;
//	}
//
//	
//	public static String encryptByPrivate( String str, String privateKey ) throws Exception{
//		//base64编码的公钥
//		byte[] decoded = Base64.decodeBase64(privateKey);
//		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));  
//		//RSA加密
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.ENCRYPT_MODE, priKey);
//		String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
//		return outStr;
//	}
//
//	public static String decryptByPub(String str, String publicKey) throws Exception{
//		//64位解码加密后的字符串
//		byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
//		//base64编码的私钥
//		byte[] decoded = Base64.decodeBase64(publicKey);  
//		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));  
//		//RSA解密
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.DECRYPT_MODE, pubKey);
//		String outStr = new String(cipher.doFinal(inputByte));
//		return outStr;
//	}
//}
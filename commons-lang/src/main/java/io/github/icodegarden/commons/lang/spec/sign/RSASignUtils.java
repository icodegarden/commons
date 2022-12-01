package io.github.icodegarden.commons.lang.spec.sign;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;

/**
 * 适用于双方各有一对RSA公私钥<br>
 * 调用方.私钥签名 -> 接收方.公钥验签, 接口同步或异步响应则与之相反
 * 
 * @author Fangfang.Xu
 *
 */
public class RSASignUtils extends BaseSignUtils {
	private static final Logger log = LoggerFactory.getLogger(RSASignUtils.class);

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";

//	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static enum SignType {
		RSA, RSA2
	}

	public static boolean supports(String signType) {
		try {
			SignType.valueOf(signType);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String requestSign(OpenApiRequestBody body, String privateKey) {
		String buildSignParams = buildRequestSignParams(body, null);
		if (log.isDebugEnabled()) {
			log.debug("request params to sign:{}", buildSignParams);
		}

		String sign_type = body.getSign_type();
		String charset = body.getCharset();

		try {
			return doSign(privateKey, buildSignParams, sign_type, charset);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean validateRequestSign(OpenApiRequestBody body, String publicKey) {
		String buildSignParams = buildRequestSignParams(body, null);

		String sign_type = body.getSign_type();
		String charset = body.getCharset();
		String sign = body.getSign();

		try {
			return doValidate(publicKey, buildSignParams, sign_type, charset, sign);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * charset 使用 UTF-8
	 */
	public static String responseSign(OpenApiResponse body, String sign_type, String privateKey) {
		return responseSign(body, sign_type, "UTF-8", privateKey);
	}

	public static String responseSign(OpenApiResponse body, String sign_type, String charset, String privateKey) {
		String buildSignParams = buildResponseSignParams(body);
		if (log.isDebugEnabled()) {
			log.debug("response params to sign:{}", buildSignParams);
		}
		try {
			return doSign(privateKey, buildSignParams, sign_type, charset);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean validateResponseSign(OpenApiResponse body, String sign_type, String charset,
			String publicKey) {
		String buildSignParams = buildResponseSignParams(body);

		String sign = body.getSign();

		try {
			return doValidate(publicKey, buildSignParams, sign_type, charset, sign);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static String doSign(String privateKey, String buildSignParams, String sign_type, String charset)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException,
			InvalidKeySpecException {
		PrivateKey priKey = null;
		java.security.Signature signature = null;

		if (SignType.RSA.name().equals(sign_type)) {
//            priKey = getPrivateKeyFromPKCS8(SIGN_TYPE_RSA, new ByteArrayInputStream(privateKey.getBytes()));
			priKey = getPrivateKeyFromPKCS8(SignType.RSA.name(), privateKey);
			signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
		} else if (SignType.RSA2.name().equals(sign_type)) {
			priKey = getPrivateKeyFromPKCS8(SignType.RSA.name(), privateKey);
			signature = java.security.Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
		} else {
			throw new IllegalArgumentException("不是支持的签名类型 : : signType=" + sign_type);
		}
		signature.initSign(priKey);

		if (hasText(charset)) {
			signature.update(buildSignParams.getBytes(charset));
		} else {
			signature.update(buildSignParams.getBytes());
		}

		byte[] signed = signature.sign();
		return new String(Base64.getEncoder().encode(signed));
	}

	private static boolean doValidate(String publicKey, String buildSignParams, String sign_type, String charset,
			String sign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
			UnsupportedEncodingException, InvalidKeySpecException {
		java.security.Signature signature = null;
//        PublicKey pubKey = getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey.getBytes()));
		PublicKey pubKey = getPublicKeyFromX509("RSA", publicKey);
		if (SignType.RSA.name().equals(sign_type)) {
			signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
		} else if (SignType.RSA2.name().equals(sign_type)) {
			signature = java.security.Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
		} else {
			throw new IllegalArgumentException("不是支持的签名类型 : signType=" + sign_type);
		}
		signature.initVerify(pubKey);

		if (hasText(charset)) {
			signature.update(buildSignParams.getBytes(charset));
		} else {
			signature.update(buildSignParams.getBytes());
		}

		return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
	}

	private static String buildResponseSignParams(OpenApiResponse body) {
		StringBuilder sb = new StringBuilder("biz_content=").append(body.getBiz_content());
		return sb.toString();
	}

	private static PrivateKey getPrivateKeyFromPKCS8(String algorithm, String privateKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

		byte[] encodedKey = Base64.getDecoder().decode(privateKey.getBytes());

		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
	}

//	private static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins)  {
//		if (ins == null || algorithm.isEmpty()) {
//			return null;
//		}
//
//		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
//
//		byte[] encodedKey = readText(ins).getBytes();
//
//		encodedKey = Base64.getDecoder().decode(encodedKey);
//
//		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
//	}

	private static PublicKey getPublicKeyFromX509(String algorithm, String publicKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

		byte[] encodedKey = Base64.getDecoder().decode(publicKey.getBytes());

		return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
	}

//	private static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins)  {
//		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
//
//		StringWriter writer = new StringWriter();
//		io(new InputStreamReader(ins), writer, -1);
//
//		byte[] encodedKey = writer.toString().getBytes();
//
//		encodedKey = Base64.getDecoder().decode(encodedKey);
//
//		return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
//	}
//
//	private static String readText(InputStream ins) throws IOException {
//		Reader reader = new InputStreamReader(ins);
//		StringWriter writer = new StringWriter();
//
//		io(reader, writer, -1);
//		return writer.toString();
//	}
//
//	private static void io(Reader in, Writer out, int bufferSize) throws IOException {
//		if (bufferSize == -1) {
//			bufferSize = DEFAULT_BUFFER_SIZE >> 1;
//		}
//
//		char[] buffer = new char[bufferSize];
//		int amount;
//
//		while ((amount = in.read(buffer)) >= 0) {
//			out.write(buffer, 0, amount);
//		}
//	}
}

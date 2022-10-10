package io.github.icodegarden.commons.lang.util;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class AESUtilsTests {

	@Test
	void encrypt_decrypt() throws Exception {
		String data = UUID.randomUUID().toString();
		String aeskey = UUID.randomUUID().toString();
		
		String encrypt = AESUtils.encrypt(data, aeskey);//结果是16进制字符串
		System.out.println("encrypt:"+encrypt);
		String decrypt = AESUtils.decrypt(encrypt, aeskey);
		System.out.println("decrypt:"+decrypt);
		
		Assertions.assertThat(decrypt).isEqualTo(data);
	}

}

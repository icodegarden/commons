package io.github.icodegarden.commons.gateway.core.security.signature;

import java.util.HashSet;
import java.util.Set;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class App {

	private String appId;
	private String appName;
	/**
	 * 适用于SHA256\SHA1\MD5的对称密钥<br>
	 * 适用于RSA2签名验证公钥<br>
	 */
	private String appKey;
	/**
	 * RSA2 response签名 私钥
	 */
	@Nullable
	private String privateKey;

	private String flowTagRequired;
	
	private String flowTagFirst;

	private Set<String> methods = new HashSet<>();
}

package io.github.icodegarden.commons.zookeeper;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ACLs {

	public static final ArrayList<ACL> OPEN_ACL_UNSAFE = Ids.OPEN_ACL_UNSAFE;
	public static final ArrayList<ACL> CREATOR_ALL_ACL = Ids.CREATOR_ALL_ACL;
	public static final ArrayList<ACL> READ_ACL_UNSAFE = Ids.READ_ACL_UNSAFE;

	/**
	 * 本机所有ipv4的ACL
	 */
	public static final List<ACL> IPV4S_ALL_ACL = SystemUtils.getIpv4s().stream()
			.map(ipv4 -> new ACL(Perms.ALL, new Id("ip", ipv4))).collect(Collectors.toList());

	public static final List<ACL> AUTH_ALL_ACL = Arrays.asList(new ACL(Perms.ALL, Ids.AUTH_IDS));

	/**
	 * 
	 * @param toDigestStr like xff:xff
	 * @return
	 */
	public static List<ACL> digestAllAcl(String toDigestStr) {
		try {
			return Arrays.asList(
					new ACL(Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(toDigestStr))));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("NoSuchAlgorithm", e);
		}
	}

}

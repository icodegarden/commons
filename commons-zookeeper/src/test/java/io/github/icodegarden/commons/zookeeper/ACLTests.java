package io.github.icodegarden.commons.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ACLTests extends CommonZookeeperBuilder {

	@Test
	void ip() throws Exception {
		zkh.getConnectedZK().create("/testacl-ip", new byte[0], ACLs.IPV4S_ALL_ACL, CreateMode.EPHEMERAL);
		byte[] data = zkh.getConnectedZK().getData("/testacl-ip", false, null);
		Assertions.assertThat(data).isNotNull();
	}

	@Test
	void auth() throws Exception {
		// 要先添加auth到session
		zkh.getConnectedZK().addAuthInfo("digest", "xff:xff".getBytes());
		// 使用Ids.AUTH_IDS即可，new
		// Id("auth",...)其实无效，因为zk只此时只需要固定值Ids.AUTH_IDS，auth的用户密码使用的是session对应的AuthInfo
		zkh.getConnectedZK().create("/testacl-auth", new byte[0], ACLs.AUTH_ALL_ACL, CreateMode.EPHEMERAL);

		byte[] data = zkh.getConnectedZK().getData("/testacl-auth", false, null);
		Assertions.assertThat(data).isNotNull();
	}

	@Test
	void digest() throws Exception {
		zkh.getConnectedZK().create("/testacl-digest", new byte[0], ACLs.digestAllAcl("xff:xff"), CreateMode.EPHEMERAL);

		// 可以后添加auth到session
		zkh.getConnectedZK().addAuthInfo("digest", "xff:xff".getBytes());
		byte[] data = zkh.getConnectedZK().getData("/testacl-digest", false, null);
		Assertions.assertThat(data).isNotNull();
	}
}

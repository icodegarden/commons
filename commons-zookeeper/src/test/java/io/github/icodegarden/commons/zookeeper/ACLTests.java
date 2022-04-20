package io.github.icodegarden.commons.zookeeper;

import java.util.ArrayList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
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
		/**
		 * zk如果在本机，还需要加入127.0.0.1
		 */
		ArrayList<ACL> acls = new ArrayList<ACL>(ACLs.IPV4S_ALL_ACL);
		acls.add(new ACL(Perms.ALL, new Id("ip", "127.0.0.1")));
		
		zkh.getConnectedZK().create("/testacl-ip", new byte[0], acls, CreateMode.EPHEMERAL);
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

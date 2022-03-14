package io.github.icodegarden.commons.lang.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class SystemUtilsTests {
	
	@Test
	void getIp() throws Exception {
		assertThat(SystemUtils.getIp()).isNotNull();
	}

	@Test
	void getIpv4s() throws Exception {
		assertThat(SystemUtils.getIpv4s()).isNotEmpty();
	}
	
	@Test
	void getIpv6s() throws Exception {
		assertThat(SystemUtils.getIpv6s()).isNotNull();
	}
	
	@Test
	void getIps() throws Exception {
		assertThat(SystemUtils.getIps()).isNotEmpty();
	}
}

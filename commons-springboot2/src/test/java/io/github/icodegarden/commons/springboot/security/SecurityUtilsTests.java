package io.github.icodegarden.commons.springboot.security;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class SecurityUtilsTests {

	@Test
	void spring() throws Exception {
		SpringUser user = new SpringUser("id", "xff", "", Collections.emptyList());
		PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(user, "",
				Collections.emptyList());

		SpringAuthentication springAuthentication = new SpringAuthentication(authentication);

		SecurityUtils.setAuthentication(springAuthentication);

		Assertions.assertThat(SecurityUtils.getAuthentication()).isNotNull();
		Assertions.assertThat(SecurityUtils.getAuthenticatedUser()).isNotNull();
		Assertions.assertThat(SecurityUtils.getAuthenticatedUser().getUserId()).isEqualTo("id");
		Assertions.assertThat(SecurityUtils.getAuthenticatedUser().getUsername()).isEqualTo("xff");
	}

	@Test
	void simple() throws Exception {
		SecurityUtils.configAuthenticationContainer(new SimpleAuthenticationContainer());
		
		SimpleUser user = new SimpleUser("id", "xff", "", Collections.emptyList());
		SimpleAuthentication simpleAuthentication = new SimpleAuthentication(user, Collections.emptyList());

		SecurityUtils.setAuthentication(simpleAuthentication);

		Assertions.assertThat(SecurityUtils.getAuthentication()).isNotNull();
		Assertions.assertThat(SecurityUtils.getAuthenticatedUser()).isNotNull();
		Assertions.assertThat(SecurityUtils.getAuthenticatedUser().getUserId()).isEqualTo("id");
		Assertions.assertThat(SecurityUtils.getAuthenticatedUser().getUsername()).isEqualTo("xff");
	}
}

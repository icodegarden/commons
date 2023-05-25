package io.github.icodegarden.commons.springboot.security;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class SpringSecurityUtilsTests {

	@Test
	void all() throws Exception {
		User user = new org.springframework.security.core.userdetails.User("xff", "", Collections.emptyList());
		PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(user, "",
				Collections.emptyList());
		SpringSecurityUtils.setAuthentication(authentication);

		Assertions.assertThat(SpringSecurityUtils.getAuthentication()).isNotNull();
		Assertions.assertThat(SpringSecurityUtils.getAuthenticatedUser()).isNotNull();
		Assertions.assertThat(SpringSecurityUtils.getAuthenticatedUser().getUsername()).isEqualTo("xff");
	}
}

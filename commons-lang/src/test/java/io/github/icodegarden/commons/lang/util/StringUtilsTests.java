package io.github.icodegarden.commons.lang.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class StringUtilsTests {

	@Test
	void camelToLower() throws Exception {
		String s = StringUtils.camelToLower("userName");
		assertThat(s).isEqualTo("username");
		
		s = StringUtils.camelToLower("userName", "_");
		assertThat(s).isEqualTo("user_name");
		
		s = StringUtils.camelToLower("userName", "");
		assertThat(s).isEqualTo("username");
	}
}

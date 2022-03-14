package io.github.icodegarden.commons.lang.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class CronUtilsTests {

	@Test
	void betweenMillis() {
		assertThat(CronUtils.betweenMillis("* * * * * *")).isEqualTo(1000);
		assertThat(CronUtils.betweenMillis("1/2 * * * * *")).isEqualTo(2000);
		assertThat(CronUtils.betweenMillis("0 * * * * *")).isEqualTo(60000);
	}

	@Test
	void isValid() {
		assertThat(CronUtils.isValid("* * * * * *")).isTrue();
		assertThat(CronUtils.isValid("1/2 * * * * *")).isTrue();
		assertThat(CronUtils.isValid("* * * * *")).isFalse();
	}

	@Test
	void next() {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		assertThat(CronUtils.next("* * * * * *")).isEqualTo(now.plusSeconds(1).withNano(0));
	}

	@Test
	void nextDelayMillis() {
		assertThat(CronUtils.nextDelayMillis("* * * * * *")).isLessThanOrEqualTo(1000).isGreaterThanOrEqualTo(0);
	}
}

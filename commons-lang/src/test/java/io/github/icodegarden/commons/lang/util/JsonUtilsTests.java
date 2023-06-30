package io.github.icodegarden.commons.lang.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class JsonUtilsTests {

	@Test
	void deserializeMap() throws Exception {
		Map<String, ReferType> types = new HashMap<>();

		ReferType referType = new ReferType("id1", 123L, System.currentTimeMillis());
		types.put("name1", referType);

		String s = JsonUtils.serialize(types);

		Map<String, ReferType> map = JsonUtils.deserializeMap(s, String.class, ReferType.class);
		assertThat(map.get("name1").getRelationId()).isNotEmpty();
	}

	@Getter
	@Setter
	@ToString
	public static class ReferType {

		private String relationId;
		private Long compare;
		private long ts;

		public ReferType() {
		}

		public ReferType(String relationId, Long compare, long ts) {
			this.relationId = relationId;
			this.compare = compare;
			this.ts = ts;
		}

	}
}

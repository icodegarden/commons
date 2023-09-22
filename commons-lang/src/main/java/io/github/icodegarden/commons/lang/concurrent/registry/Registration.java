/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.icodegarden.commons.lang.concurrent.registry;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.icodegarden.commons.lang.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Registration {

	/**
	 * @return 名称 例如serviceName
	 */
	String getName();

	/**
	 * @return 唯一标识id 例如instanceId、ip:port
	 */
	String getIdentifier();

	/**
	 * @return 租期过期时间
	 */
	default long getExpireSeconds() {
		return 30L;
	}

	JsonNode getMetadata();

	JsonNode getInfo();

	@Getter
	@Setter
	@ToString
	class Default implements Registration {
		private String name;
		private String identifier;
		private long expireSeconds;
		private JsonNode metadata;
		private JsonNode info;
		
		/**
		 * mybatis result用
		 */
		public Default() {
			
		}

		public Default(String name, String identifier, long expireSeconds, String metadata, String info) {
			super();
			this.name = name;
			this.identifier = identifier;
			this.expireSeconds = expireSeconds;
			this.metadata = metadata != null ? JsonUtils.deserialize(metadata, JsonNode.class) : null;
			this.info = info != null ? JsonUtils.deserialize(info, JsonNode.class) : null;
		}
	}
}

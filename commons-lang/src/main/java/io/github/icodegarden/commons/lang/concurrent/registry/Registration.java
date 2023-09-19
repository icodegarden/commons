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
	default Long getExpireSeconds() {
		return 30L;
	}

	JsonNode getMetadata();

	JsonNode getInfo();

}

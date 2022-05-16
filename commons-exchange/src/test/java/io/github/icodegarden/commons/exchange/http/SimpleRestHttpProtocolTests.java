package io.github.icodegarden.commons.exchange.http;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import io.github.icodegarden.commons.exchange.ProtocolParams;
import io.github.icodegarden.commons.exchange.http.RestRequestTemplateTests.UserDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class SimpleRestHttpProtocolTests {

	@Test
	void exchange_responseClass() {
		SimpleRestHttpProtocol protocol = new SimpleRestHttpProtocol("/api/v1/users/1", HttpMethod.GET, UserDTO.class);
		ProtocolParams params = new ProtocolParams(null, "localhost", 18080, null, 3000);
		HttpEntity<UserDTO> httpEntity = protocol.exchange(params);
		UserDTO user = httpEntity.getBody();

		Assertions.assertThat(user).isNotNull();
	}

	@Test
	void exchange_typeReference() {
		SimpleRestHttpProtocol protocol = new SimpleRestHttpProtocol("/api/v1/users", HttpMethod.GET,
				new ParameterizedTypeReference<List<UserDTO>>() {
				});
		ProtocolParams params = new ProtocolParams(null, "localhost", 18080, null, 3000);
		HttpEntity<List<UserDTO>> httpEntity = protocol.exchange(params);
		List<UserDTO> users = httpEntity.getBody();

		Assertions.assertThat(users.size()).isEqualTo(2);
	}
}

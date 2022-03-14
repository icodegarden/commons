package io.github.icodegarden.commons.exchange.http;

import java.util.List;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class RestRequestTemplateTests {

//	private HttpHeaders httpHeaders = new HttpHeaders();
//	{
//		httpHeaders.set("auth", "xxxxxxx");
//	}
	RestRequestTemplate httpRequestTemplate = new RestRequestTemplate();

	@Test
	void postForObject() {
		// 400
		try {
			httpRequestTemplate.postForObject("http://localhost:8080/api/v1/users", new UserDTO("", ""), UserDTO.class);
			throw new RuntimeException();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(HttpClientRemoteException.class);
			Assertions.assertThat(((HttpClientRemoteException) e).getHttpStatus()).isEqualTo(400);
		}

		// ok
		UserDTO userDTO = new UserDTO("xff", "123");
		UserDTO ret = httpRequestTemplate.postForObject("http://localhost:8080/api/v1/users", userDTO, UserDTO.class);
		Assertions.assertThat(ret).isEqualTo(userDTO);
	}

	@Test
	void postForEntity() {
		// 400
		try {
			httpRequestTemplate.postForEntity("http://localhost:8080/api/v1/users", new UserDTO("", ""), UserDTO.class);
			throw new RuntimeException();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(HttpClientRemoteException.class);
			Assertions.assertThat(((HttpClientRemoteException) e).getHttpStatus()).isEqualTo(400);
		}

		// ok
		UserDTO userDTO = new UserDTO("xff", "123");
		HttpEntity<UserDTO> ret = httpRequestTemplate.postForEntity("http://localhost:8080/api/v1/users", userDTO,
				UserDTO.class);
		Assertions.assertThat(ret.getBody()).isEqualTo(userDTO);
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();
	}

	@Test
	void exchange_responseClass() {
		// post 400
		try {
			HttpEntity<UserDTO> httpEntity = new HttpEntity<>(new UserDTO("", ""), null);
			httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.POST, httpEntity,
					UserDTO.class);
			throw new RuntimeException();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(HttpClientRemoteException.class);
			Assertions.assertThat(((HttpClientRemoteException) e).getHttpStatus()).isEqualTo(400);
		}

		// post ok
		UserDTO userDTO = new UserDTO("xff", "123");

		HttpEntity<UserDTO> ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.POST,
				new HttpEntity<>(userDTO, null), UserDTO.class);
		Assertions.assertThat(ret.getBody()).isEqualTo(userDTO);
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// put 400
		try {
			HttpEntity<UserDTO> httpEntity = new HttpEntity<>(new UserDTO("", ""), null);
			httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.PUT, httpEntity,
					UserDTO.class);
			throw new RuntimeException();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(HttpClientRemoteException.class);
			Assertions.assertThat(((HttpClientRemoteException) e).getHttpStatus()).isEqualTo(400);
		}

		// put ok
		ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.PUT,
				new HttpEntity<>(userDTO, null), UserDTO.class);
		Assertions.assertThat(ret.getBody()).isEqualTo(userDTO);
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// delete ok
		ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users/1", HttpMethod.DELETE, null,
				UserDTO.class);
		Assertions.assertThat(ret.getBody()).isNotNull();
		Assertions.assertThat(ret.getBody().getUsername()).isNotNull();
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// find one ok
		ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users/1", HttpMethod.GET, null, UserDTO.class);
		Assertions.assertThat(ret.getBody()).isNotNull();
		Assertions.assertThat(ret.getBody().getUsername()).isNotNull();
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// find all json deseria error
		Assertions
				.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> httpRequestTemplate
						.exchange("http://localhost:8080/api/v1/users", HttpMethod.GET, null, UserDTO.class))
				.withMessage("deserialize json error");
	}

	@Test
	void exchange_typeReference() {
		// post 400
		try {
			HttpEntity<UserDTO> httpEntity = new HttpEntity<>(new UserDTO("", ""), null);
			httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.POST, httpEntity,
					new ParameterizedTypeReference<UserDTO>() {
					});
			throw new RuntimeException();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(HttpClientRemoteException.class);
			Assertions.assertThat(((HttpClientRemoteException) e).getHttpStatus()).isEqualTo(400);
		}

		// post ok
		UserDTO userDTO = new UserDTO("xff", "123");

		HttpEntity<UserDTO> ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.POST,
				new HttpEntity<>(userDTO, null), new ParameterizedTypeReference<UserDTO>() {
				});
		Assertions.assertThat(ret.getBody()).isEqualTo(userDTO);
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// put 400
		try {
			HttpEntity<UserDTO> httpEntity = new HttpEntity<>(new UserDTO("", ""), null);
			httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.PUT, httpEntity,
					new ParameterizedTypeReference<UserDTO>() {
					});
			throw new RuntimeException();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(HttpClientRemoteException.class);
			Assertions.assertThat(((HttpClientRemoteException) e).getHttpStatus()).isEqualTo(400);
		}

		// put ok
		ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users", HttpMethod.PUT,
				new HttpEntity<>(userDTO, null), new ParameterizedTypeReference<UserDTO>() {
				});
		Assertions.assertThat(ret.getBody()).isEqualTo(userDTO);
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// delete ok
		ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users/1", HttpMethod.DELETE, null,
				new ParameterizedTypeReference<UserDTO>() {
				});
		Assertions.assertThat(ret.getBody()).isNotNull();
		Assertions.assertThat(ret.getBody().getUsername()).isNotNull();
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// find one ok
		ret = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users/1", HttpMethod.GET, null,
				new ParameterizedTypeReference<UserDTO>() {
				});
		Assertions.assertThat(ret.getBody()).isNotNull();
		Assertions.assertThat(ret.getBody().getUsername()).isNotNull();
		Assertions.assertThat(ret.getHeaders().getFirst("X-Test-Web")).isNotNull();

		// find all ok
		HttpEntity<List<UserDTO>> retList = httpRequestTemplate.exchange("http://localhost:8080/api/v1/users",
				HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDTO>>() {
				});
		Assertions.assertThat(retList.getBody()).isNotNull();
		Assertions.assertThat(retList.getBody().size()).isEqualTo(2);
		Assertions.assertThat(retList.getHeaders().getFirst("X-Test-Web")).isNotNull();
	}

	public static class UserDTO {
		private String username;
		private String password;

		public UserDTO() {

		}

		public UserDTO(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		@Override
		public int hashCode() {
			return Objects.hash(password, username);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserDTO other = (UserDTO) obj;
			return Objects.equals(password, other.password) && Objects.equals(username, other.username);
		}

	}
}

package io.github.icodegarden.commons.test.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Validated
@RestController
public class Restapi4RestRequestTemplateController {

	HttpHeaders httpHeaders = new HttpHeaders();
	{
		httpHeaders.set("X-Test-Web", "true");
	}

	@PostMapping("api/v1/users")
	public ResponseEntity<UserDTO> createUser(@Validated @RequestBody UserDTO dto) {
		return new ResponseEntity<>(dto, httpHeaders, HttpStatus.OK);
	}

	@PutMapping("api/v1/users")
	public ResponseEntity<UserDTO> updateUser(@Validated @RequestBody UserDTO dto) {
		return new ResponseEntity<>(dto, httpHeaders, HttpStatus.OK);
	}

	@GetMapping("api/v1/users")
	public ResponseEntity<List<UserDTO>> pageUsers() {
		List<UserDTO> list = new ArrayList<UserDTO>();

		UserDTO u1 = new UserDTO();
		u1.setUsername("u1");
		u1.setPassword("pw1");
		list.add(u1);

		UserDTO u2 = new UserDTO();
		u2.setUsername("u2");
		u2.setPassword("pw2");
		list.add(u2);

		return new ResponseEntity<List<UserDTO>>(list, httpHeaders, HttpStatus.OK);
	}

	@GetMapping("api/v1/users/{id}")
	public ResponseEntity<UserDTO> findUser(@PathVariable Long id) {
		UserDTO u1 = new UserDTO();
		u1.setUsername("u1");
		u1.setPassword("pw1");
		return new ResponseEntity<>(u1, httpHeaders, HttpStatus.OK);
	}

	@DeleteMapping("api/v1/users/{id}")
	public ResponseEntity<UserDTO> deleteUser(@PathVariable Long id) {
		UserDTO u1 = new UserDTO();
		u1.setUsername("u1");
		u1.setPassword("pw1");

		return new ResponseEntity<>(u1, httpHeaders, HttpStatus.OK);
	}

	@PostMapping("api/v1/users/500")
	public ResponseEntity<UserDTO> createUser500(@Validated @RequestBody UserDTO dto) {
		throw new RuntimeException("ex");
	}

	@Data
	public static class UserDTO {
		@NotEmpty
		private String username;
		@NotEmpty
		private String password;

		private String emptyStr;
	}

}

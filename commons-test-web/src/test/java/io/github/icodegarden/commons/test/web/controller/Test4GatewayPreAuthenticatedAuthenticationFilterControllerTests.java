package io.github.icodegarden.commons.test.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@WebMvcTest(MallOpenapiController.class)

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class Test4GatewayPreAuthenticatedAuthenticationFilterControllerTests {
	public static final String HEADER_APPID = "X-Auth-AppId";
	public static final String HEADER_APPNAME = "X-Auth-Appname";
	public static final String HEADER_USERID = "X-Auth-UserId";
	public static final String HEADER_USERNAME = "X-Auth-Username";

	@Autowired
	private MockMvc mvc;

	@BeforeEach
	void init() {
		Assert.isNull(SecurityUtils.getAuthentication(), "Authentication not null");
	}

	@Test
	public void test4userAuth() throws Exception {
		mvc.perform(get("/api/v1/test4userAuth")
				.header(HEADER_USERID, "userId1").header(HEADER_USERNAME, "username1")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	public void test4appAuth() throws Exception {
		mvc.perform(get("/openapi/v1/test4appAuth")
				.header(HEADER_APPID, "appId1").header(HEADER_APPNAME, "appname1")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());

	}
}
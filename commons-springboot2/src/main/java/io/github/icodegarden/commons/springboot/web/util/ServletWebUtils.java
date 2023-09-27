package io.github.icodegarden.commons.springboot.web.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.tuple.Tuple2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ServletWebUtils extends BaseWebUtils {
	private ServletWebUtils() {
	}

	public static HttpServletRequest getRequest() {
		return RequestContextHolder.getRequestAttributes() == null ? null
				: ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

//	@Deprecated//getted null
//	public static HttpServletResponse getResponse() {
//		return RequestContextHolder.getRequestAttributes() == null ? null
//				: ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
//	}

	public static List<String> getHeaderNames() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return Collections.emptyList();
		}

		List<String> result = new LinkedList<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			result.add(headerName);
		}
		return result;
	}

	public static String getHeader(String name) {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}

		return request.getHeader(name);
	}

	public static String getJWT() {
		String bearerToken = getAuthorizationToken();
		if (bearerToken != null) {
			return resolveBearerToken(bearerToken, " ");
		}
		return null;
	}

	public static void setJWT(String jwt) {
		HttpServletRequest request = getRequest();
		if (request != null) {
			String bearerToken = createBearerToken(jwt, " ");
			request.setAttribute(HEADER_AUTHORIZATION, bearerToken);// use for rpc
		}
	}

	public static String getBasicAuthorizationToken() {
		String basicToken = getAuthorizationToken();
		if (basicToken != null) {
			return resolveBasicToken(basicToken, " ");
		}
		return null;
	}

	public static String getAuthorizationToken() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		String authorizationToken = (String) request.getAttribute(HEADER_AUTHORIZATION);
		return authorizationToken != null ? authorizationToken : request.getHeader(HEADER_AUTHORIZATION);
	}

	public static boolean isApiRpc() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return false;
		}

		String header = request.getHeader(HEADER_API_REQUEST);
		return header != null && Boolean.valueOf(header);
	}

	public static boolean isOpenapiRpc() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return false;
		}

		String header = request.getHeader(HEADER_OPENAPI_REQUEST);
		return header != null && Boolean.valueOf(header);
	}

	public static boolean isInternalRpc() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return false;
		}

		String header = request.getHeader(HEADER_INTERNAL_RPC);
		return header != null && Boolean.valueOf(header);
	}

	public static String getRequestId() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}

		return request.getHeader(HEADER_REQUEST_ID);
	}

	public static void responseJWT(String jwt, HttpServletResponse response) {
		String bearerToken = createBearerToken(jwt, " ");
		response.setHeader(HEADER_AUTHORIZATION, bearerToken);
	}

	public static void responseWrite(int status, String body, HttpServletResponse response) throws IOException {
		Assert.hasText(body, "body must not empty");
		responseWrite(status, null, body, response);
	}

	public static void responseWrite(int status, List<Tuple2<String, List<String>>> headers,
			HttpServletResponse response) throws IOException {
		Assert.notEmpty(headers, "headers must not empty");
		responseWrite(status, headers, null, response);
	}

	public static void responseWrite(int status, @Nullable List<Tuple2<String, List<String>>> headers,
			@Nullable String body, HttpServletResponse response) throws IOException {
		response.setStatus(status);
		if (headers != null && !headers.isEmpty()) {
			for (Tuple2<String, List<String>> header : headers) {
				for (String value : header.getT2()) {
					response.addHeader(header.getT1(), value);
				}
			}
		}

		if (body == null) {
			body = "";
		}
//		response.setContentType("application/json;charset=utf-8");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");
		response.getWriter().println(body);
	}
}

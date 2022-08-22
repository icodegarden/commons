package io.github.icodegarden.commons.springboot.web.util;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.query.BaseQuery;
import io.github.icodegarden.commons.lang.query.NextQuerySupportList;
import io.github.icodegarden.commons.lang.tuple.Tuple2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class WebUtils {
	private WebUtils() {
	}

	public final static String AUTHORIZATION_HEADER = "Authorization";
	/**
	 * 总页数
	 */
	public static final String HTTPHEADER_TOTALPAGES = "X-Total-Pages";
	/**
	 * 总条数
	 */
	public static final String HTTPHEADER_TOTALCOUNT = "X-Total-Count";
	/**
	 * 下一页搜索的searchAfter
	 */
	public static final String HTTPHEADER_SEARCHAFTER = "X-Search-After";
	/**
	 * 消息描述
	 */
	public static final String HTTPHEADER_MESSAGE = "X-Message";
	/**
	 * 是否内部服务间调用的标记
	 */
	public static final String HTTPHEADER_INTERNAL_RPC = "X-Internal-Rpc";

	public static HttpHeaders pageHeaders(int totalPages, long totalCount) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HTTPHEADER_TOTALPAGES,
				(totalPages <= BaseQuery.MAX_TOTAL_PAGES ? totalPages : BaseQuery.MAX_TOTAL_PAGES) + "");
		httpHeaders.add(HTTPHEADER_TOTALCOUNT, totalCount + "");
		return httpHeaders;
	}

	public static <E> HttpHeaders pageHeaders(int totalPages, long totalCount,
			NextQuerySupportList<E> nextQuerySupportList) {
		HttpHeaders httpHeaders = pageHeaders(totalPages, totalCount);
		if (nextQuerySupportList.getSearchAfter() != null) {
			httpHeaders.add(HTTPHEADER_SEARCHAFTER, nextQuerySupportList.getSearchAfter());
		}
		return httpHeaders;
	}

	public static int getTotalPages(ResponseEntity<?> re) {
		String first = re.getHeaders().getFirst(HTTPHEADER_TOTALPAGES);
		if (first == null) {
			return 0;
		}
		return Integer.parseInt(first);
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
			request.setAttribute(AUTHORIZATION_HEADER, bearerToken);// use for rpc
		}
	}

	private static String createBearerToken(String originToken, @Nullable String concat) {
		if (concat == null) {
			concat = " ";
		}
		return "Bearer" + concat + originToken;
	}

	public static String resolveBearerToken(String bearerToken, @Nullable String concat) {
		if (concat == null) {
			concat = " ";
		}
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer" + concat)) {
			String originToken = bearerToken.substring(7, bearerToken.length());
			return originToken;
		}
		return null;
	}

	public static String getBasicAuthorizationToken() {
		String basicToken = getAuthorizationToken();
		if (basicToken != null) {
			return resolveBasicToken(basicToken, " ");
		}
		return null;
	}

	private static String resolveBasicToken(String basicToken, @Nullable String concat) {
		if (concat == null) {
			concat = " ";
		}
		if (StringUtils.hasText(basicToken) && basicToken.startsWith("Basic" + concat)) {
			String originToken = basicToken.substring(6, basicToken.length());
			return originToken;
		}
		return null;
	}

	public static String getAuthorizationToken() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		String authorizationToken = (String) request.getAttribute(AUTHORIZATION_HEADER);
		return authorizationToken != null ? authorizationToken : request.getHeader(AUTHORIZATION_HEADER);
	}

	public static boolean isInternalRpc() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return false;
		}

		String header = request.getHeader(HTTPHEADER_INTERNAL_RPC);
		return header != null && Boolean.valueOf(header);
	}

	public static void responseJWT(String jwt, HttpServletResponse response) {
		String bearerToken = createBearerToken(jwt, " ");
		response.setHeader(AUTHORIZATION_HEADER, bearerToken);
	}

//	public static void responseWrite(int status, String body, HttpServletResponse response) throws IOException {
//		response.setStatus(status);
//		response.setContentType("application/json;charset=utf-8");
//		response.getWriter().println(body);
//	}

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
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().println(body);
	}
}

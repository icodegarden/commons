package io.github.icodegarden.commons.springboot.web.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.query.BaseQuery;
import io.github.icodegarden.commons.lang.query.NextQuerySupportList;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class BaseWebUtils {
	
	public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

	public final static String HEADER_AUTHORIZATION = "Authorization";
	
	public static final String HEADER_APPID = "X-Auth-AppId";
	public static final String HEADER_APPKEY = "X-Auth-AppKey";
	public static final String HEADER_APPNAME = "X-Auth-Appname";
	
	public static final String HEADER_USERID = "X-Auth-UserId";
	public static final String HEADER_USERNAME = "X-Auth-Username";
	/**
	 * 请求id，多用于openapi
	 */
	public static final String HEADER_REQUEST_ID = "X-Request-Id";
	
	/**
	 * 总页数
	 */
	public static final String HEADER_TOTALPAGES = "X-Total-Pages";
	/**
	 * 总条数
	 */
	public static final String HEADER_TOTALCOUNT = "X-Total-Count";
	/**
	 * 下一页搜索的searchAfter
	 */
	public static final String HEADER_SEARCHAFTER = "X-Search-After";
	/**
	 * 消息描述
	 */
	public static final String HEADER_MESSAGE = "X-Message";
	/**
	 * 是否内部服务间调用的标记（网关->服务不属于）
	 */
	public static final String HEADER_INTERNAL_RPC = "X-Internal-Rpc";
	/**
	 * 是否openapi调用的标记（openapi网关->服务）
	 */
	public static final String HEADER_OPENAPI_REQUEST = "X-Openapi-Request";
	/**
	 * 是否api调用的标记（api网关->服务）
	 */
	public static final String HEADER_API_REQUEST = "X-Api-Request";

	public static HttpHeaders pageHeaders(int totalPages, long totalCount) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HEADER_TOTALPAGES,
				(totalPages <= BaseQuery.MAX_TOTAL_PAGES ? totalPages : BaseQuery.MAX_TOTAL_PAGES) + "");
		httpHeaders.add(HEADER_TOTALCOUNT, totalCount + "");
		return httpHeaders;
	}

	public static <E> HttpHeaders pageHeaders(int totalPages, long totalCount,
			NextQuerySupportList<E> nextQuerySupportList) {
		HttpHeaders httpHeaders = pageHeaders(totalPages, totalCount);
		if (nextQuerySupportList.getSearchAfter() != null) {
			httpHeaders.add(HEADER_SEARCHAFTER, nextQuerySupportList.getSearchAfter());
		}
		return httpHeaders;
	}

	public static int getTotalPages(ResponseEntity<?> re) {
		String first = re.getHeaders().getFirst(HEADER_TOTALPAGES);
		if (first == null) {
			return 0;
		}
		return Integer.parseInt(first);
	}

	static String createBearerToken(String originToken, @Nullable String concat) {
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

	public static String resolveBasicToken(String basicToken, @Nullable String concat) {
		if (concat == null) {
			concat = " ";
		}
		if (StringUtils.hasText(basicToken) && basicToken.startsWith("Basic" + concat)) {
			String originToken = basicToken.substring(6, basicToken.length());
			return originToken;
		}
		return null;
	}

}

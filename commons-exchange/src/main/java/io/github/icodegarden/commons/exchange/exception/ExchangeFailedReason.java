package io.github.icodegarden.commons.exchange.exception;

import java.io.Serializable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExchangeFailedReason implements Serializable {
	private static final long serialVersionUID = 1248382809990979544L;

	public static final String KEYWORD_CLINET_CONNECT_FAILED = "ClientConnectFailed";
	public static final String KEYWORD_REQUEST_TIMEOUT = "RequestTimeout";
	public static final String KEYWORD_CLIENT_INVALID = "ClientInvalid";
	public static final String KEYWORD_CLIENT_EXCEPTION = "ClientException";
	public static final String KEYWORD_SERVER_REJECTED = "ServerRejected";
	public static final String KEYWORD_SERVER_EXCEPTION = "ServerException";
	
	/**
	 * 服务端主动返回处理逻辑不成功，属于交互成功
	 */
	
	private String keyword;
	private String desc;

	/**
	 * 用于Kryo序列化需要无参构造
	 */
	private ExchangeFailedReason() {
		
	}
	
	private ExchangeFailedReason(String keyword, String desc) {
		this.keyword = keyword;
		this.desc = desc;
		if(desc != null && desc.length() > 200) {
			this.desc = new StringBuilder(203).append(desc.substring(0, 200)).append("...").toString();
		}
	}

	public static ExchangeFailedReason clientConnectFailed(String desc, Exception e) {
		if (desc == null || desc.isEmpty()) {
			desc = e != null ? e.getClass().getName() : "";
		}
		return new ExchangeFailedReason(KEYWORD_CLINET_CONNECT_FAILED, desc);
	}

	public static ExchangeFailedReason requestTimeout(String desc, Exception e) {
		if (desc == null || desc.isEmpty()) {
			desc = e != null ? e.getClass().getName() : "";
		}
		return new ExchangeFailedReason(KEYWORD_REQUEST_TIMEOUT, desc);
	}

	public static ExchangeFailedReason clientException(String desc, Exception e) {
		if (desc == null || desc.isEmpty()) {
			desc = e != null ? e.getClass().getName() : "";
		}
		return new ExchangeFailedReason(KEYWORD_CLIENT_EXCEPTION, desc);
	}
	
	public static ExchangeFailedReason clientInvalid(String desc, Exception e) {
		if (desc == null || desc.isEmpty()) {
			desc = e != null ? e.getClass().getName() : "";
		}
		return new ExchangeFailedReason(KEYWORD_CLIENT_INVALID, desc);
	}
	
	public static ExchangeFailedReason serverRejected(String desc, Exception e) {
		if (desc == null || desc.isEmpty()) {
			desc = e != null ? e.getClass().getName() : "";
		}
		return new ExchangeFailedReason(KEYWORD_SERVER_REJECTED, desc);
	}

	public static ExchangeFailedReason serverException(String desc, Exception e) {
		if (desc == null || desc.isEmpty()) {
			desc = e != null ? e.getClass().getName() : "";
		}
		return new ExchangeFailedReason(KEYWORD_SERVER_EXCEPTION, desc);
	}

	public String getKeyword() {
		return keyword;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		return " [keyword=" + keyword + ", desc=" + desc + "]";
	}
}
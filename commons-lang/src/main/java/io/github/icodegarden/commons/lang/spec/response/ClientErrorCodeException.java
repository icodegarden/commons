package io.github.icodegarden.commons.lang.spec.response;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ClientErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	public ClientErrorCodeException(String code, String msg, String sub_code, String sub_msg) {
		super(code, msg, sub_code, sub_msg);
	}
	
}

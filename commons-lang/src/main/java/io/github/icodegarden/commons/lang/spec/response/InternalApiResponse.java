package io.github.icodegarden.commons.lang.spec.response;

import java.util.Objects;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class InternalApiResponse<R> extends ApiResponse {

	private R biz_content;

	public static <R> InternalApiResponse<R> success(R biz_content) {
		InternalApiResponse<R> internalApiResponse = new InternalApiResponse<R>();
		internalApiResponse.setCode(ApiResponse.CODE_SUCCESS);
		internalApiResponse.setMsg(ApiResponse.MSG_SUCCESS);
		internalApiResponse.setBiz_content(biz_content);
		return internalApiResponse;
	}

	public static <R> InternalApiResponse<R> fail(ErrorCodeException e) {
		InternalApiResponse<R> internalApiResponse = new InternalApiResponse<R>();
		internalApiResponse.setCode(e.getCode());
		internalApiResponse.setMsg(e.getMsg());
		internalApiResponse.setSub_code(e.getSub_code());
		internalApiResponse.setSub_msg(e.getSub_msg());
		return internalApiResponse;
	}

	public R getBiz_content() {
		return biz_content;
	}

	public void setBiz_content(R biz_content) {
		this.biz_content = biz_content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(biz_content);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InternalApiResponse other = (InternalApiResponse) obj;
		return Objects.equals(biz_content, other.biz_content);
	}

}

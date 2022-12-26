package io.github.icodegarden.commons.lang.dao;

import java.util.ArrayList;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class OptimizeTableResults<E extends OptimizeTableResults.Result> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;

	public boolean isErrorInMysql() {
		for (OptimizeTableResults.Result result : this) {
			if ("status".equalsIgnoreCase(result.getMsg_type())) {
				if ("OK".equalsIgnoreCase(result.getMsg_text())) {
					return false;
				}
			}
		}
		return true;
	}

	public String getDesc() {
		String desc = this.stream().map(one -> one.getMsg_text()).collect(Collectors.joining(","));
		return desc;
	}

	/**
	 * 源于mysql字段 
	 */
	@Getter
	@Setter
	@ToString
	public static class Result {

		private String Table;
		private String Op;
		private String Msg_type;
		private String Msg_text;
	}
}

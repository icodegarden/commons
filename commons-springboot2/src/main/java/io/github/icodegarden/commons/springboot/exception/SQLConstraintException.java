package io.github.icodegarden.commons.springboot.exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

/**
 * sql数据完整约束异常，包括唯一约束、非空约束...
 * 
 * @author Fangfang.Xu
 *
 */
public class SQLConstraintException extends IllegalArgumentException {
	private static final long serialVersionUID = -5685277510936987458L;

	private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.*)' for key");

	private String message;

	public SQLConstraintException(DataIntegrityViolationException e) {
		super(e);
		if (e instanceof DuplicateKeyException) {
			caseDuplicateKey((DuplicateKeyException) e);
		} else {
			/**
			 * 非空约束
			 * 
			 * @param e
			 */
			this.message = e.getMessage();
		}
	}

	/**
	 * 唯一约束
	 * 
	 * @param e
	 */
	private void caseDuplicateKey(DuplicateKeyException e) {
		Throwable cause = e.getCause();
		if (cause != null && (cause instanceof SQLIntegrityConstraintViolationException)) {
			resolveDuplicate((SQLIntegrityConstraintViolationException) cause);
		} else {
			this.message = "Duplicate can not resolve, " + e.getMessage();
		}
	}

	/**
	 * 解析原生sql异常
	 * 
	 * @param e
	 */
	private void resolveDuplicate(SQLIntegrityConstraintViolationException e) {
		Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(e.getMessage());
		if (matcher.find()) {
			try {
				String value = matcher.group(1);
				this.message = String.format("Duplicate.Exists:%s", value);
			} catch (Exception e1) {
//				this.message = "resolve error";
				this.message = e.getMessage();
			}
		} else {
//			this.message = "resolve not found";
			this.message = e.getMessage();
		}
	}

	@Override
	public String getMessage() {
		return message;
	}
}

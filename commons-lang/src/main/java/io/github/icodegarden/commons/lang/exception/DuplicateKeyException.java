package io.github.icodegarden.commons.lang.exception;

import java.sql.SQLIntegrityConstraintViolationException;

import io.github.icodegarden.commons.lang.util.ExceptionUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DuplicateKeyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateKeyException(String message) {
		super(message);
	}

	public static void throwIfCompatible(Exception e) throws DuplicateKeyException {
		SQLIntegrityConstraintViolationException cause = ExceptionUtils.causeOf(e,
				SQLIntegrityConstraintViolationException.class);
		if (cause != null && cause.getMessage().contains("Duplicate")) {
			throw new DuplicateKeyException("Duplicate key", e);
		}
	}

}

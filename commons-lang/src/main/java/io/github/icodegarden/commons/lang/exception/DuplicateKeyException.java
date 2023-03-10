package io.github.icodegarden.commons.lang.exception;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

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

	public static void throwIfCompatible(SQLException e) throws DuplicateKeyException {
		if (e instanceof SQLIntegrityConstraintViolationException && e.getMessage().contains("Duplicate")) {
			throw new DuplicateKeyException("Duplicate key", e);
		}
	}

}

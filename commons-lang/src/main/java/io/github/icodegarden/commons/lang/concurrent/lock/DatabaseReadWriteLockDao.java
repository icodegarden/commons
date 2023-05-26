package io.github.icodegarden.commons.lang.concurrent.lock;

import java.util.List;

import io.github.icodegarden.commons.lang.annotation.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DatabaseReadWriteLockDao {

	public static final String TABLE_NAME = "distributed_read_write_lock";

	void createRow(String lockName, String identifier, Long expireSeconds, String lockAt, boolean readType);

	@NotNull
	List<LockDO> listLocks(String lockName, String nowStr);

	int deleteRow(String lockName, String identifier);

	@Getter
	@Setter
	@ToString
	@AllArgsConstructor
	class LockDO {
		private String identifier;
		private boolean readType;
	}
}

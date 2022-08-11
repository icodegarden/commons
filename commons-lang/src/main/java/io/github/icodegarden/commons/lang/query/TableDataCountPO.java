package io.github.icodegarden.commons.lang.query;

import java.time.LocalDateTime;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class TableDataCountPO {

	/**
	 * String 对非关系数据库友好
	 */
	private String id;
	private String tableName;
	private Long dataCount;
	private LocalDateTime updatedAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Long getDataCount() {
		return dataCount;
	}

	public void setDataCount(Long dataCount) {
		this.dataCount = dataCount;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "TableDataCountPO [id=" + id + ", tableName=" + tableName + ", dataCount=" + dataCount + ", updatedAt="
				+ updatedAt + "]";
	}

}

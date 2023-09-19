package io.github.icodegarden.commons.mybatis.repository;

import io.github.icodegarden.commons.lang.query.BaseQuery;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class DemoQuery extends BaseQuery {

	private String nameLike;// varchar(30) NOT NULL,
	private Boolean actived;// bit NOT NULL default 0,

	private With with;

	@Builder
	public DemoQuery(int page, int size, String orderBy, String nameLike, Boolean actived,
			With with) {
		super(page, size, orderBy);
		this.nameLike = nameLike;
		this.actived = actived;
		this.with = with;
	}

	@Builder
	@Data
	public static class With {
		private boolean createdBy;// varchar(80) NOT NULL COMMENT '商户系统app_id',
		private boolean createdAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
		private boolean updatedBy;// varchar(80) NOT NULL,
		private boolean updatedAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

	}
}
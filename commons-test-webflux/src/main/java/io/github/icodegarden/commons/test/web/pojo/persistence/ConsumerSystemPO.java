package io.github.icodegarden.commons.test.web.pojo.persistence;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NonNull;
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
public class ConsumerSystemPO {

	private Long id;
	private String name;
	private String email;
	private String saslUsername;
	private String saslPassword;
	private String appId;
	private Boolean actived;

	@Setter
	@Getter
	@ToString
	public static class Update {
		@NonNull
		private Long id;// bigint NOT NULL AUTO_INCREMENT,
		private String name;// varchar(30) NOT NULL,
		private String saslUsername;
		private String saslPassword;
		private Boolean actived;// bit NOT NULL default 1,
		private String updatedBy;// varchar(80) NOT NULL,
		private LocalDateTime updatedAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	}
}
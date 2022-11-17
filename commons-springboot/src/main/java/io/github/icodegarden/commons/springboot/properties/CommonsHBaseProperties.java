package io.github.icodegarden.commons.springboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.commons.hbase.HBaseEnv;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.hbase")
@Getter
@Setter
@ToString(callSuper = true)
public class CommonsHBaseProperties {

	@NonNull
	private HBaseEnv.VersionFrom versionFrom;
	@NonNull
	private String hbaseZookeeperQuorum;

	private String hbaseClientUsername;//例如 root

	private String hbaseClientPassword;//例如 root
	/**
	 * 表名前缀
	 */
	private String namePrefix = "";

}
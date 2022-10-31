package io.github.icodegarden.commons.hbase.query;

import java.util.Properties;

import io.github.icodegarden.commons.hbase.HBaseEnv;
import io.github.icodegarden.commons.hbase.HBaseEnv.VersionFrom;
import lombok.Getter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
public class HBaseEnvFactory {

	public static HBaseEnv devLocal() {
		Properties properties = new Properties();
		properties.setProperty("hbase.zookeeper.quorum", "127.0.0.1:2181");
//		properties.setProperty("hbase.client.username", "root");
//		properties.setProperty("hbase.client.password", "root");

		HBaseEnv hBaseEnv = new HBaseEnv(VersionFrom.Native, properties);
		hBaseEnv.setNamePrefix("dev_");
		return hBaseEnv;
	}

	public static HBaseEnv devAliyun() {
		Properties properties = new Properties();
		properties.setProperty("hbase.zookeeper.quorum",
				"ld-bp129w63278nrhys9-proxy-hbaseue.hbaseue.rds.aliyuncs.com:30020");
		properties.setProperty("hbase.client.username", "root");
		properties.setProperty("hbase.client.password", "root");

		HBaseEnv hBaseEnv = new HBaseEnv(VersionFrom.AliyunLindorm, properties);
		hBaseEnv.setNamePrefix("dev_");
		return hBaseEnv;
	}

	public static HBaseEnv testAliyun() {
		Properties properties = new Properties();
		properties.setProperty("hbase.zookeeper.quorum",
				"ld-bp129w63278nrhys9-proxy-hbaseue.hbaseue.rds.aliyuncs.com:30020");
		properties.setProperty("hbase.client.username", "root");
		properties.setProperty("hbase.client.password", "root");

		HBaseEnv hBaseEnv = new HBaseEnv(VersionFrom.AliyunLindorm, properties);
		hBaseEnv.setNamePrefix("test_");
		return hBaseEnv;
	}
}

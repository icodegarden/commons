package io.github.icodegarden.commons.hbase;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Getter
public class HBaseEnv implements Closeable {

	public static enum VersionFrom {
		Native/* hbase-2.4.12 */, AliyunLindorm/* HBase增强版(Lindorm) 主版本2.0 小版本2.3.1（该版本已经是最新版本） */
	}

	private final VersionFrom versionFrom;

	private String namePrefix = "";

	private final Admin admin;

	// 创建 HBase连接，在程序生命周期内只需创建一次，该连接线程安全，可以共享给所有线程使用。
	// 在程序结束后，需要将Connection对象关闭，否则会造成连接泄露。
	// 也可以采用try finally方式防止泄露
	private final Connection connection;

	private final AggregationClient aggregationClient;

	public HBaseEnv(VersionFrom versionFrom, Properties config) {
		log.info("init HBase Env with config:{}", config);
		try {
			this.versionFrom = versionFrom;

//			// 新建一个Configuration
//			Configuration conf = HBaseConfiguration.create();
//			// 集群的连接地址(VPC内网地址)在控制台页面的数据库连接界面获得
//			conf.set("hbase.zookeeper.quorum", "ld-bp129w63278nrhys9-proxy-hbaseue.hbaseue.rds.aliyuncs.com:30020");
//			// xml_template.comment.hbaseue.username_password.default
//			conf.set("hbase.client.username", "root");
//			conf.set("hbase.client.password", "root");
//			// 如果您直接依赖了阿里云hbase客户端，则无需配置connection.impl参数，如果您依赖了alihbase-connector，则需要配置此参数
//			//conf.set("hbase.client.connection.impl", AliHBaseUEClusterConnection.class.getName());

			Configuration conf = HBaseConfiguration.create();
//			conf.set("hbase.zookeeper.quorum", "10.200.188.40:2181");
//			conf.set("hbase.zookeeper.quorum", "dev-host:2181");
//			conf.set("hbase.zookeeper.property.clientPort", config.getClientPort());
//			conf.set("hbase.client.username", config.getUsername());
//			conf.set("hbase.client.password", config.getPassword());

//			conf.set("hbase.client.keyvalue.maxsize","20485760");//Default 10485760
			// hbase.server.keyvalue.maxsize //Default 10485760

			for (Entry<Object, Object> entry : config.entrySet()) {
				conf.set(entry.getKey().toString(), entry.getValue().toString());
			}

			connection = ConnectionFactory.createConnection(conf);
			admin = connection.getAdmin();
			aggregationClient = new AggregationClient(conf);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setNamePrefix(String namePrefix) {
		this.namePrefix = namePrefix;
	}

	@Override
	public void close() throws IOException {
		if (aggregationClient != null) {
			aggregationClient.close();
		}
		if (connection != null) {
			connection.close();
		}
		if (admin != null) {
			admin.close();
		}
	}
}

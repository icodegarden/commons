package io.github.icodegarden.commons.shardingsphere.springboot;
///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.github.icodegarden.beecomb.master.shardingsphere;
//
//import java.sql.SQLException;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//import javax.sql.DataSource;
//
//import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
//import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
//import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
//import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
//import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
//import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
//import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
//import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
//
//import com.zaxxer.hikari.HikariDataSource;
//
//public final class DemoOfShardingDatabasesConfigurationPrecise {
//
//	public static DataSource getDataSource() throws SQLException {
//		return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singleton(createShardingRuleConfiguration()), new Properties());
//
////		return ShardingSphereDataSourceFactory.createDataSource(createModeConfiguration(), createDataSourceMap(),
////				Collections.singleton(createShardingRuleConfiguration()), new Properties());
//	}
//
//	private static  ShardingRuleConfiguration createShardingRuleConfiguration() {
//		ShardingRuleConfiguration result = new ShardingRuleConfiguration();
//		result.getTables().add(getJobMainRuleConfiguration());
//		result.getTables().add(getJobDetailTableRuleConfiguration());
//		result.getTables().add(getDelayJobTableRuleConfiguration());
//		result.getTables().add(getScheduleJobTableRuleConfiguration());
////		result.getBroadcastTables().add("t_address");
////		result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));
//		Properties props = new Properties();
//		props.setProperty("algorithm-expression", "ds_${job_id % 2}");
//		result.getShardingAlgorithms().put("dsinline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
//		
////		props = new Properties();
////		props.setProperty("algorithm-expression", "job_main${id % 2}");
////		result.getShardingAlgorithms().put("maininline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
//		
//		props = new Properties();
//		props.setProperty("algorithm-expression", "ds_${job_id % 2}");
//		result.getShardingAlgorithms().put("detailinline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
//		
//		props = new Properties();
//		props.setProperty("algorithm-expression", "ds_${job_id % 2}");
//		result.getShardingAlgorithms().put("delayinline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
//		
//		props = new Properties();
//		props.setProperty("algorithm-expression", "ds_${job_id % 2}");
//		result.getShardingAlgorithms().put("scheduleinline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
//		
////		props.setProperty("strategy", "standard");
////		props.setProperty("algorithmClassName", "io.github.icodegarden.beecomb.master.RangeModShardingAlgorithm");
////		props.setProperty("groups", "[{\"name\":\"group0\",\"rangeGte\":0,\"rangeLt\":9223372036854775807,\"mod\":2,\"modLoadBalance\":{\"ds0\":[0],\"ds1\":[1]}}]");
////		result.getShardingAlgorithms().put("myrrrmmm", new ShardingSphereAlgorithmConfiguration("CLASS_BASED", props));
//		
//		result.getKeyGenerators().put("snowflake",
//				new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", new Properties()));
//		return result;
//	}
//
//	private static ModeConfiguration createModeConfiguration() {
//		return new ModeConfiguration("Standalone",
//				new StandalonePersistRepositoryConfiguration("File", new Properties()), true);
//	}
//
//	private static ShardingTableRuleConfiguration getJobMainRuleConfiguration() {
//		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_main","ds_${0..1}.job_main");
////		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_main");
//		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration("id", "dsinline");
//		result.setDatabaseShardingStrategy(databaseShardingStrategy);
////		StandardShardingStrategyConfiguration tableShardingStrategy = new StandardShardingStrategyConfiguration("id", "myrrrmmm1");
////		result.setTableShardingStrategy(tableShardingStrategy);
//		result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "snowflake"));
//		return result;
//	}
//
//	private static ShardingTableRuleConfiguration getJobDetailTableRuleConfiguration() {
//		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_detail","ds_${0..1}.job_detail");
////		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_detail");
//		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration("job_id", "detailinline");
//		result.setDatabaseShardingStrategy(databaseShardingStrategy);
//		return result;
//	}
//	
//	private static ShardingTableRuleConfiguration getDelayJobTableRuleConfiguration() {
//		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("delay_job","ds_${0..1}.delay_job");
////		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("delay_job");
//		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration("job_id", "delayinline");
//		result.setDatabaseShardingStrategy(databaseShardingStrategy);
//		return result;
//	}
//	
//	private static ShardingTableRuleConfiguration getScheduleJobTableRuleConfiguration() {
//		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("schedule_job","ds_${0..1}.schedule_job");
////		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("schedule_job");
//		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration("job_id", "scheduleinline");
//		result.setDatabaseShardingStrategy(databaseShardingStrategy);
//		return result;
//	}
//
//	private static Map<String, DataSource> createDataSourceMap() {
////		Map<String, DataSource> result = new HashMap<>(2, 1);
////		result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
////		result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
////		return result;
//
//		Map<String, DataSource> dataSourceMap = new HashMap<>();
//
//		// 配置第 1 个数据源
//		HikariDataSource dataSource1 = new HikariDataSource();
//		dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
//		dataSource1.setJdbcUrl("jdbc:mysql://172.22.122.23:53306/beecomb_0");
//		dataSource1.setUsername("root");
//		dataSource1.setPassword("123456");
//		dataSourceMap.put("ds_0", dataSource1);
//
//		// 配置第 2 个数据源
//		HikariDataSource dataSource2 = new HikariDataSource();
//		dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
//		dataSource2.setJdbcUrl("jdbc:mysql://172.22.122.23:53306/beecomb_1");
//		dataSource2.setUsername("root");
//		dataSource2.setPassword("123456");
//		dataSourceMap.put("ds_1", dataSource2);
//		
//		return dataSourceMap;
//	}
//}

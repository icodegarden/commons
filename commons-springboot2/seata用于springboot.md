#官方文档
	http://seata.io/zh-cn/docs/overview/what-is-seata.html
#github
	https://github.com/seata/seata
#使用例子
	https://github.com/seata/seata-samples

#部署server（TC）
##下载解压
	https://github.com/seata/seata/releases 下载  seata-server-1.6.1.zip 解压
##使用注册中心
	修改conf/application.yml ， 有application.example.yml可参考
	
	seata:
	  registry:
	    type: nacos
	    nacos:
	      application: seata-server
	      server-addr: 172.22.122.27:8848
	      #group: SEATA_GROUP
	      #namespace:
	      #cluster: default
	      #username:
	      #password:
	      #context-path:
	      ##if use MSE Nacos with auth, mutex with username/password attribute
	      #access-key:
	      #secret-key:	      
##使用配置中心	 
	修改conf/application.yml ， 有application.example.yml可参考
	
	seata:
	  config:
	    type: nacos
	    nacos:
	      server-addr: 172.22.122.27:8848
	      #group: SEATA_GROUP
	      #username:
	      #password:
	      #context-path:
	      ##if use MSE Nacos with auth, mutex with username/password attribute
	      #access-key:
	      #secret-key:
	      #data-id: seataServer.properties     
##配置存储
	修改conf/application.yml ， 有application.example.yml可参考
	seata:
	  store:
	    redis:
	      mode: single
	      database: 0
	      min-conn: 10
	      max-conn: 100
	      password:
	      max-total: 100
	      query-limit: 1000
	      single:
	        host: 127.0.0.1
	        port: 6379
	      sentinel:
	        master-name:
	        sentinel-hosts:
	        
	若存储类型选择db，则需要建表，语句在 https://github.com/seata/seata/blob/v1.6.1/script/server/db ，mysql的内容如下
	-- -------------------------------- The script used when storeMode is 'db' --------------------------------
	-- the table to store GlobalSession data
	CREATE TABLE IF NOT EXISTS `global_table`
	(
	    `xid`                       VARCHAR(128) NOT NULL,
	    `transaction_id`            BIGINT,
	    `status`                    TINYINT      NOT NULL,
	    `application_id`            VARCHAR(32),
	    `transaction_service_group` VARCHAR(32),
	    `transaction_name`          VARCHAR(128),
	    `timeout`                   INT,
	    `begin_time`                BIGINT,
	    `application_data`          VARCHAR(2000),
	    `gmt_create`                DATETIME,
	    `gmt_modified`              DATETIME,
	    PRIMARY KEY (`xid`),
	    KEY `idx_status_gmt_modified` (`status` , `gmt_modified`),
	    KEY `idx_transaction_id` (`transaction_id`)
	) ENGINE = InnoDB
	  DEFAULT CHARSET = utf8mb4;
	
	-- the table to store BranchSession data
	CREATE TABLE IF NOT EXISTS `branch_table`
	(
	    `branch_id`         BIGINT       NOT NULL,
	    `xid`               VARCHAR(128) NOT NULL,
	    `transaction_id`    BIGINT,
	    `resource_group_id` VARCHAR(32),
	    `resource_id`       VARCHAR(256),
	    `branch_type`       VARCHAR(8),
	    `status`            TINYINT,
	    `client_id`         VARCHAR(64),
	    `application_data`  VARCHAR(2000),
	    `gmt_create`        DATETIME(6),
	    `gmt_modified`      DATETIME(6),
	    PRIMARY KEY (`branch_id`),
	    KEY `idx_xid` (`xid`)
	) ENGINE = InnoDB
	  DEFAULT CHARSET = utf8mb4;
	
	-- the table to store lock data
	CREATE TABLE IF NOT EXISTS `lock_table`
	(
	    `row_key`        VARCHAR(128) NOT NULL,
	    `xid`            VARCHAR(128),
	    `transaction_id` BIGINT,
	    `branch_id`      BIGINT       NOT NULL,
	    `resource_id`    VARCHAR(256),
	    `table_name`     VARCHAR(32),
	    `pk`             VARCHAR(36),
	    `status`         TINYINT      NOT NULL DEFAULT '0' COMMENT '0:locked ,1:rollbacking',
	    `gmt_create`     DATETIME,
	    `gmt_modified`   DATETIME,
	    PRIMARY KEY (`row_key`),
	    KEY `idx_status` (`status`),
	    KEY `idx_branch_id` (`branch_id`),
	    KEY `idx_xid` (`xid`)
	) ENGINE = InnoDB
	  DEFAULT CHARSET = utf8mb4;
	
	CREATE TABLE IF NOT EXISTS `distributed_lock`
	(
	    `lock_key`       CHAR(20) NOT NULL,
	    `lock_value`     VARCHAR(20) NOT NULL,
	    `expire`         BIGINT,
	    primary key (`lock_key`)
	) ENGINE = InnoDB
	  DEFAULT CHARSET = utf8mb4;
	
	INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('AsyncCommitting', ' ', 0);
	INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('RetryCommitting', ' ', 0);
	INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('RetryRollbacking', ' ', 0);
	INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('TxTimeoutCheck', ' ', 0);	      
	      
##启动server	    
	linux ./bin/seata-server.sh 或 ./bin/seata-server.sh -p 8091 -h 127.0.0.1 -m file
	windows bin\seata-server.bat  		
##打开web	          
	http://localhost:7091
	seata-server会起2个端口，7091是web，8091是client rpc

##docker
	https://hub.docker.com/r/seataio/seata-server
	
	docker run --name seata-server -p 8091:8091 -p7091:7091 seataio/seata-server:1.6.1

##k8s
	先在nacos配置好seata-server的application.yml对应的配置，例如使用redis、db存储

	apiVersion: apps/v1
	kind: Deployment
	metadata:
	  name: seata-server
	  labels:
	    app: seata-server
	spec:
	  replicas: 2
	  selector:
	    matchLabels:
	      app: seata-server
	  template:
	    metadata:
	      labels:
	        app: seata-server
	    spec:
	      containers:
	        - name: seata-server
	          image: seataio/seata-server:1.6.1
	          imagePullPolicy: IfNotPresent
	          env:
	            - name: SEATA_CONFIG_NAME
	              value: file:/root/seata-config/registry
	          ports:
	            - name: rpc
	              containerPort: 8091
	              protocol: TCP
	            - name: web
	              containerPort: 7091
	              protocol: TCP	              
	          volumeMounts:
	            - name: seata-config
	              mountPath: /root/seata-config
	      volumes:
	        - name: seata-config
	          configMap:
	            name: seata-server-config
	---
	apiVersion: v1
	kind: ConfigMap
	metadata:
	  name: seata-server-config
	data:
	  registry.conf: |
	    registry {
	        type = "nacos"
	        nacos {
	          application = "seata-server"
	          serverAddr = "192.168.199.2"
	        }
	    }
	    config {
	      type = "nacos"
	      nacos {
	        serverAddr = "192.168.199.2"
	        group = "SEATA_GROUP"
	      }
	    }

#应用服务（TM/RM）
##引入依赖
		<dependency>
		    <groupId>io.seata</groupId>
		    <artifactId>seata-spring-boot-starter</artifactId>
		    <version>1.6.1</version><!--包含了seata-all -->
		</dependency>
<!-- 		<dependency> -->
<!-- 		    <groupId>com.alibaba.cloud</groupId> -->
<!-- 		    <artifactId>spring-cloud-alibaba-seata</artifactId> -->
<!-- 		    <version>2.2.0.RELEASE</version> -->
<!-- 		    <exclusions> -->
<!--                 <exclusion> -->
<!--                     <groupId>io.seata</groupId> -->
<!--                     <artifactId>seata-spring-boot-starter</artifactId> -->
<!--                 </exclusion> -->
<!--             </exclusions> -->
<!-- 		</dependency> -->
	不要引入以上版本的spring-cloud-alibaba-seata，因为这个版本不支持springboot2.5.5，他还在使用ribbon导致类找不到无法启动
	重写spring-cloud-alibaba-seata的传递xid代码，包括Feign/RestTemplate出去、HandlerInterceptor进来

##AT建表
	若代码中有使用AT模式，则需要建表，语句在 https://github.com/seata/seata/blob/v1.6.1/script/client/at/db ，mysql的内容如下
	
	-- for AT mode you must to init this sql for you business database. the seata server not need it.
	CREATE TABLE IF NOT EXISTS `undo_log`
	(
	    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
	    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
	    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
	    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
	    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
	    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
	    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
	    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
	) ENGINE = InnoDB
	  AUTO_INCREMENT = 1
	  DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
	  
##TCC建表
	若代码中有使用TCC模式的useTCCFence=true，则需要建表，语句在 https://github.com/seata/seata/blob/v1.6.1/script/client/tcc/db ，mysql的内容如下
	
	-- -------------------------------- The script use tcc fence  --------------------------------
	CREATE TABLE IF NOT EXISTS `tcc_fence_log`
	(
	    `xid`           VARCHAR(128)  NOT NULL COMMENT 'global id',
	    `branch_id`     BIGINT        NOT NULL COMMENT 'branch id',
	    `action_name`   VARCHAR(64)   NOT NULL COMMENT 'action name',
	    `status`        TINYINT       NOT NULL COMMENT 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
	    `gmt_create`    DATETIME(3)   NOT NULL COMMENT 'create time',
	    `gmt_modified`  DATETIME(3)   NOT NULL COMMENT 'update time',
	    PRIMARY KEY (`xid`, `branch_id`),
	    KEY `idx_gmt_modified` (`gmt_modified`),
	    KEY `idx_status` (`status`)
	) ENGINE = InnoDB
	DEFAULT CHARSET = utf8mb4;  
	
##SAGA建表	
	若代码中有使用SAGA模式，则需要建表，语句在 https://github.com/seata/seata/blob/v1.6.1/script/client/saga/db ，mysql的内容如下
	
	-- -------------------------------- The script used for sage  --------------------------------


	CREATE TABLE IF NOT EXISTS `seata_state_machine_def`
	(
	    `id`               VARCHAR(32)  NOT NULL COMMENT 'id',
	    `name`             VARCHAR(128) NOT NULL COMMENT 'name',
	    `tenant_id`        VARCHAR(32)  NOT NULL COMMENT 'tenant id',
	    `app_name`         VARCHAR(32)  NOT NULL COMMENT 'application name',
	    `type`             VARCHAR(20)  COMMENT 'state language type',
	    `comment_`         VARCHAR(255) COMMENT 'comment',
	    `ver`              VARCHAR(16)  NOT NULL COMMENT 'version',
	    `gmt_create`       DATETIME(3)  NOT NULL COMMENT 'create time',
	    `status`           VARCHAR(2)   NOT NULL COMMENT 'status(AC:active|IN:inactive)',
	    `content`          TEXT COMMENT 'content',
	    `recover_strategy` VARCHAR(16) COMMENT 'transaction recover strategy(compensate|retry)',
	    PRIMARY KEY (`id`)
	) ENGINE = InnoDB
	  DEFAULT CHARSET = utf8mb4;
	
	CREATE TABLE IF NOT EXISTS `seata_state_machine_inst`
	(
	    `id`                  VARCHAR(128)            NOT NULL COMMENT 'id',
	    `machine_id`          VARCHAR(32)             NOT NULL COMMENT 'state machine definition id',
	    `tenant_id`           VARCHAR(32)             NOT NULL COMMENT 'tenant id',
	    `parent_id`           VARCHAR(128) COMMENT 'parent id',
	    `gmt_started`         DATETIME(3)             NOT NULL COMMENT 'start time',
	    `business_key`        VARCHAR(48) COMMENT 'business key',
	    `start_params`        TEXT COMMENT 'start parameters',
	    `gmt_end`             DATETIME(3) COMMENT 'end time',
	    `excep`               BLOB COMMENT 'exception',
	    `end_params`          TEXT COMMENT 'end parameters',
	    `status`              VARCHAR(2) COMMENT 'status(SU succeed|FA failed|UN unknown|SK skipped|RU running)',
	    `compensation_status` VARCHAR(2) COMMENT 'compensation status(SU succeed|FA failed|UN unknown|SK skipped|RU running)',
	    `is_running`          TINYINT(1) COMMENT 'is running(0 no|1 yes)',
	    `gmt_updated`         DATETIME(3) NOT NULL,
	    PRIMARY KEY (`id`),
	    UNIQUE KEY `unikey_buz_tenant` (`business_key`, `tenant_id`)
	) ENGINE = InnoDB
	  DEFAULT CHARSET = utf8mb4;
	
	CREATE TABLE IF NOT EXISTS `seata_state_inst`
	(
	    `id`                       VARCHAR(48)  NOT NULL COMMENT 'id',
	    `machine_inst_id`          VARCHAR(128) NOT NULL COMMENT 'state machine instance id',
	    `name`                     VARCHAR(128) NOT NULL COMMENT 'state name',
	    `type`                     VARCHAR(20)  COMMENT 'state type',
	    `service_name`             VARCHAR(128) COMMENT 'service name',
	    `service_method`           VARCHAR(128) COMMENT 'method name',
	    `service_type`             VARCHAR(16) COMMENT 'service type',
	    `business_key`             VARCHAR(48) COMMENT 'business key',
	    `state_id_compensated_for` VARCHAR(50) COMMENT 'state compensated for',
	    `state_id_retried_for`     VARCHAR(50) COMMENT 'state retried for',
	    `gmt_started`              DATETIME(3)  NOT NULL COMMENT 'start time',
	    `is_for_update`            TINYINT(1) COMMENT 'is service for update',
	    `input_params`             TEXT COMMENT 'input parameters',
	    `output_params`            TEXT COMMENT 'output parameters',
	    `status`                   VARCHAR(2)   NOT NULL COMMENT 'status(SU succeed|FA failed|UN unknown|SK skipped|RU running)',
	    `excep`                    BLOB COMMENT 'exception',
	    `gmt_updated`              DATETIME(3) COMMENT 'update time',
	    `gmt_end`                  DATETIME(3) COMMENT 'end time',
	    PRIMARY KEY (`id`, `machine_inst_id`)
	) ENGINE = InnoDB
	  DEFAULT CHARSET = utf8mb4;	

##使用注解	
	AT只需 @GlobalTransactional
	TCC需要 @GlobalTransactional + @TwoPhaseBusinessAction + @LocalTcc
	
	
#FAQ
##AT和TCC对比
	https://developer.aliyun.com/article/1116097	
##seata是如何解决TCC空回滚、幂等、悬挂
	https://blog.csdn.net/Saintmm/article/details/127735963	
	
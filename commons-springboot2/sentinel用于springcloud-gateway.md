
#引入依赖
	<dependency>
	  	<groupId>com.alibaba.csp</groupId>
	  	<artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
	  	<version>1.8.5</version>
	</dependency>
	<!-- 不需要引入，试过引入也没异常，就像应用服务一样，但为了保持干净不用引入
	<dependency>
	  	<groupId>com.alibaba.cloud</groupId>
	  	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
	  	<version>2021.0.1.0</version>
	</dependency>-->
	<dependency>
		<groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-datasource-nacos</artifactId>
        <version>1.8.5</version>
    </dependency>
	<dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-transport-simple-http</artifactId>
        <version>1.8.5</version>
    </dependency>
    <dependency>
		<groupId>com.alibaba.csp</groupId>
		<artifactId>sentinel-cluster-client-default</artifactId>
		<version>1.8.5</version>
	</dependency>	
	

#像springboot一样使用SentinelConfiguration
	包括了
	连接控制台
	连接集群server，别忘了在server配置支持该应用(即namespace)
	监听nacos动态规则
	以及 SentinelGatewayFilter
	
#配置规则
	在nacos配置规则，就像使用springboot一样
	额外的sentinel对springcloud支持GatewayFlowRule
	默认route的id就是resourceName，另外还可以使用ApiDefinition来自定义
	
#转换为错误码
	增强ServerErrorGlobalFilter，支持对Sentinel异常的适配
	

	

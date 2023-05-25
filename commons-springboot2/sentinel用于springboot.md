
#引入依赖
	<dependency>
		<groupId>com.alibaba.cloud</groupId>
		<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
		<version>2021.1</version><!--注意版本要和springcloud匹配，该2021.1版本可以 匹配 cloud版本2020.0.5、boot版本2.5.5 -->
	</dependency>
	该依赖包含了很多sentinel子项目，例如
	<dependency>
		<groupId>com.alibaba.csp</groupId>
		<artifactId>sentinel-core</artifactId>
		<version>1.8.5</version>
	</dependency>
	<dependency>
		<groupId>com.alibaba.csp</groupId>
		<artifactId>sentinel-transport-simple-http</artifactId>
		<version>1.8.5</version>
	</dependency>
	等

#配置控制台的接入（可选，非springboot项目则需要sentinel.properties）
##引入依赖
	<dependency>
		<groupId>com.alibaba.csp</groupId>
		<artifactId>sentinel-transport-simple-http</artifactId>
		<version>1.8.5</version>
	</dependency>

##配置连接到控制台
	在application.yml配置如下（非springboot项目则需要sentinel.properties配置csp.sentinel.dashboard.server=localhost:8858）
	spring:
	  cloud:
	    sentinel:
	      transport:
	        port: 8719 #客户端开启的端口
	        dashboard: localhost:8858    #dashboard端口
	 
	控制台可以下载jar启动或docker
	jar方式： 
	java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.5.jar   
	8858是控制台web端口，-Dcsp.sentinel.dashboard.server=localhost:8858指的是自己监控自己
	
	docker方式：
	docker pull bladex/sentinel-dashboard:1.8.0 （没有发现官方镜像，这个应该是个人打包上去的，没有发现1.8.5版本）
	docker run --name bladex/sentinel-dashboard -d -p 8858:8858 bladex/sentinel-dashboard:1.8.0	       

#开启对Feign的保护（可选）
	feign:
	  sentinel:
	    enabled: true #默认false
	同时关闭重试
	spring:
	  cloud: 
	    loadbalancer: #LoadBalancerClientsProperties
	      retry:
	        enabled: false #默认true

#配置规则
##静态配置
	参考basic demo
	
##nacos动态配置
	sentinel支持从nacos获取规则并动态刷新，需要做以下事情
###引入依赖
	<dependency>
		<groupId>com.alibaba.csp</groupId>
	    <artifactId>sentinel-datasource-nacos</artifactId>
	    <version>1.8.5</version>
	</dependency>
	写本文时，使用的nacos版本是2.1.1

###在nacos中配置规则
	支持json和xml，不支持yaml格式，用json格式就可以了，注意规则是数组形式
	
	{
	    "flows":[
		    {
		    "resource":"pageSentinels",
		    "count":2,
		    "grade":1,
		    "limitApp":"default"
		    }
		],
	    "degrades":[
		    {
		    "resource":"GET:http://abc/ticket/{number}",
		    "count": 0.1,
		    "grade":1,
	        "statIntervalMs":1000,
	        "minRequestAmount":1,
	        "timeWindow":10	    
		    }
		]
	}

###sentinel监听nacos
	/**
	 * 使用 @Bean
	 * @author Fangfang.Xu
	 *
	 */
	@Slf4j
	public class SentinelNacosDynamicConfigBean {
	
		@Autowired
		private NacosConfigProperties nacosConfigProperties;
	
		// final String dataId = env.getRequiredProperty("spring.application.name");
		private final String dataId;
	//	final String groupId = "Sentinel";
		private final String groupId;
	
		public SentinelNacosDynamicConfigBean(String dataId, String groupId) {
			this.dataId = dataId;
			this.groupId = groupId;
		}
	
		@PostConstruct
		private void startSentinelConfigAutoRefresh() throws Exception {
			Properties properties = new Properties();
			properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosConfigProperties.getServerAddr());
			properties.setProperty(PropertyKeyConst.USERNAME, nacosConfigProperties.getUsername());
			properties.setProperty(PropertyKeyConst.PASSWORD, nacosConfigProperties.getPassword());
	
			ReadableDataSource<String, SentinelProperties> nacosDataSource = new NacosDataSource<>(properties, groupId,
					dataId, source -> JsonUtils.deserialize(source, SentinelProperties.class));
	
			/**
			 * 套接一层可以使配置在同一个文件中，更友好，还能减少连接数
			 */
			SentinelProperty<List<SystemRule>> systemsSentinelProperty = new DynamicSentinelProperty<List<SystemRule>>();
			SentinelProperty<List<AuthorityRule>> authoritysSentinelProperty = new DynamicSentinelProperty<List<AuthorityRule>>();
			SentinelProperty<List<FlowRule>> flowsSentinelProperty = new DynamicSentinelProperty<List<FlowRule>>();
			SentinelProperty<List<DegradeRule>> degradesSentinelProperty = new DynamicSentinelProperty<List<DegradeRule>>();
	
			nacosDataSource.getProperty().addListener(new PropertyListener<SentinelProperties>() {
	
				@Override
				public void configUpdate(SentinelProperties value) {
					log.info("sentinel config update.");
	
					if (!CollectionUtils.isEmpty(value.getSystems())) {
						log.info("sentinel SystemRule update, value:{}", value.getSystems());
						systemsSentinelProperty.updateValue(value.getSystems());
					}
					if (!CollectionUtils.isEmpty(value.getAuthoritys())) {
						log.info("sentinel AuthorityRule update, value:{}", value.getAuthoritys());
						authoritysSentinelProperty.updateValue(value.getAuthoritys());
					}
					if (!CollectionUtils.isEmpty(value.getFlows())) {
						log.info("sentinel FlowRule update, value:{}", value.getFlows());
						flowsSentinelProperty.updateValue(value.getFlows());
					}
					if (!CollectionUtils.isEmpty(value.getDegrades())) {
						log.info("sentinel DegradeRule update, value:{}", value.getDegrades());
						degradesSentinelProperty.updateValue(value.getDegrades());
					}
				}
	
				@Override
				public void configLoad(SentinelProperties value) {
					log.info("sentinel config load.");
	
					if (!CollectionUtils.isEmpty(value.getSystems())) {
						log.info("sentinel SystemRule load, value:{}", value.getSystems());
						systemsSentinelProperty.updateValue(value.getSystems());
					}
					if (!CollectionUtils.isEmpty(value.getAuthoritys())) {
						log.info("sentinel AuthorityRule load, value:{}", value.getAuthoritys());
						authoritysSentinelProperty.updateValue(value.getAuthoritys());
					}
					if (!CollectionUtils.isEmpty(value.getFlows())) {
						log.info("sentinel FlowRule load, value:{}", value.getFlows());
						flowsSentinelProperty.updateValue(value.getFlows());
					}
					if (!CollectionUtils.isEmpty(value.getDegrades())) {
						log.info("sentinel DegradeRule load, value:{}", value.getDegrades());
						degradesSentinelProperty.updateValue(value.getDegrades());
					}
				}
			});
	
			SystemRuleManager.register2Property(systemsSentinelProperty);
			AuthorityRuleManager.register2Property(authoritysSentinelProperty);
			FlowRuleManager.register2Property(flowsSentinelProperty);
			DegradeRuleManager.register2Property(degradesSentinelProperty);
		}
	
		@Getter
		@Setter
		@ToString
		public static class SentinelProperties {
			private List<SystemRule> systems;
			private List<AuthorityRule> authoritys;
			private List<FlowRule> flows;
			private List<DegradeRule> degrades;
		}
	}

##sentinel控制台动态配置
	该方式增加或修改配置时是可视化界面，更加友好，其过程是： 控制台->nacos->各个应用
	该方式需要对控制台做一些改造才能实现

#保护资源
##注解（面向spring bean）
	使用@SentinelResource

##自动适配
	对于feign等是自动适配的，不需要做什么，只需做好fallback等配置；
	feign的fallbackFactory等注解对sentinel有时候并不十分友好，这种时候还是用service包一层feign，对service使用 @SentinelResource 比较合适
##API
	特殊场景直接使用api，如何使用见basic demo

#增加各种log，让原因可见
	使用事件监听器，感知熔断的打开/关闭
	EventObserverRegistry.getInstance().addStateChangeObserver("logging",
            (prevState, newState, rule, snapshotValue) -> {
                if (newState == State.OPEN) {
                    System.out.println(String.format("%s -> OPEN at %d, snapshotValue=%.2f", prevState.name(),
                        TimeUtil.currentTimeMillis(), snapshotValue));
                } else {
                    System.out.println(String.format("%s -> %s at %d", prevState.name(), newState.name(),
                        TimeUtil.currentTimeMillis()));
                }
            });
            

#转换为错误码
	继承ApiResponseExceptionHandler、NativeRestApiExceptionHandler，支持对Sentinel异常的适配
	
#集群模式
	仅支持流控的集群，熔断不支持集群，因此意义上少了一些。
##集群server
###引入maven依赖
	<dependency>
		<groupId>com.alibaba.csp</groupId>
		<artifactId>sentinel-cluster-server-default</artifactId>
		<version>1.8.5</version>
	</dependency>		
###监听nacos规则
	集群只支持流控，所以只要管理流控配置
###配置支持哪些namespace
	监听nacos或设置进去
###启动server			
	tokenServer.start();
	
##集群client
###修改规则为集群模式
	例如
	{
		"resource": "pageDataMains",
		"count": 1,
		"grade": 1,
		"limitApp": "default",
		"clusterMode":true,
		"clusterConfig":{
			"flowId":123, -- 要求每个规则全局唯一
			"thresholdType":1 -- 1表示集群总体qps总和=count，0是本地均摊qps总和=实例数*count
		}
	}
	
###引入maven依赖
	<dependency>
		<groupId>com.alibaba.csp</groupId>
		<artifactId>sentinel-cluster-client-default</artifactId>
		<version>1.8.5</version>
	</dependency>	
###配置为client身份
	ClusterStateManager.applyState(ClusterStateManager.CLUSTER_CLIENT);
###配置server地址
	DynamicSentinelProperty<ClusterClientAssignConfig> sentinelProperty = new DynamicSentinelProperty<ClusterClientAssignConfig>();
    ClusterClientConfigManager.registerServerAssignProperty(sentinelProperty);
    ClusterClientAssignConfig clusterClientAssignConfig = new ClusterClientAssignConfig("localhost", 11111);
    sentinelProperty.updateValue(clusterClientAssignConfig);	
###启动client
	在springboot应用中经过上面的步骤，将会自动启动

#FAQ
##如何设置1qps/5秒
###ParamFlowRule方式
	他支持durationInSec可以M次/N秒，还支持集群，这种方式最好
###FlowRule方式
	Sentinel无法直接设定一个5秒一共1个请求的流控规则，但可以使用PaceFlow方式来变相支持
	FlowRule rule1 = new FlowRule();
    rule1.setResource(KEY);
    rule1.setCount(1);
    rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
    rule1.setLimitApp("default");   
    rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
    rule1.setMaxQueueingTimeMs(5 * 1000);
	以上规则是 1qps/5秒，队列时间是5秒，控制方式是RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER
	这样在5秒内一共支持1qps，5秒结束前过多的qps会进行队列，5秒结束时qps超过部分将被拒绝
	调用方的现象将会是RT=0（正好窗口期要结束，超过部分被拒绝）-5秒（正好窗口期刚开始，进入队列等5秒）
	
	这个方式的缺点是不能把窗口期设置很大，否则高并发时有很多请求将进入队列，这样等待时间会很长，最好还是按秒级来划分		
###GatewayFlowRule方式
	另一个方式是在gateway上使用GatewayFlowRule.intervalSec，这个值可以调整qps是几秒而不是固定1秒；但GatewayFlowRule不支持集群，他没有clusterMode可以设置

##@SentinelResource.exceptionsToIgnore有什么用
	配置了exceptionsToIgnore后，对应的异常就会被直接抛出而不会被blockHandler、fallback等处理，也不会被Tracer记录统计从而不影响熔断的统计

##@SentinelResource.blockHandler可以处理哪些异常
	只能处理BlockException，且必须使用BlockException接收

##@SentinelResource.fallback可以处理哪些异常
	对所有异常起作用，包括BlockException，除了exceptionsToIgnore。可以使用Throwable接收
	
	如果同时配置了blockHandler、fallback，则BlockException只会进入blockHandler，其他异常进fallback
	如果都没配，则直接抛出（如果方法没有定义throws BlockException 则会被JVM包装一层UndeclaredThrowableException）

##对Feign的FallbackFactory可以处理哪些异常
	对所有异常起作用，意外异常和BlockException都会进，这个方式的缺点是无法配置需要忽略的异常（不过基本上feign的异常不能忽略）
	@Component//需要是个spring bean
	public class SentinelFeignFallbackFactory implements FallbackFactory<SentinelFeign> {
	
		@Override
		public SentinelFeign create(Throwable cause) {
			return new SentinelFeign() {
				@Override
				public String call(String number) {
					System.out.println(cause);
					System.out.println(number);
					return "fallback factory";
				}
			};
		}
	}
	

##对Feign的resource名称是什么
	feign的资源名=method:protocol://url ， 例如 GET:http://服务名或域名/ticket/{number} ， 其中{number}只需原样填入即可

##如何强制打开/关闭流控
	只需设置QPS的count=0并刷新，即可全部限流；再次设置为>0或删除规则即可恢复正常流控

##如何强制打开/关闭熔断
	需要更新或新增规则类型是 慢比例，count=0，slowRatioThreshold=0.0，minRequestAmount=1 并刷新，即可打开熔断；再次设置或删除规则即可关闭熔断
	DegradeRule rule = new DegradeRule("HelloWorld")
	            .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
	            // Max allowed response time
	            .setCount(0)
	            // Retry timeout (in second)
	            .setTimeWindow(10)
	            // Circuit breaker opens when slow request ratio > 60%
	            .setSlowRatioThreshold(0.0)
	            .setMinRequestAmount(1)
	            .setStatIntervalMs(10000);
	 
	

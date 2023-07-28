# 0.2.2

* 优化AbstractExceptionHandler的返回结果
* 增加HashFunction接口
* 增加一致性hash
* 优化shardingshare时sql语句的打印输出
* 修复nio java包中server下线后一直尝试重连的问题

# 0.3.0

* 增加kafka消费者强制关闭
* 增加NextQuery支持类
* 增加是否允许TableCount的支持类
* 增加hbase模块

# 0.3.1

* fix hbase 客户端类型修改为可选
* NioClientPool优化

# 1.0.0
* 增加gateway模块
* 增加elasticsearch模块
* 增加dao系列接口（mybatis、hbase、elasticsearch）
* 增加springboot模块支持sentinel
* 增加FlowTagLoadBalancer负载均衡

* fix RedisLock 使用RedisTemplate 类型转换问题
* 优化ZooKeeperHolder重连保障
* 优化AbstractExceptionHandler

# 1.1.0
* springboot模块增加elasticsearch、hbase、kafka、zookeeper、shardingsphere的AutoConfiguration

# 1.2.0
* 增加hessian2

# 1.2.1
* fix ZooKeeperHolder 锁

# 2.0.0
* 增加seata springcloud支持（springcloud lb的xid传递，spring-cloud-alibaba-seata只支持ribbon）
* 增加beecomb的AutoConfiguration
* 增加mysql分布式锁
* 增加skywalking/arms TraceCtx
* 增加logback LogbackExtConverter
* 增加gateway Signature模式下哪些path需要认证可配
* 修复gateway CacheRequestBody springboot3之前有OutOfDirectMemoryError
* 修复gateway 连不上sentinel dashbaord，初始化顺序所致
* Cacher的AutoConfiguration增加支持负载保护（防穿透等）

# 2.1.0
* 优化NIO
* 支持Reactive Web 的SecurityUtils, ReactiveWebUtils
* 增加RedisBloomFilter
* 支持顺序消费OrderedReliabilityConsumer



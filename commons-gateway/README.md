# 工程使用方式
* 支持直接打包成可执行jar使用
* 也可以当成框架引入进行扩展开发，这种方式下src/main/resources需要全部重新定义，无法继承
* 模式选择--spring.profiles.active=apigateway|openapigateway

# 配置支持
* bootstrap.yml 可配不同nacos
* application.yml 可配常规参数，一般不用修改
* GatewaySecurityConfiguration.java 可配是否启用该类和配置参数
* GatewaySentinelConfiguration.java 可配是否启用该类和配置参数

# FQA
* spring gateway中使用controller的并发线程受cpu线程数影响（例如2线程的cpu只能并发处理2个，未找到可配项），但使用endpoint则无此问题（效果跟请求下游服务一样）
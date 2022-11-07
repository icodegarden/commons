# 工程使用方式
* 支持直接打包成可执行jar使用
* 也可以当成框架引入进行扩展开发，这种方式下src/main/resources需要全部重新定义，无法继承

# 配置支持
* bootstrap.yml 可配不同nacos
* application.yml 可配常规参数，一般不用修改
* GatewaySecurityConfiguration.java 可配是否启用该类和配置参数
* GatewaySentinelConfiguration.java 可配是否启用该类和配置参数



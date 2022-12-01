# 工程使用方式
* 支持直接打包成可执行jar使用
* 也可以当成框架引入进行扩展开发，这种方式下src/main/resources需要全部重新定义，无法继承
* 模式选择--spring.profiles.active=apigateway|openapigateway

# 配置支持
* bootstrap.yml 可配nacos参数
* application.yml 可配常规参数，一般不用修改
* GatewaySecurityConfiguration.java 可配是否启用该类和配置参数
* GatewaySentinelConfiguration.java 可配是否启用该类和配置参数

# 核心类说明
## io.github.icodegarden.commons.gateway.autoconfigure
* GatewayBeanAutoConfiguration.java 通用bean配置
* GatewaySecurityAutoConfiguration.java springsecurity配置，决定了使用什么认证方式
* GatewaySentinelAutoConfiguration.java 配置网关sentinel支持
* ReadnessEndpointAutoConfiguration.java 配置网关的readness接口，这同时需要配置management.endpoints.web.exposure.include: health,readness
* RoutePredicateFactoryAutoConfiguration.java 配置自定义的路由器

## io.github.icodegarden.commons.gateway.core.security
* AppProvider.java 支持自定义的App数据源
* DefaultAppProvider.java 默认的使用配置中心的apps
* DefaultOpenApiRequestValidator.java 默认防重放验证器
* JWTAuthenticationWebFilter.java jwt认证方式，选1
* SignAuthenticationWebFilter.java 签名认证方式，选1

## io.github.icodegarden.commons.gateway.filter
* OpenapiResponseSignGlobalFilter.java 对openapi的响应结果补充签名字段
* SentinelIdentityParamFlowGlobalFilter.java 支持以身份方式（userId/appId）进行限流
* ServerErrorGlobalFilter.java 对网关发生的异常进行处理

## io.github.icodegarden.commons.gateway.predicate
* BodyMethodRoutePredicateFactory.java 从请求体的method进行路由

## io.github.icodegarden.commons.gateway.properties
* CommonsGatewaySecurityProperties.java 安全配置，配置jwt还是signature方式决定了使用什么认证方式


# FQA
* spring gateway中使用controller的并发线程受cpu线程数影响（例如2线程的cpu只能并发处理2个，未找到可配项），但使用endpoint则无此问题（效果跟请求下游服务一样）

# 动态修改日志级别

配置 management.endpoints.web.exposure.include: loggers

```java
curl http://localhost:8080/actuator/loggers/com.geely.ros.tspgateway.security.TspWebFilter -i -X POST -D '{"configuredLevel":"DEBUG"}' --header "Content-Type:application/json"
```
package io.github.icodegarden.commons.springboot.configuration;

import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@LoadBalancerClients(defaultConfiguration = FlowTagLoadBalancerClientConfiguration.class)
public class FlowTagLoadBalancerAutoConfiguration {

}
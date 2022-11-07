package io.github.icodegarden.commons.apigateway.configuration;
//package com.geely.gddc.apigateway.configuration;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.annotation.PostConstruct;
//
//import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
//import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
//import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
//import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
//import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
//import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
//import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
//import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
//import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
//import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
//import com.alibaba.csp.sentinel.slots.block.RuleConstant;
//import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
//import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
//import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
//import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
//import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
//import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
//import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
//
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.codec.ServerCodecConfigurer;
//import org.springframework.web.reactive.result.view.ViewResolver;
//
///**
// * @author Eric Zhao
// */
//@Configuration
//public class GatewayConfigurationDemo {
//
//    private final List<ViewResolver> viewResolvers;
//    private final ServerCodecConfigurer serverCodecConfigurer;
//
//    public GatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
//                                ServerCodecConfigurer serverCodecConfigurer) {
//        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
//        this.serverCodecConfigurer = serverCodecConfigurer;
//    }
//
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
//        // Register the block exception handler for Spring Cloud Gateway.
//        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
//    }
//
//    @Bean
//    @Order(-1)
//    public GlobalFilter sentinelGatewayFilter() {
//        return new SentinelGatewayFilter();
//    }
//
//    @PostConstruct
//    public void doInit() {
//        initCustomizedApis();
//        initGatewayRules();
//    }
//
//    private void initCustomizedApis() {
//        Set<ApiDefinition> definitions = new HashSet<>();
//        ApiDefinition api1 = new ApiDefinition("some_customized_api")
//            .setPredicateItems(new HashSet<ApiPredicateItem>() {{
//                add(new ApiPathPredicateItem().setPattern("/ahas"));
//                add(new ApiPathPredicateItem().setPattern("/product/**")
//                    .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
//            }});
////        ApiDefinition api2 = new ApiDefinition("another_customized_api")
////            .setPredicateItems(new HashSet<ApiPredicateItem>() {{
////                add(new ApiPathPredicateItem().setPattern("/**")
////                    .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
////            }});
//        definitions.add(api1);
////        definitions.add(api2);
//        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
//    }
//
//    private void initGatewayRules() {
//        Set<GatewayFlowRule> rules = new HashSet<>();
//        rules.add(new GatewayFlowRule("aliyun_route")
//            .setCount(10)
//            .setIntervalSec(1)
//        );
//        rules.add(new GatewayFlowRule("aliyun_route")
//            .setCount(2)
//            .setIntervalSec(2)
//            .setBurst(2)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
//            )
//        );
//        rules.add(new GatewayFlowRule("httpbin_route")
//            .setCount(10)
//            .setIntervalSec(1)
//            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
//            .setMaxQueueingTimeoutMs(600)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
//                .setFieldName("X-Sentinel-Flag")
//            )
//        );
//        rules.add(new GatewayFlowRule("httpbin_route")
//            .setCount(1)
//            .setIntervalSec(1)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
//                .setFieldName("pa")
//            )
//        );
//        rules.add(new GatewayFlowRule("httpbin_route")
//            .setCount(2)
//            .setIntervalSec(30)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
//                .setFieldName("type")
//                .setPattern("warn")
//                .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_CONTAINS)
//            )
//        );
//
//        rules.add(new GatewayFlowRule("some_customized_api")
//            .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
//            .setCount(5)
//            .setIntervalSec(1)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
//                .setFieldName("pn")
//            )
//        );
//        
//        
//        
//        rules.add(new GatewayFlowRule("manage_route")
//                .setCount(1)
//                .setIntervalSec(3)
//            );
//        
//        
//        GatewayRuleManager.loadRules(rules);
//        
//        
////        AuthorityRule authorityRule = new AuthorityRule();
////        authorityRule.setResource("manage_route");
////        authorityRule.setLimitApp("localhost");
////        authorityRule.setStrategy(1);
////        AuthorityRule authorityRule2 = new AuthorityRule();
////        authorityRule2.setResource("manage_route");
////        authorityRule2.setLimitApp("127.0.0.1");
////        authorityRule2.setStrategy(1);
////        AuthorityRuleManager.loadRules(Arrays.asList(authorityRule,authorityRule2));
//
//        
//        
////        FlowRule flowRule = new FlowRule();
////        flowRule.setResource("manage_route");
////        flowRule.setLimitApp("default");
////        flowRule.setCount(1);
////        FlowRuleManager.loadRules(Arrays.asList(flowRule));
//        
//        
////        DegradeRule rule = new DegradeRule("manage_route")
////                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
////                // Set ratio threshold to 50%.
////                .setCount(0.1d)
////                .setStatIntervalMs(3000)
////                .setMinRequestAmount(1)
////                // Retry timeout (in second)
////                .setTimeWindow(5);
////        DegradeRuleManager.loadRules(Arrays.asList(rule));
//    }
//}

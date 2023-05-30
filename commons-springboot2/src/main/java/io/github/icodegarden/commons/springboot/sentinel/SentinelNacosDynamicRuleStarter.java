package io.github.icodegarden.commons.springboot.sentinel;

import java.util.List;
import java.util.Properties;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.nacos.api.PropertyKeyConst;

import io.github.icodegarden.commons.lang.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class SentinelNacosDynamicRuleStarter {

	public static void start(NacosConfigProperties nacosConfigProperties, String dataId, String groupId) {
		log.info("starting sentinel nacos dynamic, nacosConfigProperties:{}, dataId:{}, groupId:{}",
				nacosConfigProperties, dataId, groupId);

		Properties properties = new Properties();
		properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosConfigProperties.getServerAddr());
		properties.setProperty(PropertyKeyConst.USERNAME, nacosConfigProperties.getUsername());
		properties.setProperty(PropertyKeyConst.PASSWORD, nacosConfigProperties.getPassword());

		ReadableDataSource<String, RuleProperties> nacosDataSource = new NacosDataSource<>(properties, groupId, dataId,
				source -> {
					if (!StringUtils.hasText(source)) {// 有可能没有配置
						return new RuleProperties();
					}
					return JsonUtils.deserialize(source, RuleProperties.class);
				});

		/**
		 * 套接一层可以使配置在同一个文件中，更友好，还能减少连接数
		 */
		SentinelProperty<List<SystemRule>> systemsSentinelProperty = new DynamicSentinelProperty<List<SystemRule>>();
		SentinelProperty<List<AuthorityRule>> authoritysSentinelProperty = new DynamicSentinelProperty<List<AuthorityRule>>();
		SentinelProperty<List<FlowRule>> flowsSentinelProperty = new DynamicSentinelProperty<List<FlowRule>>();
		SentinelProperty<List<ParamFlowRule>> paramFlowsSentinelProperty = new DynamicSentinelProperty<List<ParamFlowRule>>();
		SentinelProperty<List<DegradeRule>> degradesSentinelProperty = new DynamicSentinelProperty<List<DegradeRule>>();

		nacosDataSource.getProperty().addListener(new PropertyListener<RuleProperties>() {

			@Override
			public void configUpdate(RuleProperties value) {
				log.info("sentinel config update.");
				if(value == null) {
					return;
				}

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
				if (!CollectionUtils.isEmpty(value.getParamFlows())) {
					log.info("sentinel ParamFlowRule update, value:{}", value.getParamFlows());
					paramFlowsSentinelProperty.updateValue(value.getParamFlows());
				}
				if (!CollectionUtils.isEmpty(value.getDegrades())) {
					log.info("sentinel DegradeRule update, value:{}", value.getDegrades());
					degradesSentinelProperty.updateValue(value.getDegrades());
				}
			}

			@Override
			public void configLoad(RuleProperties value) {
				log.info("sentinel config load.");
				if(value == null) {
					return;
				}
				
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
				if (!CollectionUtils.isEmpty(value.getParamFlows())) {
					log.info("sentinel ParamFlowRule load, value:{}", value.getParamFlows());
					paramFlowsSentinelProperty.updateValue(value.getParamFlows());
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
		ParamFlowRuleManager.register2Property(paramFlowsSentinelProperty);
		DegradeRuleManager.register2Property(degradesSentinelProperty);
	}

}
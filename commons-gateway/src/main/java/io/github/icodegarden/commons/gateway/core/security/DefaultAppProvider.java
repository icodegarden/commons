package io.github.icodegarden.commons.gateway.core.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 从配置中获取的
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class DefaultAppProvider implements AppProvider {

	@Autowired
	private CommonsGatewaySecurityProperties securityProperties;

	private Map<String/* appId */, App> appMap = new HashMap<String, App>();

	@PostConstruct
	private void init() {
		ScheduledThreadPoolExecutor scheduledThreadPool = ThreadPoolUtils
				.newSingleScheduledThreadPool("DefaultAppsProvider");
		scheduledThreadPool.scheduleWithFixedDelay(() -> {
			try {
				refreshApps();
			} catch (Exception e) {
				log.error("ex on refreshApps", e);
			}
		}, 0, 5, TimeUnit.SECONDS);
	}

	@Override
	public App getApp(String appId) {
		return appMap.get(appId);
	}

	private void refreshApps() {
		CommonsGatewaySecurityProperties.Signature signature = securityProperties.getSignature();
		if (signature != null) {
			List<App> apps = signature.getApps();
			if(apps != null) {
				this.appMap = apps.stream().collect(Collectors.toMap(App::getAppId, app -> app));	
			}
		}
	}
}

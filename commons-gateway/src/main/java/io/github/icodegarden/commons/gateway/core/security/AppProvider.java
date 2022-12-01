package io.github.icodegarden.commons.gateway.core.security;

import org.springframework.lang.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface AppProvider {

//	@Nullable
//	Map<String/* appId */, App> getApps();

	@Nullable
	App getApp(String appId);
}

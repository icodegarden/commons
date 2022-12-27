package io.github.icodegarden.commons.gateway.spi;

import org.springframework.lang.Nullable;

import io.github.icodegarden.commons.gateway.core.security.signature.App;

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

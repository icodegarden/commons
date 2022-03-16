package io.github.icodegarden.commons.springboot;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 通常你需要 @Bean
 * @author Fangfang.Xu
 *
 */
public class SpringContext implements ApplicationContextAware {

	private static ApplicationContext ac;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContext.ac = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return ac;
	}
}
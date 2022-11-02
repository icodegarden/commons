package io.github.icodegarden.commons.springboot.configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.springboot.event.RemoveCacheEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@Slf4j
public class CommonsCacheAutoConfiguration {

	@ConditionalOnClass(name = "io.github.icodegarden.wing.Cacher")
	@ConditionalOnProperty(value = "commons.cacher.removeAfterTransactionCommit.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	protected class RemoveAfterTransactionCommitAutoConfiguration extends AutoConfigurationSupport {
		{
			log.info("commons init bean of RemoveAfterTransactionCommitAutoConfiguration");
		}

		@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
		public void handleRemoveCacheEvent(RemoveCacheEvent event) {
			if (!CollectionUtils.isEmpty(event.getCacheKeys())) {
				if (log.isInfoEnabled()) {
					log.info("remove cache after transaction, keys:{}", event.getCacheKeys());
				}
				doRemove(event);
			}
		}
	}

	/**
	 * 适用于单元测试，因为单元测试入口一般带有事务，无法使用@TransactionalEventListener
	 * 
	 * @author Fangfang.Xu
	 *
	 */
	@ConditionalOnClass(name = "io.github.icodegarden.wing.Cacher")
	@ConditionalOnProperty(value = "commons.cacher.removeOnEvent.enabled", havingValue = "true", matchIfMissing = false)
	@Configuration
	protected class RemoveOnEventAutoConfiguration extends AutoConfigurationSupport {
		{
			log.info("commons init bean of RemoveOnEventAutoConfiguration");
		}
		
		@EventListener(value = RemoveCacheEvent.class)
		public void handleRemoveCacheEvent(RemoveCacheEvent event) {
			if (!CollectionUtils.isEmpty(event.getCacheKeys())) {
				if (log.isInfoEnabled()) {
					log.info("remove cache on event, keys:{}", event.getCacheKeys());
				}
				doRemove(event);
			}
		}
	}

	protected static class AutoConfigurationSupport implements ApplicationContextAware {
		private Method methodRemove;
		private List<Object> cachers;

		@Override
		public void setApplicationContext(ApplicationContext ac) throws BeansException {
			Class<?> cacherInterface;
			try {
				cacherInterface = Class.forName("io.github.icodegarden.wing.Cacher");
				methodRemove = cacherInterface.getDeclaredMethod("remove", Collection.class);
			} catch (Exception e) {
				throw new IllegalStateException("ex on init", e);
			}

			String[] beanDefinitionNames = ac.getBeanDefinitionNames();

			cachers = Arrays.asList(beanDefinitionNames).stream().filter(name -> {
				if (!name.contains("cache")) {
					return false;
				}
				Object bean = ac.getBean(name);
				if (!cacherInterface.isAssignableFrom(bean.getClass())) {
					return false;
				}
				return true;
			}).map(name -> {
				return ac.getBean(name);
			}).collect(Collectors.toList());
			
			log.info("commons init found cachers:{}", cachers);
		}

		void doRemove(RemoveCacheEvent event) {
			if(!CollectionUtils.isEmpty(cachers)) {
				for (Object cacher : cachers) {
					try {
						methodRemove.invoke(cacher, event.getCacheKeys());
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						log.error("ex on remove cache, cacher:{}, keys:{}", cacher, event.getCacheKeys(), e);
					}
				}
			}
		}
	}
}

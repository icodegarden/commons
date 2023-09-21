package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.Hessian2Deserializer;
import io.github.icodegarden.commons.lang.serialization.Hessian2Serializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.springboot.cache.SpringApplicationCacher;
import io.github.icodegarden.commons.springboot.event.RemoveCacheEvent;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.protect.Filter;
import io.github.icodegarden.wing.protect.OverloadProtectionCacher;
import io.github.icodegarden.wing.protect.Protector;
import io.github.icodegarden.wing.redis.RedisCacher;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@AutoConfigureAfter(CommonsRedisAutoConfiguration.class) // 顺序依赖
@Configuration
@Slf4j
public class CommonsCacheAutoConfiguration {

	@ConditionalOnClass({ Cacher.class })
	@ConditionalOnBean(RedisExecutor.class) // 依赖项
	@ConditionalOnProperty(value = "commons.cacher.redis.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	protected static class RedisCacherAutoConfiguration {

		@ConditionalOnMissingBean(Cacher.class)
		@Bean
		public Cacher springApplicationCacher(List<Filter> filters, List<Protector> protectors,
				RedisExecutor redisExecutor, ApplicationEventPublisher applicationEventPublisher) {
			log.info("commons init bean of SpringApplicationCacher");

			Serializer<?> serializer = new Hessian2Serializer();
			Deserializer<?> deserializer = new Hessian2Deserializer();
			RedisCacher cacher = new RedisCacher(redisExecutor, serializer, deserializer);

			OverloadProtectionCacher overloadProtectionCacher = new OverloadProtectionCacher(cacher, filters,
					protectors);

			return new SpringApplicationCacher(overloadProtectionCacher, applicationEventPublisher);
		}
	}

	@ConditionalOnClass({ Cacher.class, Transactional.class })
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
	 * 适用于没有开启事务的，不会触发@TransactionalEventListener
	 */
	@ConditionalOnClass(value = { Cacher.class, Transactional.class })
	@ConditionalOnProperty(value = "commons.cacher.removeOnEvent.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	protected class RemoveOnEventAutoConfiguration extends AutoConfigurationSupport {
		{
			log.info("commons init bean of RemoveOnEventAutoConfiguration");
		}

		@EventListener(value = RemoveCacheEvent.class)
		public void handleRemoveCacheEvent(RemoveCacheEvent event) {
			if (TransactionSynchronizationManager.isActualTransactionActive()) {
				/**
				 * 如果发现开启了事务，则由@TransactionalEventListener处理
				 */
				return;
			}

			if (!CollectionUtils.isEmpty(event.getCacheKeys())) {
				if (log.isInfoEnabled()) {
					log.info("remove cache on event, keys:{}", event.getCacheKeys());
				}
				doRemove(event);
			}
		}
	}

	/**
	 * 适用于单元测试，因为单元测试入口一般带有事务，无法使用@TransactionalEventListener
	 */
	@ConditionalOnClass(value = { Cacher.class }, name = { "org.junit.jupiter.api.Test" })
	@ConditionalOnProperty(value = "commons.cacher.removeForTest.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	protected class RemoveForTestAutoConfiguration extends AutoConfigurationSupport {
		{
			log.info("commons init bean of RemoveForTestAutoConfiguration");
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

	/**
	 * 适用于wing-core没有引入的情况下，只能用反射处理
	 */
//	protected static class AutoConfigurationSupport implements ApplicationContextAware {
//		private Method methodRemove;
//		private Collection<Object> cachers;
//
//		@Override
//		public void setApplicationContext(ApplicationContext ac) throws BeansException {
//			Class<?> cacherInterface;
//			try {
//				cacherInterface = Class.forName("io.github.icodegarden.wing.Cacher");
//				methodRemove = cacherInterface.getDeclaredMethod("remove", Collection.class);
//			} catch (Exception e) {
//				throw new IllegalStateException("ex on init", e);
//			}
//
//			String[] beanDefinitionNames = ac.getBeanDefinitionNames();
//
//			cachers = Arrays.asList(beanDefinitionNames).stream().filter(name -> {
//				if (!name.contains("cache")) {
//					return false;
//				}
//				Object bean = ac.getBean(name);
//				if (!cacherInterface.isAssignableFrom(bean.getClass())) {
//					return false;
//				}
//				return true;
//			}).map(name -> {
//				return ac.getBean(name);
//			}).collect(Collectors.toList());
//
//			log.info("commons init found cachers:{}", cachers);
//		}
//
//		protected void doRemove(RemoveCacheEvent event) {
//			if (!CollectionUtils.isEmpty(cachers)) {
//				for (Object cacher : cachers) {
//					try {
//						methodRemove.invoke(cacher, event.getCacheKeys());
//					} catch (Exception e) {
//						log.error("ex on remove cache, cacher:{}, keys:{}", cacher, event.getCacheKeys(), e);
//					}
//				}
//			}
//		}
//	}

	/**
	 * 现已引入wing-core 作为Optional
	 */
	protected static class AutoConfigurationSupport implements ApplicationContextAware {
		private Collection<Cacher> cachers;

		@Override
		public void setApplicationContext(ApplicationContext ac) throws BeansException {
			Map<String, Cacher> beansOfType = ac.getBeansOfType(Cacher.class);
			if (beansOfType != null) {
				cachers = beansOfType.values().stream().map(cacher -> {
					if (cacher instanceof SpringApplicationCacher) {
						return ((SpringApplicationCacher) cacher).getCacher();
					}
					return cacher;
				}).collect(Collectors.toList());
			}

			log.info("commons init found cachers:{}", cachers);
		}

		protected void doRemove(RemoveCacheEvent event) {
			if (!CollectionUtils.isEmpty(cachers)) {
				for (Cacher cacher : cachers) {
					try {
						cacher.remove(event.getCacheKeys());
					} catch (Exception e) {
						log.error("ex on remove cache, cacher:{}, keys:{}", cacher, event.getCacheKeys(), e);
					}
				}
			}
		}
	}
}

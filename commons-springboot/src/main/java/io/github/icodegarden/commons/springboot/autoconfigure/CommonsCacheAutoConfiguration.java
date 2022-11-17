package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.springboot.event.RemoveCacheEvent;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.redis.RedisCacher;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@Slf4j
public class CommonsCacheAutoConfiguration {

	@ConditionalOnClass({ Cacher.class })
	@ConditionalOnProperty(value = "commons.cacher.redisCacher.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	protected static class RedisCacherAutoConfiguration {
		
		@ConditionalOnMissingBean(Cacher.class)
		@ConditionalOnBean(RedisExecutor.class)//依赖项
		@Bean
		public Cacher redisCacher(RedisExecutor redisExecutor) {
			log.info("commons init bean of RedisCacher");

			Serializer<?> serializer = new KryoSerializer();
			KryoDeserializer deserializer = new KryoDeserializer();
			return new RedisCacher(redisExecutor, serializer, deserializer);
		}
	}

	@ConditionalOnClass({ Cacher.class })
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
	 */
	@ConditionalOnClass(value = { Cacher.class }, name = { "org.junit.jupiter.api.Test" })
	@ConditionalOnProperty(value = "commons.cacher.removeOnEvent.enabled", havingValue = "true", matchIfMissing = false)
	@Configuration
	protected class Remove4TestAutoConfiguration extends AutoConfigurationSupport {
		{
			log.info("commons init bean of Remove4TestAutoConfiguration");
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
				cachers = beansOfType.values();
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

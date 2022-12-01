package io.github.icodegarden.commons.gateway.core.security;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.LogUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class DefaultOpenApiRequestValidator implements OpenApiRequestValidator {

	/**
	 * 可配
	 */
	public static int REJECT_SECONDS_BEFORE = 5 * 60;

	private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

	private Object object = new Object();

	private Map<String/* appId */, Map<RequestIdSoftReference, Object>> appRequestIds = new HashMap<>(64);

	public DefaultOpenApiRequestValidator() {
		/**
		 * 也可以在validate方法最后进行清理检查，但那样需要控制并发
		 */
		new Thread() {
			public void run() {
				for (;;) {
					/**
					 * 一个SoftReference对象占用的内存=16字节头空间+8字节内部所引用的基本数据类型，开销很低可以使用大量对象，但也需要提供一下保障，否则几百万个对象就需要大量内存了
					 */
					try {
						/**
						 * 从referenceQueue获取出来的，reference.get() 是null
						 */
						RequestIdSoftReference reference = (RequestIdSoftReference) referenceQueue.remove();// 阻塞的
						Map<RequestIdSoftReference, Object> map = appRequestIds.get(reference.app_id);
						map.remove(reference);
					} catch (InterruptedException e) {
					}
				}
			};
		}.start();
	}

	@Override
	public boolean validate(OpenApiRequestBody requestBody) {
		Assert.hasText(requestBody.getTimestamp(), "Missing:timestamp");
		Assert.hasText(requestBody.getApp_id(), "Missing:app_id");
		Assert.hasText(requestBody.getRequest_id(), "Missing:request_id");

		/**
		 * n分钟之前的直接拒绝
		 */
		LocalDateTime ts = LocalDateTime.parse(requestBody.getTimestamp(), SystemUtils.STANDARD_DATETIME_FORMATTER);
		if (ts.plusSeconds(REJECT_SECONDS_BEFORE).isBefore(SystemUtils.now())) {
			return false;
		}

		/**
		 * appId隔离<br>
		 * 由于该方式不是集群的，不严格，因此所使用的Map也没必要使用支持并发的Map<br>
		 */
		Map<RequestIdSoftReference, Object> requestIds = appRequestIds.computeIfAbsent(requestBody.getApp_id(),
				key -> new HashMap<RequestIdSoftReference, Object>(10240));

		Object pre = requestIds.put(
				new RequestIdSoftReference(requestBody.getApp_id(), requestBody.getRequest_id(), referenceQueue),
				object);
		if (pre != null) {
			LogUtils.infoIfEnabled(log, () -> log.info("openapi request reject by duplicate, request_id:{}, app_id:{}",
					requestBody.getRequest_id(), requestBody.getApp_id()));
			return false;
		}

		return true;
	}

	public int getAppExistRequestIdSize(String app_id) {
		return appRequestIds.getOrDefault(app_id, Collections.emptyMap()/* getOrDefault 不会put进map */).size();
	}

	private static class RequestIdSoftReference extends SoftReference<String> {

		/**
		 * 该字段不是referent，可以放这里使用
		 */
		private final String app_id;

		/**
		 * 不可以直接存 String request_id; 否则将导致Reference无法进ReferenceQueue，因为request_id被引用着
		 */
		private final int request_id_hash;

		public RequestIdSoftReference(String app_id, String request_id, ReferenceQueue<? super String> q) {
			super(request_id, q);
			this.app_id = app_id;
			this.request_id_hash = request_id.hashCode();
		}

		/**
		 * 存入map的hash即request_id的hash
		 */
		@Override
		public int hashCode() {
			return request_id_hash;
		}

		/**
		 * 存入map的equals 即request_id的equals
		 */
		@Override
		public boolean equals(Object obj) {
			RequestIdSoftReference target = (RequestIdSoftReference) obj;
			return this.request_id_hash == target.request_id_hash;
		}

		@Override
		public String toString() {
			return app_id + Integer.toString(request_id_hash);
		}
	}

}

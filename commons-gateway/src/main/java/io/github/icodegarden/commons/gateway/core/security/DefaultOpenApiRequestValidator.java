package io.github.icodegarden.commons.gateway.core.security;

import java.lang.ref.SoftReference;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultOpenApiRequestValidator implements OpenApiRequestValidator {

	/**
	 * 可改
	 */
	public static int REJECT_SECONDS_BEFORE = 5 * 60;
	/**
	 * 当超过这个数量的时候，有很多历史数据早已无效
	 */
	public static int MAX_REQUESTIDS_PER_APP = 1024 * 100;

	private Object object = new Object();

	private Map<String/* appId */, Map<RequestIdSoftReference, Object>> appRequestIds = new HashMap<>(64);

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

		Object pre = requestIds.put(new RequestIdSoftReference(requestBody.getRequest_id()), object);
		if (pre != null) {
			return false;
		}

		/**
		 * 一个SoftReference对象占用的内存=16字节头空间+8字节内部所引用的基本数据类型，开销很低可以使用大量对象，但也需要提供一下保障
		 */
		if (requestIds.size() > MAX_REQUESTIDS_PER_APP) {
			requestIds.clear();
		}

		return true;
	}

	public int getAppExistRequestIdSize(String app_id) {
		return appRequestIds.getOrDefault(app_id, Collections.emptyMap()/* getOrDefault 不会put进map */).size();
	}

	private class RequestIdSoftReference extends SoftReference<String> {

		public RequestIdSoftReference(String referent) {
			super(referent);
		}

		@Override
		public int hashCode() {
			return get().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			RequestIdSoftReference target = (RequestIdSoftReference) obj;
			return get().equals(target.get());
		}

		@Override
		public String toString() {
			return get() != null ? get().toString() : null;
		}
	}

}

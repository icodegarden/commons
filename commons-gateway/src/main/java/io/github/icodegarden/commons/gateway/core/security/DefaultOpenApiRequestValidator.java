package io.github.icodegarden.commons.gateway.core.security;

import java.lang.ref.SoftReference;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

	private Object object;

	private Map<String/* appId */, Map<RequestIdSoftReference, Object>> appRequestIds = new HashMap<>(64);

	@Override
	public boolean validate(OpenApiRequestBody requestBody) {
		/**
		 * n分钟之前的直接拒绝
		 */
		LocalDateTime ts = LocalDateTime.parse(requestBody.getTimestamp(), SystemUtils.STANDARD_DATETIME_FORMATTER);
		if (ts.plusMinutes(REJECT_SECONDS_BEFORE).isBefore(SystemUtils.now())) {
			return true;
		}

		/**
		 * appId隔离<br>
		 * 由于该方式不是集群的，不严格，因此所使用的Map也没必要使用支持并发的Map<br>
		 */
		String request_id = requestBody.getRequest_id();
		Map<RequestIdSoftReference, Object> requestIds = appRequestIds.getOrDefault(request_id,
				new HashMap<RequestIdSoftReference, Object>(10240));

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
			return get().equals(obj);
		}
	}

	public static void main(String[] args) {
//		SoftReference<HashSet<Object>> softReference = new SoftReference<>(new HashSet<>(Arrays.asList(new byte[1024*1024*10])));
//		WeakReference<HashSet<String>> softReference = new WeakReference<>(hashSet);

//		hashSet = null;

//		System.out.println(softReference.get());

//		System.gc();

		int i = 0;
		LinkedList<Object> linkedList = new LinkedList<>();
		for (;;) {
			System.out.println(i++);
			linkedList.add(new SoftReference<>(new byte[1024 * 1024]));

//			if(softReference.get() != null) {
////				linkedList.add(new byte[1024*1024]);
//				softReference.get().add(new byte[1024*1024]);
//			}
//			System.out.println(softReference.get() != null);	

		}

	}

}

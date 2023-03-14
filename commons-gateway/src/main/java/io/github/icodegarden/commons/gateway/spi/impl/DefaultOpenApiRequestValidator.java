package io.github.icodegarden.commons.gateway.spi.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.gateway.core.security.signature.App;
import io.github.icodegarden.commons.gateway.spi.OpenApiRequestValidator;
import io.github.icodegarden.commons.gateway.util.CommonsGatewayUtils;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterMissingErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientPermissionErrorCodeException;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.LogUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class DefaultOpenApiRequestValidator implements OpenApiRequestValidator {

	public static long REJECT_SECONDS_BEFORE = 5 * 60;
	public static long REJECT_SECONDS_AFTER = 10;
	public static Pattern DATETIME_PATTERN = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$");
	public static Function<String, LocalDateTime> TIMESTAMP_PARSER = timestamp -> LocalDateTime.parse(timestamp,
			SystemUtils.STANDARD_DATETIME_FORMATTER);

	private GeneralValidator generalValidator = new GeneralValidator();
	private RequestIdValidator requestIdValidator = new RequestIdValidator();

	@Override
	public void validate(String requestPath, OpenApiRequestBody requestBody, App app) {
		if (log.isDebugEnabled()) {
			log.debug("Validate OpenApi request body:{}", requestBody.toStringExcludeBizContent());
		}

		generalValidator.validate(requestPath, requestBody, app);

		requestIdValidator.validate(requestPath, requestBody, app);
	}

	public static class GeneralValidator {

		public void validate(String requestPath, OpenApiRequestBody requestBody, App app) {
			// ParameterMissing--------------------------------------------------
			if (!StringUtils.hasText(requestBody.getMethod())) {
				throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_METHOD));
			}
			if (!StringUtils.hasText(requestBody.getSign())) {
				throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_SIGNATURE));
			}
			if (!StringUtils.hasText(requestBody.getSign_type())) {
				throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_SIGNATURE_TYPE));
			}
			if (!StringUtils.hasText(requestBody.getApp_id())) {
				throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_APP_ID));
			}
			if (!StringUtils.hasText(requestBody.getTimestamp())) {
				throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_TIMESTAMP));
			}
			if (!StringUtils.hasText(requestBody.getVersion())) {
				throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_VERSION));
			}
			if (!StringUtils.hasText(requestBody.getRequest_id())) {
				throw new ErrorCodeAuthenticationException(new ClientParameterMissingErrorCodeException(
						ClientParameterMissingErrorCodeException.SubPair.MISSING_REQUEST_ID));
			}

			// ParameterInvalid--------------------------------------
			if (requestBody.getApp_id().length() > 32) {
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_APP_ID));
			}

			if (!"JSON".equalsIgnoreCase(requestBody.getFormat())) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_FORMAT:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getFormat()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_FORMAT));
			}
			if (!CommonsGatewayUtils.supportsSignType(requestBody.getSign_type())) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_SIGNATURE_TYPE:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getSign_type()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE_TYPE));
			}
			if (!DATETIME_PATTERN.matcher(requestBody.getTimestamp()).matches()) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_TIMESTAMP:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getTimestamp()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_TIMESTAMP));
			}
			/**
			 * n秒（例如5分钟）之前的视为重放;比现在晚n秒（例如10秒）以上视为不符合
			 */
			LocalDateTime ts = TIMESTAMP_PARSER.apply(requestBody.getTimestamp());
			if (ts.plusSeconds(REJECT_SECONDS_BEFORE).isBefore(SystemUtils.now())
					|| ts.minusSeconds(REJECT_SECONDS_AFTER).isAfter(SystemUtils.now())) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_TIMESTAMP:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getTimestamp()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_TIMESTAMP));
			}
			if (!StringUtils.hasText(requestBody.getCharset()) || !"UTF-8".equalsIgnoreCase(requestBody.getCharset())) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_CHARSET:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getCharset()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_CHARSET));
			}
			if (requestBody.getRequest_id().length() > 32) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_REQUEST_ID:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getRequest_id()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_REQUEST_ID));
			}

			boolean b = CommonsGatewayUtils.validateSign(requestBody, app.getAppKey());
			/**
			 * 验签不通过
			 */
			if (!b) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_SIGNATURE:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getSign()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE));
			}

			// Permission--------------------------------------
			/**
			 * 如果没有配置则表示拥有所有接口权限
			 */
			if (!app.getMethods().isEmpty() && !app.getMethods().contains(requestBody.getMethod())) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INSUFFICIENT_PERMISSIONS:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getMethod()));
				throw new ErrorCodeAuthenticationException(new ClientPermissionErrorCodeException(
						ClientPermissionErrorCodeException.SubPair.INSUFFICIENT_PERMISSIONS));
			}
		}
	}

	public static class RequestIdValidator {

		private LocalDuplicateRequestIdValidator localDuplicateRequestIdValidator = new LocalDuplicateRequestIdValidator();

		public void validate(String requestPath, OpenApiRequestBody requestBody, App app) {
			/**
			 * 把request_id的校验放在签名校验之后，是因为校验防重放可能使用网络IO更耗时，以便前面的验证不通过直接拒绝
			 */
			if (!localDuplicateRequestIdValidator.validate(requestBody)) {
				LogUtils.infoIfEnabled(log, () -> log.info("app:{}.{} of rquest path:{} INVALID_REQUEST_ID:{}",
						app.getAppName(), app.getAppId(), requestPath, requestBody.getRequest_id()));
				throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
						ClientParameterInvalidErrorCodeException.SubPair.INVALID_REQUEST_ID.getSub_code(),
						"Duplicate Request"));
			}
		}
	}

	/**
	 * 使用SoftReference（GC除SoftReference对象仍不够用才回收SoftReference）保存已使用的request_id<br>
	 * WeakReference由于每次GC即回收过于频繁，失去更多准确度
	 * 
	 * @author Fangfang.Xu
	 *
	 */
	@Slf4j
	public static class LocalDuplicateRequestIdValidator {

		private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

		private Object object = new Object();

		private Map<String/* appId */, Map<RequestIdSoftReference, Object>> appRequestIds = new HashMap<>(64);

		public LocalDuplicateRequestIdValidator() {
			/**
			 * 也可以在validate方法最后进行清理检查，但那样需要控制并发
			 */
			new Thread(LocalDuplicateRequestIdValidator.class.getSimpleName()) {
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

		public boolean validate(OpenApiRequestBody requestBody) {
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
				LogUtils.infoIfEnabled(log,
						() -> log.info("openapi request reject by duplicate, request_id:{}, app_id:{}",
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

}

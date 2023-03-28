package io.github.icodegarden.commons.test.web.controller;

import javax.annotation.Nullable;

import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@RestController
public class ApiResponseController {

	/**
	 * 接收OpenApiRequestBody，返回OpenApiResponse
	 */
	@PostMapping("openapi/v1/metadatas/system")
	public ResponseEntity<OpenApiResponse> getSystemMetadata(@RequestBody OpenApiRequestBody body) {
		if (log.isInfoEnabled()) {
			log.info("openapi getKafkaMetadata, request body:{}", body);
		}
		GetSystemMetadataOpenapiDTO dto = deserializeBizContent(body, GetSystemMetadataOpenapiDTO.class);
		dto.validate();

		SystemMetadata vo = new SystemMetadata();
		vo.setRefreshMetadataIntervalMillis(60000L);

		if (log.isInfoEnabled()) {
			log.info("openapi getSystemMetadata, app_id:{}, result:{}", body.getApp_id(), vo);
		}

		OpenApiResponse openApiResponse = OpenApiResponse.success(body.getMethod(), JsonUtils.serialize(vo));
		return ResponseEntity.ok(openApiResponse);
	}

	/**
	 * 接收只有biz_content，返回OpenApiResponse
	 */
	@PostMapping("openapi/v1/metadatas/system_biz_content1")
	public ResponseEntity<OpenApiResponse> getSystemMetadata_biz_content(@RequestBody GetSystemMetadataOpenapiDTO dto) {
		if (log.isInfoEnabled()) {
			log.info("openapi getKafkaMetadata, request body:{}", dto);
		}
		Assert.hasText(dto.getName(), "Missing:name");

		SystemMetadata vo = new SystemMetadata();
		vo.setRefreshMetadataIntervalMillis(60000L);

		OpenApiResponse openApiResponse = OpenApiResponse.success(null, JsonUtils.serialize(vo));
		return ResponseEntity.ok(openApiResponse);
	}

	/**
	 * 接收和返回都只有biz_content
	 */
	@PostMapping("openapi/v1/metadatas/system_biz_content2")
	public ResponseEntity<SystemMetadata> getSystemMetadata(@RequestBody GetSystemMetadataOpenapiDTO dto) {
		if (log.isInfoEnabled()) {
			log.info("openapi getKafkaMetadata, request body:{}", dto);
		}
		Assert.hasText(dto.getName(), "Missing:name");

		/**
		 * 模拟业务失败用
		 */
//		if(1==1) {
//			Assert.hasText(null, "Missing:name");	
//		}

		SystemMetadata vo = new SystemMetadata();
		vo.setRefreshMetadataIntervalMillis(60000L);

		return ResponseEntity.ok(vo);
	}

	protected <T> T deserializeBizContent(OpenApiRequestBody body, Class<T> type) throws ErrorCodeException {
		try {
			return JsonUtils.deserialize(body.getBiz_content(), type);
		} catch (Exception e) {
			log.error("ex on deserialize open api biz_content, OpenApiRequestBody:{}", body, e);
			throw new ClientParameterInvalidErrorCodeException(
					ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(), "biz_content不合法");
		}
	}

	@Data
	public static class GetSystemMetadataOpenapiDTO {

		@Nullable
		private String name;

		public void validate() {
		}
	}

	public static class SystemMetadata {
		private Long refreshMetadataIntervalMillis;

		public Long getRefreshMetadataIntervalMillis() {
			return refreshMetadataIntervalMillis;
		}

		public void setRefreshMetadataIntervalMillis(Long refreshMetadataIntervalMillis) {
			this.refreshMetadataIntervalMillis = refreshMetadataIntervalMillis;
		}

		@Override
		public String toString() {
			return "SystemMetadata [refreshMetadataIntervalMillis=" + refreshMetadataIntervalMillis + "]";
		}
	}
	// ------------------------------------------------------------------------------------------------------

	@GetMapping("api/v1/metadatas/system")
	public ResponseEntity<InternalApiResponse<SystemMetadata>> getSystemMetadata() {
		log.info("request api/v1/metadatas/system");
		
		SystemMetadata vo = new SystemMetadata();
		vo.setRefreshMetadataIntervalMillis(60000L);
		return ResponseEntity.ok(InternalApiResponse.success(vo));
	}

	@GetMapping("api/v1/metadatas/system_biz_content")
	public ResponseEntity<SystemMetadata> getSystemMetadata_biz_content() {
		log.info("request api/v1/metadatas/system_biz_content");
		
		/**
		 * 模拟业务失败用
		 */
//		if(1==1) {
//			Assert.hasText(null, "Missing:name");	
//		}
		
		SystemMetadata vo = new SystemMetadata();
		vo.setRefreshMetadataIntervalMillis(60000L);
		return ResponseEntity.ok(vo);
	}
}

package io.github.icodegarden.commons.elasticsearch.v7.dao;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class BulkResponseHasErrorV7Exception extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;
	private final BulkResponse response;

	public BulkResponseHasErrorV7Exception(String desc, BulkResponse response) {
		this.response = response;

		StringBuilder sb = new StringBuilder(200);
		sb.append(desc);

		List<BulkItemResponse> errorItems = errorItems();
		errorItems.forEach(i -> {
			sb.append("item id:").append(i.getId()).append(", reason:").append(i.getFailure().getMessage())
					.append("\n");
		});
		message = sb.toString();
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public List<BulkItemResponse> errorItems() {
		return Arrays.asList(response.getItems()).stream().filter(i -> i.getFailure() != null)
				.collect(Collectors.toList());
	}

	public List<String> errorIds() {
		return Arrays.asList(response.getItems()).stream().filter(i -> i.getFailure() != null).map(i -> i.getId())
				.collect(Collectors.toList());
	}

	public List<BulkItemResponse> successItems() {
		return Arrays.asList(response.getItems()).stream().filter(i -> i.getFailure() == null)
				.collect(Collectors.toList());
	}

	public List<String> successIds() {
		return Arrays.asList(response.getItems()).stream().filter(i -> i.getFailure() == null).map(i -> i.getId())
				.collect(Collectors.toList());
	}
}

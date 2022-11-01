package io.github.icodegarden.commons.elasticsearch;

import java.util.List;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class BulkResponseHasErrorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;
	private final BulkResponse response;

	public BulkResponseHasErrorException(String desc, BulkResponse response) {
		this.response = response;

		StringBuilder sb = new StringBuilder(200);
		sb.append(desc);

		List<BulkResponseItem> errorItems = errorItems();
		errorItems.forEach(i -> {
			sb.append("item id:").append(i.id()).append(", reason:").append(i.error().reason()).append("\n");
		});
		message = sb.toString();
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public List<BulkResponseItem> errorItems() {
		return response.items().stream().filter(i -> i.error() != null).collect(Collectors.toList());
	}

	public List<String> errorIds() {
		return response.items().stream().filter(i -> i.error() != null).map(i -> i.id()).collect(Collectors.toList());
	}

	public List<BulkResponseItem> successItems() {
		return response.items().stream().filter(i -> i.error() == null).collect(Collectors.toList());
	}

	public List<String> successIds() {
		return response.items().stream().filter(i -> i.error() == null).map(i -> i.id()).collect(Collectors.toList());
	}
}

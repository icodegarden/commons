package io.github.icodegarden.commons.elasticsearch.dao;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.MgetRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.lang.IdObject;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class GenericElasticsearchDao<PO extends IdObject<String>, U extends IdObject<String>, Q extends ElasticsearchQuery<W>, W, DO extends IdObject<String>>
		extends ElasticsearchDao<PO, U, Q, W, DO> {

	public GenericElasticsearchDao(ElasticsearchClient client, String index) {
		this(client, index, null);
	}

	public GenericElasticsearchDao(ElasticsearchClient client, String index, String idFieldName) {
		this(client, index, null, false);
	}

	public GenericElasticsearchDao(ElasticsearchClient client, String index, String idFieldName,
			boolean aliasOfMultiIndex) {
		super(client, index, idFieldName, aliasOfMultiIndex);
	}

	@Override
	protected IndexRequest.Builder<PO> buildIndexRequestBuilderOnAdd(PO po) {
		return new IndexRequest.Builder<PO>().index(getIndex()).id(po.getId()).document(po);
	}

	@Override
	protected BulkRequest.Builder buildBulkRequestBuilderOnAddBatch(Collection<PO> pos) {
		List<BulkOperation> operations = pos.stream().map(po -> {
			return new BulkOperation.Builder().index(b -> b.index(getIndex()).id(po.getId()).document(po)).build();
		}).collect(Collectors.toList());

		return new BulkRequest.Builder().operations(operations);
	}

	@Override
	protected UpdateRequest.Builder<U, U> buildUpdateRequestBuilderOnUpdate(U update) {
		return new UpdateRequest.Builder<U, U>().index(getIndex()).id(update.getId()).doc(update);
	}

	@Override
	protected BulkRequest.Builder buildBulkRequestBuilderOnUpdateBatch(Collection<U> updates) {
		List<BulkOperation> operations = updates.stream().map(update -> {
			return new BulkOperation.Builder()
					.update(b -> b.index(getIndex()).id(update.getId()).action(b2 -> b2.doc(update))).build();
		}).collect(Collectors.toList());

		return new BulkRequest.Builder().operations(operations);
	}

	@Override
	protected SearchRequest.Builder buildSearchRequestBuilderOnFindAll(Q query) {
		//TODO 应该根据Q query实现查询条件，还有其他方法也类似
		return new SearchRequest.Builder().index(getIndex());
	}

	@Override
	protected CountRequest.Builder buildCountRequestBuilderOnCount(Q query) {
		return new CountRequest.Builder().index(getIndex());
	}

	@Override
	protected GetRequest.Builder buildGetRequestBuilderOnFindOne(String id, W with) {
		return new GetRequest.Builder().index(getIndex()).id(id);
	}

	@Override
	protected MgetRequest.Builder buildMgetRequestBuilderOnFindByIds(List<String> ids, W with) {
		return new MgetRequest.Builder().index(getIndex()).ids(ids);
	}

	@Override
	protected DeleteRequest.Builder buildDeleteRequestBuilderOnDelete(String id) {
		return new DeleteRequest.Builder().index(getIndex()).id(id);
	}

	@Override
	protected BulkRequest.Builder buildBulkRequestBuilderOnDeleteBatch(Collection<String> ids) {
		List<BulkOperation> operations = ids.stream().map(id -> {
			return new BulkOperation.Builder().delete(b -> b.index(getIndex()).id(id)).build();
		}).collect(Collectors.toList());

		return new BulkRequest.Builder().operations(operations);
	}

	@Override
	protected DeleteByQueryRequest.Builder buildDeleteByQueryRequestBuilderOnDeleteByQuery(Q query) {
//		return new DeleteByQueryRequest.Builder().index(getIndex());
		throw new UnsupportedOperationException("Please Impl buildDeleteByQueryRequestBuilderOnDeleteByQuery.");
	}

	@Override
	protected String extractSearchAfter(DO obj) {
		return obj.getId();
	}
}

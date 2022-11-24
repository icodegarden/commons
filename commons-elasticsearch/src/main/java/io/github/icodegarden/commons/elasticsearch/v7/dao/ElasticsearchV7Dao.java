package io.github.icodegarden.commons.elasticsearch.v7.dao;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.elasticsearch.dao.ElasticsearchDaoSupport;
import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.elasticsearch.v7.BulkResponseHasErrorV7Exception;
import io.github.icodegarden.commons.lang.query.NextQuerySupportArrayList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportPage;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.PageHelperUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ElasticsearchV7Dao<PO, U, Q extends ElasticsearchQuery<W>, W, DO>
		extends ElasticsearchDaoSupport<PO, U, Q, W, DO> {

	private final RestHighLevelClient client;

	public ElasticsearchV7Dao(RestHighLevelClient client, String index) {
		this(client, index, null);
	}

	public ElasticsearchV7Dao(RestHighLevelClient client, String index, String idFieldName) {
		this(client, index, null, false);
	}

	public ElasticsearchV7Dao(RestHighLevelClient client, String index, String idFieldName, boolean aliasOfMultiIndex) {
		super(index, idFieldName, aliasOfMultiIndex);
		this.client = client;
	}

	@Override
	public void add(PO po) {
		validate(po);

		IndexRequest indexRequest = buildIndexRequestOnAdd(po);
		try {
			IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
			// response:IndexResponse[index=vehicle_assignment,type=_doc,id=1,version=7,result=updated,seqNo=6,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
			if (indexResponse.getShardInfo().getSuccessful() < 1) {
				throw new IllegalStateException(
						"add failed, successful shards:" + indexResponse.getShardInfo().getSuccessful());
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void addBatch(Collection<PO> pos) {
		if (CollectionUtils.isEmpty(pos)) {
			return;
		}

		BulkRequest bulkRequest = buildBulkRequestOnAddBatch(pos);
		try {
			BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
			if (response.hasFailures()) {
				throw new BulkResponseHasErrorV7Exception("addBatch Bulk had errors", response);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int update(U update) {
		try {
			doUpdate(getIndex(), update);
		} catch (ElasticsearchStatusException e) {
			/**
			 * 理用这个可以得到id
			 */
			UpdateRequest updateRequest = buildUpdateRequestOnUpdate(update);
			String id = updateRequest.id();
			/**
			 * alias的次最新，去更新(次最新)的index(结果无论失败)，通常是可以的，因为这种场景是最新的刚出来，次最新里的暂时还需要写操作，而不会是次次最新里的数据
			 */
			doOnRealIndexIf404(getIndex(), id, e, realIndex -> doUpdate(realIndex, update));
		}
		return 1;
	}
	
	/**
	 * Bulk方式
	 */
	@Override
	public int updateBatch(Collection<U> updates) {
		if (CollectionUtils.isEmpty(updates)) {
			return 0;
		}

		BulkRequest bulkRequest = buildBulkRequestOnUpdateBatch(updates);
		try {
			BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
			if (response.hasFailures()) {
				throw new BulkResponseHasErrorV7Exception("updateBatch Bulk had errors", response);
			}
			return updates.size();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void doUpdate(String index, U update) throws ElasticsearchStatusException {
		UpdateRequest updateRequest = buildUpdateRequestOnUpdate(update);
		updateRequest.index(index);
		try {
			UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
			if (updateResponse.getShardInfo().getFailed() > 0) {
				throw new IllegalStateException(
						"update failed, failed shards:" + updateResponse.getShardInfo().getFailed());
			}
		} catch (ElasticsearchStatusException e) {
			if (RestStatus.CONFLICT.equals(e.status())) {
				/**
				 * 通过id的更新，几乎不应该存在并发更新
				 */
				throw new IllegalStateException("conflict on update", e);
			}
			throw e;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public NextQuerySupportPage<DO> findAll(Q query) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		/**
		 * 
		 */
		QueryBuilder queryBuilder = buildQueryBuilder(query);
		searchSourceBuilder.query(queryBuilder);

		searchSourceBuilder.from((query.getPage() - 1) * query.getSize());
		searchSourceBuilder.size(query.getSize() + 1);// more 1
		/**
		 * sort
		 */
		if (StringUtils.hasText(query.getOrderBy())) {
			String[] split1 = query.getOrderBy().split(",");
			for (String s : split1) {
				String[] split2 = s.split(" ");
				searchSourceBuilder.sort(split2[0].trim(),
						split2[1].trim().equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
			}
		}
		if (query.getSearchAfters() != null) {
			searchSourceBuilder.searchAfter(query.getSearchAfters().toArray());
		}
		searchSourceBuilder.timeout(new TimeValue(getReadTimeoutMillis(), TimeUnit.MILLISECONDS));

		boolean isCount = PageHelperUtils.isCount();
		if (!isCount) {
			// 是否进行count，count需要消耗一点性能
			searchSourceBuilder.trackTotalHits(false);
		}

		SearchRequest searchRequest = buildSearchRequestOnFindAll(query);
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHits hits = searchResponse.getHits();
			long total = 0;
			/**
			 * 是否count
			 */
			if (isCount) {
				total = hits.getTotalHits().value;// 按count
			} else {
				total = hits.getHits().length;// 按查询结果数组长度
			}
			NextQuerySupportList<DO> nextQuerySupportList;
			if (total == 0) {
				nextQuerySupportList = NextQuerySupportArrayList.empty();
			} else {
				LinkedList<DO> list = new LinkedList<DO>();
				AtomicReference<Boolean> hasNextPage = new AtomicReference<Boolean>(false);
				int i = 0;
				for (SearchHit hit : hits) {
					if (i != query.getSize()) {// not last one
						DO result = extractResult(hit);
						list.add(result);
					} else {// more 1,last
						hasNextPage.set(true);
						break;
					}

					i++;
				}

				nextQuerySupportList = NextQuerySupportArrayList.newSupportSearchAfter(list,
						last -> hasNextPage.get() ? extractSearchAfter(last) : null);
			}

			NextQuerySupportPage<DO> page = new NextQuerySupportPage<DO>(query.getPage(), query.getSize(), total,
					isCount, query.getOrderBy(), nextQuerySupportList);
			return page;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long count(Q query) {
		QueryBuilder queryBuilder = buildQueryBuilder(query);

		CountRequest countRequest = buildCountRequestOnCount(query);
		countRequest.query(queryBuilder);
		try {
			CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
			return countResponse.getCount();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 不满足时自行覆盖
	 * 
	 * @param query
	 * @return
	 */
	protected BoolQueryBuilder buildQueryBuilder(Q query) {
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		if (query.getTerms() != null) {
			for (Entry<String, Object> entry : query.getTerms().entrySet()) {
				if (entry.getValue() != null) {
					boolQueryBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
				}
			}
		}

		if (query.getMatches() != null) {
			for (Entry<String, Object> entry : query.getMatches().entrySet()) {
				if (entry.getValue() != null) {
					boolQueryBuilder.must(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
				}
			}
		}

		if (query.getMultiMatches() != null) {
			for (Entry<Object, List<String>> entry : query.getMultiMatches().entrySet()) {
				if (entry.getValue() != null) {
					boolQueryBuilder.must(QueryBuilders.multiMatchQuery(entry.getKey(),
							entry.getValue().toArray(new String[entry.getValue().size()])));
				}
			}
		}

		if (query.getRangeFroms() != null) {
			for (Entry<String, Object> entry : query.getRangeFroms().entrySet()) {
				if (entry.getValue() != null) {
					boolQueryBuilder.must(QueryBuilders.rangeQuery(entry.getKey()).from(entry.getValue(), true));
				}
			}
		}

		if (query.getRangeTos() != null) {
			for (Entry<String, Object> entry : query.getRangeTos().entrySet()) {
				if (entry.getValue() != null) {
					boolQueryBuilder.must(QueryBuilders.rangeQuery(entry.getKey()).to(entry.getValue(), true));
				}
			}
		}

		if (query.getWildcards() != null) {
			for (Entry<String, Object> entry : query.getWildcards().entrySet()) {
				if (entry.getValue() != null) {
					boolQueryBuilder
							.must(QueryBuilders.wildcardQuery(entry.getKey(), String.format("*%s*", entry.getValue())));
				}
			}
		}
		return boolQueryBuilder;
	}

	/**
	 * 先根据id查index<br>
	 * 如果没有则会404，接着若是aliasOfMultiIndex，则使用id term查，如果还是没有就确实不存在
	 */
	@Override
	public DO findOne(String id, W with) {
		GetRequest getRequest = buildGetRequestOnFindOne(id, with);
		try {
			GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
			if (!getResponse.isExists()) {
				return null;
			}
			return extractResult(getResponse);
		} catch (ElasticsearchStatusException e) {
			/**
			 * 404
			 */
			if (RestStatus.NOT_FOUND.equals(e.status())) {
				if (isAliasOfMultiIndex()) {
					/**
					 * 如果source中没有定义id字段：不支持通过id查询（因为无法确定real index，pre也不一定准确）
					 */
					SearchHit hit = findOneIfAliasOfMultiIndex(id);
					return hit == null ? null : extractResult(hit);
				}

				/**
				 * 单索引，id确实不存在
				 */
				return null;
			}

//			前置化查询
//			/**
//			 * 400：别名of多索引情况下，get /{id} 一定是400
//			 */
//			if (RestStatus.BAD_REQUEST.equals(e.status())
//					&& e.getDetailedMessage().contains("has more than one indices associated")) {
//				/**
//				 * source中没有定义id字段：不支持通过id查询（因为无法确定real index，pre也不一定准确）
//				 */
//				if (idFieldName == null) {
//					throw e;
//				}
//				SearchCondition condition = new SearchCondition();
//				condition.term(idFieldName, id);
//				List<T> list = list(condition, ret);
//				return list.isEmpty() ? null : list.get(0);
//			}
			throw e;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<DO> findByIds(List<String> ids, W with) {
		MultiGetRequest multiGetRequest = buildMultiGetRequestOnFindByIds(ids, with);
		try {
			MultiGetResponse multiGetResponse = client.mget(multiGetRequest, RequestOptions.DEFAULT);

			MultiGetItemResponse[] responses = multiGetResponse.getResponses();
			if (responses == null || responses.length == 0) {
				return Collections.emptyList();
			}

			List<DO> list = Arrays.asList(responses).stream().map(response -> extractResult(response.getResponse()))
					.collect(Collectors.toList());
			return list;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 文档可能存在于非最新索引，可以使用term查询还处于可读的索引（hot、warm、cold）
	 * 
	 * @param id
	 * @return
	 */
	private SearchHit findOneIfAliasOfMultiIndex(String id) {
		if (isAliasOfMultiIndex()) {
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must(QueryBuilders.termQuery(getIdFieldName(), id));
			sourceBuilder.query(boolQueryBuilder);
			sourceBuilder.size(1);
			sourceBuilder.timeout(new TimeValue(getReadTimeoutMillis(), TimeUnit.MILLISECONDS));

			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices(getIndex());
			searchRequest.source(sourceBuilder);

			try {
				SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
				SearchHits hits = searchResponse.getHits();
				long total = hits.getTotalHits().value;
				return total == 0 ? null : hits.iterator().next();
			} catch (IOException e2) {
				throw new IllegalStateException(e2);
			}
		}
		return null;
	}

	@Override
	public int delete(String id) {
		try {
			doDelete(getIndex(), id);
		} catch (ElasticsearchStatusException e) {
			doOnRealIndexIf404(getIndex(), id, e, realIndex -> doDelete(realIndex, id));
		}
		return 1;
	}

	private void doDelete(String index, String id) throws ElasticsearchStatusException {
		DeleteRequest deleteRequest = buildDeleteRequestOnDelete(id);
		deleteRequest.index(index);
		try {
			DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
			/**
			 * 删除时如果数据不存在，以前的版本不会报ElasticsearchStatusException 404，即使报了这里做冗余也不会错的
			 */
			if (deleteResponse.getResult().equals(org.elasticsearch.action.DocWriteResponse.Result.NOT_FOUND)) {
				throw new ElasticsearchStatusException("result not found", RestStatus.NOT_FOUND);
			}
			if (deleteResponse.getShardInfo().getSuccessful() < 1) {
				throw new IllegalStateException(
						"delete failed, successful shards:" + deleteResponse.getShardInfo().getSuccessful());
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int deleteBatch(Collection<String> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return 0;
		}

		BulkRequest bulkRequest = buildBulkRequestOnDeleteBatch(ids);
		try {
			BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
			if (response.hasFailures()) {
				throw new BulkResponseHasErrorV7Exception("deleteBatch Bulk had errors", response);
			}
			return ids.size();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param consumer<T> T 真实索引
	 */
	private void doOnRealIndexIf404(String index, String id, ElasticsearchStatusException e,
			Consumer<String> consumer) {
		/**
		 * 404：#数据确实不存在；#别名of多索引情况下，使用id进行更新或删除(删除时的404不是es
		 * client报出来的，是得到response后解析并throw的ElasticsearchStatusException)，
		 * id对应到最新的索引是ok的，如果是老索引则404
		 */
		if (RestStatus.NOT_FOUND.equals(e.status())) {
			/**
			 * index不是aliasOfMultiIndex，则数据确实不存在
			 */
			if (!isAliasOfMultiIndex()) {
				throw e;
			}
			/**
			 * source中有id字段：精确查询该数据的index去更新
			 */
			SearchHit hit = findOneIfAliasOfMultiIndex(id);
			if (hit == null) {
				/**
				 * 还是没有数据
				 */
				throw new IllegalArgumentException("doc really Not Found with id:" + id, e);
			}
			String realIndex = hit.getIndex();
			consumer.accept(realIndex);
			return;
		}
		/**
		 * 其他错误
		 */
		throw e;
	}

	protected abstract IndexRequest buildIndexRequestOnAdd(PO po);

	protected abstract BulkRequest buildBulkRequestOnAddBatch(Collection<PO> pos);

	protected abstract UpdateRequest buildUpdateRequestOnUpdate(U update);
	
	protected abstract BulkRequest buildBulkRequestOnUpdateBatch(Collection<U> updates);

	protected abstract SearchRequest buildSearchRequestOnFindAll(Q query);

	protected abstract CountRequest buildCountRequestOnCount(Q query);

	protected abstract GetRequest buildGetRequestOnFindOne(String id, W with);

	protected abstract MultiGetRequest buildMultiGetRequestOnFindByIds(List<String> ids, W with);

	protected abstract DeleteRequest buildDeleteRequestOnDelete(String id);

	protected abstract BulkRequest buildBulkRequestOnDeleteBatch(Collection<String> ids);

	protected DO extractResult(SearchHit hit) {
		String json = hit.getSourceAsString();
		return JsonUtils.deserialize(json, getClassDO());
	}

	protected DO extractResult(GetResponse getResponse) {
		String json = getResponse.getSourceAsString();
		return JsonUtils.deserialize(json, getClassDO());
	}

}

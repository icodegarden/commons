package io.github.icodegarden.commons.elasticsearch.dao;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.ErrorResponse;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.MgetRequest;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.get.GetResult;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import io.github.icodegarden.commons.elasticsearch.BulkResponseHasErrorException;
import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.lang.IdObject;
import io.github.icodegarden.commons.lang.query.NextQuerySupportArrayList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportPage;
import io.github.icodegarden.commons.lang.util.LogUtils;
import io.github.icodegarden.commons.lang.util.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class ElasticsearchDao<PO, U, Q extends ElasticsearchQuery<W>, W, DO>
		extends ElasticsearchDaoSupport<PO, U, Q, W, DO> {

	private final ElasticsearchClient client;
//	private final ElasticsearchAsyncClient asyncClient;

	public ElasticsearchDao(ElasticsearchClient client, String index) {
		this(client, index, null);
	}

	public ElasticsearchDao(ElasticsearchClient client, String index, String idFieldName) {
		this(client, index, null, false);
	}

	public ElasticsearchDao(ElasticsearchClient client, String index, String idFieldName, boolean aliasOfMultiIndex) {
		super(index, idFieldName, aliasOfMultiIndex);
		this.client = client;
//		ElasticsearchTransport transport = client._transport();
//		this.asyncClient = new ElasticsearchAsyncClient(transport);
	}

	@Override
	public void add(PO po) {
		validate(po);

		IndexRequest.Builder<PO> builder = buildIndexRequestBuilderOnAdd(po);
		builder.index(getIndex());
		try {
			IndexResponse indexResponse = client.index(builder.build());
			// response:IndexResponse[index=vehicle_assignment,type=_doc,id=1,version=7,result=updated,seqNo=6,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
			if (indexResponse.shards().successful().intValue() < 1) {
				throw new IllegalStateException(
						"add failed, successful shards:" + indexResponse.shards().successful().intValue());
			}
			IdObject.setIdIfNecessary(indexResponse.id(), po);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void addBatch(Collection<PO> pos) {
		if (CollectionUtils.isEmpty(pos)) {
			return;
		}

		BulkRequest.Builder builder = buildBulkRequestBuilderOnAddBatch(pos);
//		builder.index(index);
		try {
			BulkResponse response = client.bulk(builder.build());
			if (response.errors()) {
				throw new BulkResponseHasErrorException("addBatch Bulk had errors", response);
			}

			List<BulkResponseItem> items = response.items();
			if (pos.size() == items.size()) {
				Iterator<PO> it1 = pos.iterator();
				Iterator<BulkResponseItem> it2 = items.iterator();
				while (it1.hasNext()) {
					PO po = it1.next();
					BulkResponseItem item = it2.next();
					IdObject.setIdIfNecessary(item.id(), po);
				}
			} else {
				LogUtils.warnIfEnabled(log,
						() -> log.warn("can not setId after addBatch, size not eq, pos size:{}, responseItems size:{}",
								pos.size(), items.size()));
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int update(U update) {
		try {
			doUpdate(getIndex(), update);
		} catch (ElasticsearchException e) {
			LogUtils.warnIfEnabled(log, () -> log.warn("Elasticsearch update status is {}, index:{}, update:{}",
					e.status(), getIndex(), update));

			/**
			 * 理用这个可以得到id
			 */
			UpdateRequest.Builder<U, U> builder = buildUpdateRequestBuilderOnUpdate(update);
			String id = builder.build().id();
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

		BulkRequest.Builder builder = buildBulkRequestBuilderOnUpdateBatch(updates);
//		builder.index(index);
		try {
			BulkResponse response = client.bulk(builder.build());
			if (response.errors()) {
				throw new BulkResponseHasErrorException("updateBatch Bulk had errors", response);
			}
			return updates.size();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void doUpdate(String index, U update) throws ElasticsearchException {
		UpdateRequest.Builder<U, U> builder = buildUpdateRequestBuilderOnUpdate(update);
		builder.index(index);
		try {
			UpdateResponse<U> updateResponse = client.update(builder.build(), (Class) update.getClass());
			if (updateResponse.shards().failed().intValue() > 0) {
				throw new IllegalStateException(
						"update failed, failed shards:" + updateResponse.shards().failed().intValue());
			}
		} catch (ElasticsearchException e) {
			if (e.status() == 409) {// CONFLICT
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
		SearchRequest.Builder builder = buildSearchRequestBuilderOnFindAll(query);

		/**
		 * 
		 */
		co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder queryBuilder = buildQueryBuilder(query);
		builder.query(queryBuilder.build());

		builder.from((query.getPage() - 1) * query.getSize());
		builder.size(query.getSize() + 1);// more 1
		/**
		 * sort
		 */
		if (StringUtils.hasText(query.getOrderBy())) {
			String[] split1 = query.getOrderBy().split(",");
			for (String s : split1) {
				String[] split2 = s.split(" ");

				builder.sort(b -> {
					return b.field(b2 -> {
						return b2.field(split2[0].trim())
								.order(split2[1].trim().equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc);
					});
				});
			}
		}
		if (query.getSearchAfters() != null) {
			builder.searchAfter(query.getSearchAfters().stream().map(i -> i.toString()).collect(Collectors.toList()));
		}
		builder.timeout(getReadTimeoutMillis() + "ms");

		boolean isCount = PageHelperUtils.isCount();
		if (!isCount) {
			// 是否进行count，count需要消耗一点性能
			builder.trackTotalHits(b -> b.enabled(false));
		}

		try {
			SearchResponse<DO> searchResponse = client.search(builder.build(), getClassDO());
			HitsMetadata<DO> hits = searchResponse.hits();
			long total = 0;
			/**
			 * 是否count
			 */
			if (isCount) {
				total = hits.total().value();// 按count
			} else {
				total = hits.hits().size();// 按查询结果数组长度
			}
			NextQuerySupportList<DO> nextQuerySupportList;
			if (total == 0) {
				nextQuerySupportList = NextQuerySupportArrayList.empty();
			} else {
				LinkedList<DO> list = new LinkedList<DO>();
				AtomicReference<Boolean> hasNextPage = new AtomicReference<Boolean>(false);
				int i = 0;
				for (Hit<DO> hit : hits.hits()) {
					if (i != query.getSize()) {// not last one
						DO result = hit.source();

						IdObject.setIdIfNecessary(hit.id(), result);

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
		CountRequest.Builder builder = buildCountRequestBuilderOnCount(query);

		co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder queryBuilder = buildQueryBuilder(query);
		builder.query(queryBuilder.build());
		try {
			CountResponse countResponse = client.count(builder.build());
			return countResponse.count();
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
	protected co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder buildQueryBuilder(Q query) {
		co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder queryBuilder = new co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder();
		BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

		if (query.getTerms() != null) {
			for (Entry<String, Object> entry : query.getTerms().entrySet()) {
				if (entry.getValue() != null) {
					boolBuilder.must(b -> b.term(b2 -> b2.field(entry.getKey()).value(entry.getValue().toString())));
				}
			}
		}

		if (query.getMatches() != null) {
			for (Entry<String, Object> entry : query.getMatches().entrySet()) {
				if (entry.getValue() != null) {
					boolBuilder.must(b -> b.match(b2 -> b2.field(entry.getKey()).query(entry.getValue().toString())));
				}
			}
		}

		if (query.getMultiMatches() != null) {
			for (Entry<Object, List<String>> entry : query.getMultiMatches().entrySet()) {
				if (entry.getValue() != null) {
					String value = entry.getValue().stream().collect(Collectors.joining(" "));
					boolBuilder.must(b -> b.multiMatch(b2 -> b2.fields(entry.getKey().toString()).query(value)));
				}
			}
		}

		if (query.getRangeFroms() != null) {
			for (Entry<String, Object> entry : query.getRangeFroms().entrySet()) {
				if (entry.getValue() != null) {
					boolBuilder.must(b -> b.range(b2 -> b2.field(entry.getKey()).gte(JsonData.of(entry.getValue()))));
				}
			}
		}

		if (query.getRangeTos() != null) {
			for (Entry<String, Object> entry : query.getRangeTos().entrySet()) {
				if (entry.getValue() != null) {
					boolBuilder.must(b -> b.range(b2 -> b2.field(entry.getKey()).lte(JsonData.of(entry.getValue()))));
				}
			}
		}

		if (query.getWildcards() != null) {
			for (Entry<String, Object> entry : query.getWildcards().entrySet()) {
				if (entry.getValue() != null) {
					boolBuilder.must(b -> b
							.wildcard(b2 -> b2.field(entry.getKey()).value(String.format("*%s*", entry.getValue()))));
				}
			}
		}

		queryBuilder.bool(boolBuilder.build());
		return queryBuilder;
	}

	/**
	 * 先根据id查index<br>
	 * 如果没有则会404，接着若是aliasOfMultiIndex，则使用id term查，如果还是没有就确实不存在
	 */
	@Override
	public DO findOne(String id, W with) {
		GetRequest.Builder builder = buildGetRequestBuilderOnFindOne(id, with);
		try {
			GetResponse<DO> getResponse = client.get(builder.build(), getClassDO());
			if (!getResponse.found()) {
				return null;
			}

			DO result = getResponse.source();
			IdObject.setIdIfNecessary(getResponse.id(), result);

			return result;
		} catch (ElasticsearchException e) {
			LogUtils.warnIfEnabled(log,
					() -> log.warn("Elasticsearch findOne status is {}, index:{}, id:{}", e.status(), getIndex(), id));

			/**
			 * 404
			 */
			if (e.status() == 404) {
				if (isAliasOfMultiIndex()) {
					/**
					 * 如果source中没有定义id字段：不支持通过id查询（因为无法确定real index，pre也不一定准确）
					 */
					Hit<DO> hit = findOneIfAliasOfMultiIndex(id);
					return hit == null ? null : hit.source();
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
		MgetRequest.Builder builder = buildMgetRequestBuilderOnFindByIds(ids, with);
		try {
			MgetResponse<DO> mgetResponse = client.mget(builder.build(), getClassDO());

			List<MultiGetResponseItem<DO>> docs = mgetResponse.docs();
			if (CollectionUtils.isEmpty(docs)) {
				return Collections.emptyList();
			}

			List<DO> list = docs.stream().map(doc -> {
				GetResult<DO> getResult = doc.result();

				DO result = getResult.source();

				IdObject.setIdIfNecessary(getResult.id(), result);

				return result;
			}).collect(Collectors.toList());
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
	private Hit<DO> findOneIfAliasOfMultiIndex(String id) {
		if (isAliasOfMultiIndex()) {
			LogUtils.infoIfEnabled(log, () -> log.info("findOneIfAliasOfMultiIndex index:{}, id:{}", getIndex(), id));

			co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder queryBuilder = new co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder();
			BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
			boolBuilder.must(b -> b.term(b2 -> b2.field(getIdFieldName()).value(id)));
			SearchRequest searchRequest = new SearchRequest.Builder().index(getIndex()).query(queryBuilder.build())
					.size(1).timeout(getReadTimeoutMillis() + "ms").build();
			try {
				SearchResponse<DO> searchResponse = client.search(searchRequest, getClassDO());

				HitsMetadata<DO> hits = searchResponse.hits();
				long total = hits.total().value();
				return total == 0 ? null : hits.hits().get(0);
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
		} catch (ElasticsearchException e) {
			LogUtils.warnIfEnabled(log,
					() -> log.warn("Elasticsearch delete status is {}, index:{}, id:{}", e.status(), getIndex(), id));

			doOnRealIndexIf404(getIndex(), id, e, realIndex -> doDelete(realIndex, id));
		}
		return 1;
	}

	private void doDelete(String index, String id) throws ElasticsearchException {
		DeleteRequest.Builder builder = buildDeleteRequestBuilderOnDelete(id);
		builder.index(index);
		try {
			DeleteResponse deleteResponse = client.delete(builder.build());
			/**
			 * 删除时如果数据不存在，以前的版本不会报ElasticsearchStatusException 404，即使报了这里做冗余也不会错的
			 */
			if (deleteResponse.result().equals(co.elastic.clients.elasticsearch._types.Result.NotFound)) {
				throw new ElasticsearchException("result not found", new ErrorResponse.Builder().status(404)
						.error(new ErrorCause.Builder().reason("not found").build()).build());
			}
			if (deleteResponse.shards().successful().intValue() < 1) {
				throw new IllegalStateException(
						"delete failed, successful shards:" + deleteResponse.shards().successful().intValue());
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

		BulkRequest.Builder builder = buildBulkRequestBuilderOnDeleteBatch(ids);
		try {
			BulkResponse response = client.bulk(builder.build());
			if (response.errors()) {
				throw new BulkResponseHasErrorException("deleteBatch Bulk had errors", response);
			}
			return ids.size();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param consumer<T> T 真实索引
	 */
	private void doOnRealIndexIf404(String index, String id, ElasticsearchException e, Consumer<String> consumer) {
		/**
		 * 404：#数据确实不存在；#别名of多索引情况下，使用id进行更新或删除(删除时的404不是es
		 * client报出来的，是得到response后解析并throw的ElasticsearchStatusException)，
		 * id对应到最新的索引是ok的，如果是老索引则404
		 */
		if (e.status() == 404) {
			/**
			 * index不是aliasOfMultiIndex，则数据确实不存在
			 */
			if (!isAliasOfMultiIndex()) {
				throw e;
			}
			/**
			 * source中有id字段：精确查询该数据的index去更新
			 */
			Hit<DO> hit = findOneIfAliasOfMultiIndex(id);
			if (hit == null) {
				/**
				 * 还是没有数据
				 */
				throw new IllegalArgumentException("doc really Not Found with id:" + id, e);
			}
			String realIndex = hit.index();
			consumer.accept(realIndex);
			return;
		}
		/**
		 * 其他错误
		 */
		throw e;
	}

	protected abstract IndexRequest.Builder<PO> buildIndexRequestBuilderOnAdd(PO po);

	protected abstract BulkRequest.Builder buildBulkRequestBuilderOnAddBatch(Collection<PO> pos);

	protected abstract UpdateRequest.Builder<U, U> buildUpdateRequestBuilderOnUpdate(U update);

	protected abstract BulkRequest.Builder buildBulkRequestBuilderOnUpdateBatch(Collection<U> updates);

	protected abstract SearchRequest.Builder buildSearchRequestBuilderOnFindAll(Q query);

	protected abstract CountRequest.Builder buildCountRequestBuilderOnCount(Q query);

	protected abstract GetRequest.Builder buildGetRequestBuilderOnFindOne(String id, W with);

	protected abstract MgetRequest.Builder buildMgetRequestBuilderOnFindByIds(List<String> ids, W with);

	protected abstract DeleteRequest.Builder buildDeleteRequestBuilderOnDelete(String id);

	protected abstract BulkRequest.Builder buildBulkRequestBuilderOnDeleteBatch(Collection<String> ids);

}

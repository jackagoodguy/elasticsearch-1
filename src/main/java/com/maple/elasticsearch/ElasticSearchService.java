package com.maple.elasticsearch;

import com.maple.elasticsearch.config.*;
import com.maple.elasticsearch.model.ElasticBaseDocument;
import com.maple.elasticsearch.model.EsSearchResult;
import com.maple.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by maple on 2021/2/1
 */
@Slf4j
@Component
public class ElasticSearchService {

  @Autowired
  private RestHighLevelClient client;

  @Autowired
  protected ElasticConfig elasticConfig;


  public List<String> listAllIndex() {
    List<String> resultList = new ArrayList<>();
    GetAliasesRequest request = new GetAliasesRequest();
    GetAliasesResponse alias;
    try {
      alias = client.indices().getAlias(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new ESException("listAllIndex error", e);
    }
    Map<String, Set<AliasMetadata>> map = alias.getAliases();
    map.forEach((k, v) -> {
      if (!k.startsWith(".")) {//忽略elasticSearch 默认的
        resultList.add(k);
      }
    });
    return resultList;
  }

  /**
   * 判断一个 index 是否存在
   */
  public boolean isIndexExists(String index) {
    try {
      return client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new ESException("isIndexExists error", e);
    }
  }

  /**
   * 创建 ElasticSearch 索引
   */
  public CreateIndexResponse createIndex(EsIndex index, Setting setting) {
    return createIndex(index.getCode(), setting, index.getDocumentClass());
  }

  /**
   * 根据注解 ESField 自动创建索引
   */
  public CreateIndexResponse createIndex(String index, Setting setting, Class<?> clazz) {
    return createIndex(
        new Index().setName(index).setSetting(setting).setMapping(new Mapping(buildPropertiesMapping(clazz))));
  }

  /**
   * 创建 ElasticSearch 索引
   */
  public CreateIndexResponse createIndex(Index index) {
    CreateIndexRequest indexRequest = new CreateIndexRequest(index.getName());

    if (index.getSetting() != null) {
      indexRequest.settings((Settings.builder()
          .put("index.number_of_shards", index.getSetting().getShards())
          .put("index.number_of_replicas", index.getSetting().getReplicas())
      ));
    }
    String mapping = JsonUtils.toString(index.getMapping());
    log.info("createIndex mapping = {}", mapping);
    indexRequest.mapping(mapping, XContentType.JSON);
    try {
      return client.indices().create(indexRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new ESException("创建索引异常", e);
    }
  }


  /**
   * 删除索引
   */
  public AcknowledgedResponse deleteIndex(String index) {
    try {
      return client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new ESException("deleteIndex error", e);
    }
  }

  /**
   * 刷新索引
   */
  public RefreshResponse refreshEsIndexes(String... indices) {
    try {
      return client.indices().refresh(new RefreshRequest(indices), RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new ESException("refreshEsIndexes error", e);
    }
  }

  /**
   * 创建文档
   */
  public <V extends ElasticBaseDocument> IndexResponse createDocument(String index, V value) {
    IndexRequest request = new IndexRequest(index)
        .source(JsonUtils.toString(value), XContentType.JSON);
    if (Strings.isNotBlank(value.get_id())) {
      request.id(value.get_id());
    }
    esLogInfo("createDocument request = {}", request.toString());
    try {
      IndexResponse response = client.index(request, RequestOptions.DEFAULT);
      esLogInfo("createDocument response = {}", response.toString());
      return response;
    } catch (final IOException e) {
      throw new ESException("插入索引文档异常", e);
    }
  }

  /**
   * 批量创建文档
   */
  public <V extends ElasticBaseDocument> BulkResponse bulkCreateDocuments(String index, List<V> values) {
    BulkRequest request = new BulkRequest();
    values.forEach(value -> {
      IndexRequest indexRequest = new IndexRequest(index)
          .source(JsonUtils.toString(value), XContentType.JSON);
      if (Strings.isNotBlank(value.get_id())) {
        indexRequest.id(value.get_id());
      }
      request.add(indexRequest);
    });
    try {
      BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
      esLogInfo("bulkCreateDocuments ids = {},value = {},UpdateResponse = {}", values.stream().map(ElasticBaseDocument::get_id).collect(Collectors.toList()), JsonUtils.toString(values), JsonUtils.toString(response));
      return response;
    } catch (IOException e) {
      throw new ESException("批量插入索引文档异常", e);
    }
  }

  /**
   * 更新文档
   */
  public <V extends ElasticBaseDocument> UpdateResponse updateDocument(String index, V value) {
    if (Strings.isBlank(value.get_id())) {
      throw new ESException("更新索引文档异常,esId 不能为空");
    }
    UpdateRequest request = new UpdateRequest();
    request.index(index);
    request.id(value.get_id());
    request.doc(JsonUtils.toString(value), XContentType.JSON);
    request.retryOnConflict(elasticConfig.getUpdateRetryOnConflict());
    try {
      UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
      esLogInfo("id = {},value = {},UpdateResponse = {}", value.get_id(), JsonUtils.toString(value), JsonUtils.toString(response));
      return response;
    } catch (IOException e) {
      throw new ESException("更新索引文档异常", e);
    }
  }

  /**
   * 批量更新文档
   */
  public <V extends ElasticBaseDocument> BulkResponse bulkUpdateDocuments(List<V> values) {
    BulkRequest request = new BulkRequest();
    values.forEach(value -> {
      UpdateRequest indexRequest = new UpdateRequest()
          .index(value.getIndex())
          .doc(JsonUtils.toString(value), XContentType.JSON)
          .retryOnConflict(elasticConfig.getUpdateRetryOnConflict());
      if (Strings.isNotBlank(value.get_id())) {
        indexRequest.id(value.get_id());
      }
      request.add(indexRequest);
    });
    try {
      BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
      esLogInfo("ids = {},value = {}", values.stream().map(ElasticBaseDocument::get_id).collect(Collectors.toList()), JsonUtils.toString(values));
      return response;
    } catch (IOException e) {
      throw new ESException("批量插入索引文档异常", e);
    }
  }


  /**
   * 通过ID进行查询
   */
  public <T> T fetchById(Class<T> clazz, String index, String id) {
    GetRequest getRequest = new GetRequest(index, id);
    GetResponse getResponse;
    try {
      getResponse = client.get(getRequest, RequestOptions.DEFAULT);
      esLogInfo("findById  id = {}, getResponse = {}", id, JsonUtils.toString(getResponse));
    } catch (IOException e) {
      throw new ESException("fetchById error", e);
    }
    return getResponse.getSourceAsString() != null ? JsonUtils.fromOrException(getResponse.getSourceAsString(), clazz) : null;
  }

  /**
   * 通过ID进行批量查询
   */
  public <V> EsSearchResult<V> fetchByIds(Class<V> clazz, String index, String... ids) {
    IdsQueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds(ids);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder);
    return fetchBySearchSourceBuilder(clazz, index, searchSourceBuilder);
  }

  public <V> EsSearchResult<V> fetchBySearchSourceBuilder(Class<V> clazz, String index, SearchSourceBuilder searchSourceBuilder) {
    searchSourceBuilder.trackTotalHits(elasticConfig.isTrackTotalHits());
    SearchRequest searchRequest = new SearchRequest()
        .indices(index)
        .source(searchSourceBuilder);
    esLogInfo("findBySearchSourceBuilder searchRequest = {}", searchRequest.toString());
    updateIndexMaxResultWindow(index, searchSourceBuilder.from(), searchSourceBuilder.size());
    try {
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      SearchHit[] searchHit = searchResponse.getHits().getHits();
      long totalHits = searchResponse.getHits().getTotalHits().value;
      long took = searchResponse.getTook().getMillis();
      List<V> results = new ArrayList<>();
      for (SearchHit document : searchHit) {
        String sourceAsString = document.getSourceAsString();
        results.add(JsonUtils.fromOrException(sourceAsString, clazz));
      }
      return new EsSearchResult<>(results, totalHits, took);
    } catch (ElasticsearchStatusException e) {
      if (RestStatus.NOT_FOUND.equals(e.status())) {
        log.warn("findBySearchSourceBuilder message = {}", e.getMessage());
        return new EsSearchResult<>(new ArrayList<>(), 0L, 0L);
      }
      throw new ESException("search error", e);
    } catch (IOException e) {
      throw new ESException("search error", e);
    }
  }

  //更新索引的max_result_window参数
  private boolean updateIndexMaxResultWindow(String index, int from, int size) {
    int querySize = from + size;
    if (querySize <= 10000) {
      return true;
    }
    return updateIndexSetting(index, "index.max_result_window", querySize);
  }

  private boolean updateIndexSetting(String index, String key, int value) {
    UpdateSettingsRequest request = new UpdateSettingsRequest(index)
        .settings(Settings.builder()
            .put(key, value)
            .build()
        );
    try {
      AcknowledgedResponse updateSettingsResponse = client.indices().putSettings(request, RequestOptions.DEFAULT);
      return updateSettingsResponse.isAcknowledged();
    } catch (IOException e) {
      throw new ESException("updateIndexSetting error", e);
    }
  }

  public <V> EsSearchResult<V> fetchByScroll(Class<V> clazz, String index, SearchSourceBuilder searchSourceBuilder) {
    try {
      SearchRequest searchRequest = new SearchRequest()
          .indices(index)
          .source(searchSourceBuilder)
          .scroll(TimeValue.timeValueMinutes(1L));
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      long took = searchResponse.getTook().millis();
      List<V> results = new ArrayList<>();
      while (searchResponse.getHits().getHits().length > 0) {
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHit = hits.getHits();
        for (SearchHit document : searchHit) {
          String sourceAsString = document.getSourceAsString();
          results.add(JsonUtils.fromOrException(sourceAsString, clazz));
        }
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
        scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
        searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        took += searchResponse.getTook().millis();
      }
      return new EsSearchResult<>(results, (long) results.size(), took);
    } catch (IOException e) {
      throw new ESException("fetchByScroll error", e);
    }
  }


  private void esLogInfo(String format, Object... arguments) {
    if (elasticConfig.isEsModelLogSwitch()) {
      log.info(format, arguments);
    }
  }

  /**
   * 反射获取clazz标记ESField的字段,自动构建mapping
   */
  public static Map<String, Property> buildPropertiesMapping(Class clazz) {
    List<Field> esFieldList = FieldUtils.getFieldsListWithAnnotation(clazz, ESField.class);
    Map<String, Property> properties = new HashMap<>(esFieldList.size());
    for (Field field : esFieldList) {
      ESField annotation = field.getAnnotation(ESField.class);
      if (annotation.unwrapped()) {
        properties.putAll(buildPropertiesMapping(field.getType()));
        continue;
      }
      Property property = new Property();
      String name = Strings.isNotBlank(annotation.name()) ? annotation.name() : field.getName();
      property.setType(annotation.type());
      property.setIndex(annotation.index());
      property.setAnalyzer(annotation.analyzer());
      property.setFormat(annotation.format());
      if ("nested".equals(annotation.type()) || "object".equals(annotation.type())) {
        //嵌套一般只针对 collection
        if (Collection.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType) {
          Type argType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
          if (argType instanceof ParameterizedTypeImpl) {
            property.setNestedProperties(buildPropertiesMapping(((ParameterizedTypeImpl) argType).getRawType()));
          } else {
            property.setNestedProperties(buildPropertiesMapping((Class<?>) argType));
          }
        } else {
          property.setNestedProperties(buildPropertiesMapping(field.getType()));
        }
      }
      properties.put(name, property);
    }
    return properties;
  }


}

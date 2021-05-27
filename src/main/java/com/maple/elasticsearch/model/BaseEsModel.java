package com.maple.elasticsearch.model;

import com.maple.elasticsearch.*;
import com.maple.elasticsearch.config.*;
import com.maple.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by maple on 2021/1/22
 */
@Data
@Slf4j
public class BaseEsModel<V extends ElasticBaseDocument> {
  @Autowired
  protected ElasticSearchService elasticSearchService;
  @Autowired
  protected ElasticConfig elasticConfig = new ElasticConfig();

  private final Class<V> valuesClass;
  private final EsIndex esIndex;

  public BaseEsModel(Class<V> valuesClass, EsIndex esIndex) {
    this.valuesClass = valuesClass;
    this.esIndex = esIndex;
  }

  public V findById(String index, String id) {
    return elasticSearchService.fetchById(valuesClass, index, id);
  }

  public String insert(V value) {
    createIndexIfNotExists(value.getIndex());
    return elasticSearchService.createDocument(value.getIndex(), value).getId();
  }

  private void createIndexIfNotExists(String index) {
    if (!elasticSearchService.isIndexExists(index)) {
      log.info("index {} is not exists. start to create", index);
      CreateIndexResponse response = elasticSearchService.createIndex(index, new Setting(elasticConfig.getShards(), elasticConfig.getReplicas()), esIndex.getDocumentClass());
      if (!response.isAcknowledged()) {
        throw new ESException("ElasticSearch create error");
      }
    }
  }

  public List<String> batchInsert(List<V> allValues) {
    if (allValues.isEmpty()) {
      return new ArrayList<>();
    }
    Map<String, List<V>> indexValuesMap = allValues.stream().collect(Collectors.groupingBy(ElasticBaseDocument::getIndex));
    List<String> ids = new ArrayList<>();
    indexValuesMap.forEach((index, values) -> {
      try {
        createIndexIfNotExists(index);
        BulkResponse bulkResponse = elasticSearchService.bulkCreateDocuments(index, values);
        for (BulkItemResponse item : bulkResponse.getItems()) {
          ids.add(item.getId());
        }
      } catch (Exception e) {
        log.error("batchInsert error index = {},valueIds= {}", index, JsonUtils.toString(values.stream().map(ElasticBaseDocument::get_id).collect(Collectors.toList())), e);
      }
    });
    return ids;
  }

  public UpdateResponse update(V value) {
    return elasticSearchService.updateDocument(value.getIndex(), value);
  }

  public BulkResponse batchUpdate(List<V> values) {
    return elasticSearchService.bulkUpdateDocuments(values);
  }

  public EsSearchResult<V> findByCondition(AbstractBaseEsCondition condition) {
    if (condition.getIndex() == null) {
      throw new ESException("index cant be null");
    }
    if (condition.getFrom() == null && condition.getSize() == null) {
      return elasticSearchService.fetchByScroll(valuesClass, condition.getIndex(), condition.getSearchSourceBuilder());
    } else {
      return elasticSearchService.fetchBySearchSourceBuilder(valuesClass, condition.getIndex(), condition.getSearchSourceBuilder());
    }
  }

}
